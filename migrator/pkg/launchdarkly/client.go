package launchdarkly

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
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
	client.initEnvironments()
	return client
}

type httpError struct {
	status  int
	message string
}

type Project struct {
	Environments []Environment `json:"environments"`
}

type Environment struct {
	Key string `json:"key"`
}

func (c *Client) initEnvironments() {
	url := fmt.Sprintf("%s/api/v2/projects/%s", BaseURL, c.params.ProjectKey)

	req, err := http.NewRequest(http.MethodGet, url, nil)
	errors.Check(err)
	req.Header.Set("Authorization", c.params.APIKey)

	resp, err := c.httpClient.Do(req)
	errors.Check(err)

	project := Project{}
	err = json.NewDecoder(resp.Body).Decode(&project)
	errors.Check(err)
	envKeys := make([]string, 0, len(project.Environments))
	for _, env := range project.Environments {
		envKeys = append(envKeys, env.Key)
	}
	c.environments = envKeys
}

func (c *Client) SetFlagRules(flag *Flag) error {
	var err *httpError
	for _, environment := range c.environments {
		for i := 0; i <= c.params.MaxRetries; i++ {
			err = c.patchFeatureFlag(flag, environment)
			if err == nil {
				log.Printf("Flag rules %s created successfully\n", flag.Key)
				break
			}

			time.Sleep(c.params.SleepAfterRequest)

			shouldRetry := err.status == http.StatusTooManyRequests

			if !shouldRetry {
				break
			}

			sleepDuration := c.params.SleepAfterTooManyRequests
			log.Printf("Sleeping for %s due to API rate limit\n", sleepDuration)
			time.Sleep(sleepDuration)
		}
		if err != nil {
			break
		}
	}
	if err != nil {
		return errors.New(err.message)
	}
	return nil
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

func (c *Client) CreateFlag(flag *Flag) error {
	var err *httpError
	for i := 0; i <= c.params.MaxRetries; i++ {
		err = c.postFeatureFlag(flag)
		if err == nil {
			log.Printf("Flag %s created successfully\n", flag.Key)
			return nil
		}

		time.Sleep(c.params.SleepAfterRequest)

		shouldRetry := err.status == http.StatusTooManyRequests

		if !shouldRetry {
			break
		}

		sleepDuration := c.params.SleepAfterTooManyRequests
		log.Printf("Sleeping for %s due to API rate limit\n", sleepDuration)
		time.Sleep(sleepDuration)
	}

	return errors.New(err.message)
}

func (c *Client) postFeatureFlag(flag *Flag) *httpError {
	content, err := json.Marshal(flag)
	errors.Check(err)

	url := fmt.Sprintf("%s/api/v2/flags/%s", BaseURL, c.params.ProjectKey)
	req, err := http.NewRequest(http.MethodPost, url, bytes.NewReader(content))
	errors.Check(err)
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", c.params.APIKey)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return &httpError{status: -1, message: err.Error()}
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		respBodyContent, _ := io.ReadAll(resp.Body)
		return &httpError{
			status:  resp.StatusCode,
			message: fmt.Sprintf("could not create flag %s, status: %d, %s", flag.Key, resp.StatusCode, string(respBodyContent)),
		}
	}

	if err := json.NewDecoder(resp.Body).Decode(flag); err != nil {
		return &httpError{
			status:  -1,
			message: fmt.Sprintf("could not parse flag %s: %s", flag.Key, err.Error()),
		}
	}

	return nil
}
