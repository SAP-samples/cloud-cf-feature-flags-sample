package parameters

import (
	"flag"
	"log"
	"sync"
	"time"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/launchdarkly"
)

const (
	apiKeyParamName                    = "api-key"
	projectKeyParamName                = "project-key"
	sleepAfterRequestParamName         = "sleep-after-request"
	sleepAfterTooManyRequestsParamName = "sleep-after-too-many-requests"
	maxRetriesParamName                = "max-retries"
	jsonFilePathParamName              = "flags-file"
)

type Parameters struct {
	launchdarkly.ClientParams
	JSONFilePath string
}

var params Parameters
var once sync.Once

func (p Parameters) validate() {
	if p.APIKey == "" {
		log.Fatalf("%s parameter not provided\n", apiKeyParamName)
	}
	if p.ProjectKey == "" {
		log.Fatalf("%s parameter not provided\n", projectKeyParamName)
	}
	if p.JSONFilePath == "" {
		log.Fatalf("%s parameter not provided\n", jsonFilePathParamName)
	}
}

func Get() Parameters {
	once.Do(func() {
		flag.StringVar(&params.APIKey, apiKeyParamName, "", "LaunchDarkly API key")
		flag.StringVar(&params.ProjectKey, projectKeyParamName, "", "LaunchDarkly Project key")
		flag.StringVar(&params.JSONFilePath, jsonFilePathParamName, "",
			"Path to json file exported from Feature Flags dashboard")
		flag.DurationVar(&params.SleepAfterRequest, sleepAfterRequestParamName, 1*time.Second,
			"How much time to wait between subsequent requests to LaunchDarkly (default: 1s)")
		flag.DurationVar(&params.SleepAfterTooManyRequests, sleepAfterTooManyRequestsParamName, 2*time.Second,
			"How much time to wait after LaunchDarkly has returned 429 Too Many Requests status (default: 2s)")
		flag.IntVar(&params.MaxRetries, maxRetriesParamName, 10,
			"How many times to retry a request that has received a 429 Too Many Requests status (default: 10)")

		flag.Parse()
		params.validate()
	})
	return params
}
