package main

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	ldapi "github.com/launchdarkly/api-client-go"
)

const (
	apiTokenParamName     = "api-token"
	projectKeyParamName   = "project-key"
	jsonFilePathParamName = "flags-file"
)

type parameters struct {
	APIToken     string
	ProjectKey   string
	JSONFilePath string
}

func (p parameters) validate() {
	if p.APIToken == "" {
		log.Fatalf("%s parameter not provided\n", apiTokenParamName)
	}
	if p.ProjectKey == "" {
		log.Fatalf("%s parameter not provided\n", projectKeyParamName)
	}
	if p.JSONFilePath == "" {
		log.Fatalf("%s parameter not provided\n", jsonFilePathParamName)
	}
}

func getParams() parameters {
	params := parameters{}
	flag.StringVar(&params.APIToken, apiTokenParamName, "", "LaunchDarkly API token")
	flag.StringVar(&params.ProjectKey, projectKeyParamName, "", "LaunchDarkly Project key")
	flag.StringVar(&params.JSONFilePath, jsonFilePathParamName, "", "Path to json file exported from Feature Flags dashboard")
	flag.Parse()
	params.validate()
	return params
}

func checkError(err error) {
	if err != nil {
		log.Fatalln(err)
	}
}

func ldApiTokenContext(apiToken string) context.Context {
	return context.WithValue(context.Background(), ldapi.ContextAPIKey, ldapi.APIKey{
		Key: apiToken,
	})
}

func readFlags(jsonFilePath string) Flags {
	fileContent, err := os.ReadFile(jsonFilePath)
	checkError(err)
	var flags Flags
	err = json.Unmarshal(fileContent, &flags)
	checkError(err)
	log.Printf("%d flags read from %s", len(flags.Flags), jsonFilePath)
	return flags
}

func main() {
	params := getParams()
	flags := readFlags(params.JSONFilePath)
	//fmt.Println(flags)
	createFlagsInLaunchDarkly(params, flags)
	//ctx := ldApiTokenContext(params.APIToken)
	//client := ldapi.NewAPIClient(ldapi.NewConfiguration())
	//
	//valOneVal := "variation 1"
	//valTwoVal := "variation 2"
	//var valOne interface{} = valOneVal //map[string]interface{}{"one": valOneVal}
	//var valTwo interface{} = valTwoVal //map[string]interface{}{"one": valOneVal}
	//
	//flag := ldapi.FeatureFlagBody{
	//	Name:        "test-flag-s3",
	//	Key:         "test-flag-s3-key",
	//	Description: "test desc",
	//	Variations: []ldapi.Variation{{
	//		Id:          "id1",
	//		Name:        "variation 1",
	//		Description: "description 1",
	//		Value:       &valOne,
	//	}, {
	//		Id:          "id2",
	//		Name:        "variation 2",
	//		Description: "description 2",
	//		Value:       &valTwo,
	//	}},
	//	Temporary:              false,
	//	Tags:                   nil,
	//	IncludeInSnippet:       false,
	//	ClientSideAvailability: nil,
	//	Defaults: &ldapi.Defaults{
	//		OnVariation:  0,
	//		OffVariation: 1,
	//	},
	//}
	//ff, _, err := client.FeatureFlagsApi.PostFeatureFlag(ctx, params.ProjectKey, flag, nil)
	//checkError(err)
	////body, err := io.ReadAll(resp.Body)
	////checkError(err)
	////fmt.Println("body:", string(body))
	//fmt.Println("ff:", ff)
}

func handleSwaggerError(swaggerError ldapi.GenericSwaggerError) error {
	if strings.Contains(swaggerError.Error(), http.StatusText(http.StatusTooManyRequests)) {
		log.Println("Sleeping for 2 seconds due to API rate limit")
		time.Sleep(time.Second * 2)
		err := createFlagInLaunchDarkly(ctx, client, params.ProjectKey, flag)
	} else {
		failedFlags[flag.ID] = fmt.Errorf("error during creation of flag %s - %s: %s", flag.ID,
			swaggerError.Error(), string(swaggerError.Body()))
	}

}

func createFlagsInLaunchDarkly(params parameters, flags Flags) {
	ctx, client := createClient(params)
	failedFlags := make(map[string]error)
	for _, flag := range flags.Flags {
		if err := createFlagInLaunchDarkly(ctx, client, params.ProjectKey, flag); err != nil {
			swaggerError, ok := err.(ldapi.GenericSwaggerError)
			if ok {
				err = handleSwaggerError(swaggerError, flag.ID)
			}
			if err != nil {
				failedFlags[flag.ID] = err
			}
		}
	}
	allFlagsCount := len(flags.Flags)
	log.Printf("%d/%d flags created successfully\n", allFlagsCount-len(failedFlags), allFlagsCount)
	for flagID, err := range failedFlags {
		log.Printf("Flag %s could not be created. Reason: %s\n", flagID, err.Error())
	}
}

func createClient(params parameters) (context.Context, *ldapi.APIClient) {
	return ldApiTokenContext(params.APIToken), ldapi.NewAPIClient(ldapi.NewConfiguration())
}

var (
	VariationFalse interface{} = false
	VariationTrue  interface{} = true
)

func createFlagInLaunchDarkly(ctx context.Context, client *ldapi.APIClient, projectKey string, flag Flag) error {
	//valOneVal := "variation 1"
	//valTwoVal := "variation 2"
	//var valOne interface{} = valOneVal //map[string]interface{}{"one": valOneVal}
	//var valTwo interface{} = valTwoVal //map[string]interface{}{"one": valOneVal}

	ldVariations := make([]ldapi.Variation, len(flag.Variations))

	if flag.VariationType == "BOOLEAN" {
		ldVariations = append(ldVariations, ldapi.Variation{
			Value: &VariationFalse,
		}, ldapi.Variation{
			Value: &VariationTrue,
		})
	} else {
		for _, variation := range flag.Variations {
			var v interface{} = variation
			ldVariations = append(ldVariations, ldapi.Variation{
				Value: &v,
			})
		}
	}

	ldFlag := ldapi.FeatureFlagBody{
		Name:                   flag.ID,
		Key:                    flag.ID,
		Description:            flag.Description,
		Variations:             ldVariations,
		Temporary:              false,
		Tags:                   nil,
		IncludeInSnippet:       false,
		ClientSideAvailability: nil,
		Defaults: &ldapi.Defaults{
			OnVariation:  flag.DefaultVariationIndex,
			OffVariation: flag.OffVariationIndex,
		},
	}
	_, _, err := client.FeatureFlagsApi.PostFeatureFlag(ctx, projectKey, ldFlag, nil)
	return err
}

type ldError struct {
	Status int
	Error  error
}

type Flags struct {
	Flags []Flag `json:"flags"`
}

type Flag struct {
	ID                    string            `json:"id"`
	Description           string            `json:"description"`
	DirectShipments       []DirectShipments `json:"directShipments"`
	WeightedChoices       []WeightedChoices `json:"weightedChoices"`
	VariationType         string            `json:"variationType"`
	Variations            []string          `json:"variations"`
	OffVariationIndex     int32             `json:"offVariationIndex"`
	DefaultVariationIndex int32             `json:"defaultVariationIndex"`
	Enabled               bool              `json:"enabled"`
}

type DirectShipments struct {
	ID             int      `json:"id"`
	VariationIndex int      `json:"variationIndex"`
	Receivers      []string `json:"receivers"`
}

type WeightedChoices struct {
	VariationIndex int `json:"variationIndex"`
	Weight         int `json:"weight"`
}
