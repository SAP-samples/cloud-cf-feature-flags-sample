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
	params     ClientParams
	httpClient *http.Client
}

func NewClient(params ClientParams) Client {
	return Client{
		params:     params,
		httpClient: http.DefaultClient,
	}
}

type httpError struct {
	status  int
	message string
}

func (c Client) CreateFlag(flag Flag) error {
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

func (c Client) postFeatureFlag(flag Flag) *httpError {
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

	return nil
}
