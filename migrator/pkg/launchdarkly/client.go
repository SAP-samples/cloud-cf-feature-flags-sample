package launchdarkly

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/errors"
)

type ClientParams struct {
	ProjectKey string
	APIKey     string

	SleepAfterRequest         time.Duration
	SleepAfterTooManyRequests time.Duration
	MaxRetries                int
}

type Client struct {
	params       ClientParams
	httpClient   *http.Client
	environments []string
}

func NewClient(params ClientParams) *Client {
	client := &Client{
		params:     params,
		httpClient: http.DefaultClient,
	}
	client.loadEnvironments()
	log.Printf("[INFO] Flags will be created in the following environments: %s\n",
		strings.Join(client.environments, ", "))
	return client
}

type httpError struct {
	status  int
	message string
}

type project struct {
	Environments []environment `json:"environments"`
}

type environment struct {
	Key string `json:"key"`
}

func (c *Client) loadEnvironments() {
	url := fmt.Sprintf("%s/api/v2/projects/%s", BaseURL, c.params.ProjectKey)

	req, err := http.NewRequest(http.MethodGet, url, nil)
	errors.Check(err)
	req.Header.Set("Authorization", c.params.APIKey)

	resp, err := c.httpClient.Do(req)
	errors.Check(err)
	defer resp.Body.Close()

	project := project{}
	err = json.NewDecoder(resp.Body).Decode(&project)
	errors.Check(err)

	envKeys := make([]string, 0, len(project.Environments))
	for _, env := range project.Environments {
		envKeys = append(envKeys, env.Key)
	}
	c.environments = envKeys
}

func (c *Client) withRetry(action func() *httpError) {
	for i := 0; i <= c.params.MaxRetries; i++ {
		err := action()
		if err == nil {
			return
		}

		time.Sleep(c.params.SleepAfterRequest)

		shouldRetry := err.status == http.StatusTooManyRequests

		if !shouldRetry {
			return
		}

		sleepDuration := c.params.SleepAfterTooManyRequests
		log.Printf("[INFO] Sleeping for %s due to API rate limit\n", sleepDuration)
		time.Sleep(sleepDuration)
	}
}

func (c *Client) CreateFlag(flag Flag) (*Flag, error) {
	var err *httpError
	var createdFlag *Flag

	c.withRetry(func() *httpError {
		createdFlag, err = c.postFeatureFlag(flag)
		return err
	})

	if err == nil {
		log.Printf("[INFO] Flag %s created successfully\n", flag.Key)
		return createdFlag, nil
	}

	return nil, errors.New(err.message)
}

func (c *Client) postFeatureFlag(flag Flag) (*Flag, *httpError) {
	content, err := json.Marshal(flag)
	errors.Check(err)

	url := fmt.Sprintf("%s/api/v2/flags/%s", BaseURL, c.params.ProjectKey)
	req, err := http.NewRequest(http.MethodPost, url, bytes.NewReader(content))
	errors.Check(err)

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", c.params.APIKey)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, &httpError{status: -1, message: err.Error()}
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		respBodyContent, _ := io.ReadAll(resp.Body)
		return nil, &httpError{
			status:  resp.StatusCode,
			message: fmt.Sprintf("could not create flag %s, status: %d, %s", flag.Key, resp.StatusCode, string(respBodyContent)),
		}
	}

	var newlyCreatedFlag Flag
	if err := json.NewDecoder(resp.Body).Decode(&newlyCreatedFlag); err != nil {
		return nil, &httpError{
			status:  -1,
			message: fmt.Sprintf("could not parse flag %s: %s", flag.Key, err.Error()),
		}
	}

	return &newlyCreatedFlag, nil
}

func (c *Client) UpdateFlagRules(flag *Flag) error {
	for _, environment := range c.environments {
		if err := c.updateFlagInEnvironment(flag, environment); err != nil {
			return errors.New(err.message)
		}
	}
	return nil
}

func (c *Client) updateFlagInEnvironment(flag *Flag, environment string) *httpError {
	var err *httpError

	c.withRetry(func() *httpError {
		err = c.patchFeatureFlag(flag, environment)
		return err
	})

	if err == nil {
		log.Printf("[INFO] Flag rules for %s created successfully in environment %s\n", flag.Key, environment)
		return nil
	}

	return err
}

type SemanticPatchBody struct {
	EnvironmentKey string `json:"environmentKey"`
	Targets        `json:",inline"`
}

func (c *Client) patchFeatureFlag(flag *Flag, environment string) *httpError {
	content, err := json.Marshal(&SemanticPatchBody{
		EnvironmentKey: environment,
		Targets:        flag.Targets,
	})
	errors.Check(err)

	url := fmt.Sprintf("%s/api/v2/flags/%s/%s", BaseURL, c.params.ProjectKey, flag.Key)
	requestBodyBytes := bytes.NewReader(content)

	req, err := http.NewRequest(http.MethodPatch, url, requestBodyBytes)
	errors.Check(err)
	req.Header.Set("Content-Type", "application/json; domain-model=launchdarkly.semanticpatch")
	req.Header.Set("Authorization", c.params.APIKey)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return &httpError{status: -1, message: err.Error()}
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		respBodyContent, _ := io.ReadAll(resp.Body)
		return &httpError{
			status:  resp.StatusCode,
			message: fmt.Sprintf("could not create flag %s, status: %d, %s", flag.Key, resp.StatusCode, string(respBodyContent)),
		}
	}

	return nil
}
