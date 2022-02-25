package main

import (
	"flag"
	"fmt"
	"log"
	"strconv"
	"time"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/launchdarkly"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/sap"
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

func getParams() Parameters {
	params := Parameters{}
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
	return params
}

func main() {
	params := getParams()
	sapFlags := sap.ReadFlags(params.JSONFilePath)
	createFlagsInLaunchDarkly(params, sapFlags.Flags)
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

func setLDTargets(sapFlag sap.Flag, ldFlag *launchdarkly.Flag) {
	// Set Flag enabled/disabled
	var turnFlagInstruction string
	if sapFlag.Enabled {
		turnFlagInstruction = "turnFlagOn"
	} else {
		turnFlagInstruction = "turnFlagOff"
	}
	ldFlag.Targets.Instructions = append(ldFlag.Targets.Instructions, launchdarkly.Instruction{
		Kind: turnFlagInstruction,
	})

	// Set Flag rules
	if len(sapFlag.DirectShipments) > 0 {
		rulesInstruction := launchdarkly.Instruction{
			Kind:  "replaceRules",
			Rules: make([]launchdarkly.Rule, 0, len(sapFlag.DirectShipments)),
		}

		for _, directShipment := range sapFlag.DirectShipments {
			rulesInstruction.Rules = append(rulesInstruction.Rules, launchdarkly.Rule{
				VariationID: ldFlag.Variations[directShipment.VariationIndex].ID,
				Clauses: []launchdarkly.Clause{
					{
						Attribute: "identifier",
						Op:        "in",
						Values:    directShipment.Receivers,
					},
				},
			})
		}
		ldFlag.Targets.Instructions = append(ldFlag.Targets.Instructions, rulesInstruction)
	}

	// Set Flag weights
	if len(sapFlag.WeightedChoices) > 0 {
		weightInstruction := launchdarkly.Instruction{
			Kind:           "updateFallthroughVariationOrRollout",
			RolloutWeights: map[string]int{},
		}
		for _, choice := range sapFlag.WeightedChoices {
			variationID := ldFlag.Variations[choice.VariationIndex].ID
			weightInstruction.RolloutWeights[variationID] = choice.Weight * 100
		}
		ldFlag.Targets.Instructions = append(ldFlag.Targets.Instructions, weightInstruction)
	}
}

func convertFlag(sapFlag sap.Flag) launchdarkly.Flag {
	return launchdarkly.Flag{
		Name:        sapFlag.ID,
		Kind:        resolveKind(sapFlag.VariationType),
		Key:         sapFlag.ID,
		Description: sapFlag.Description,
		Temporary:   false,
		Variations:  resolveVariations(sapFlag),
		Defaults: launchdarkly.DefaultVariation{
			OffVariation: sapFlag.OffVariationIndex,
			OnVariation:  sapFlag.DefaultVariationIndex,
		},
	}
}

func resolveKind(sapVariationType string) string {
	if sapVariationType == "BOOLEAN" {
		return "boolean"
	}
	if sapVariationType == "STRING" {
		return "string"
	}
	panic(fmt.Sprintf("found unexpected variation type %s", sapVariationType))
}

func resolveVariations(sapFlag sap.Flag) []launchdarkly.Variation {
	variations := make([]launchdarkly.Variation, 0, len(sapFlag.Variations))

	for _, sapVariation := range sapFlag.Variations {
		if sapFlag.VariationType == "BOOLEAN" {
			variationValue, err := strconv.ParseBool(sapVariation)
			if err != nil {
				panic(fmt.Sprintf("found unexpected boolean variation value %s for flag %s", sapVariation, sapFlag.ID))
			}
			variations = append(variations, launchdarkly.Variation{Value: variationValue})
		} else {
			variations = append(variations, launchdarkly.Variation{Value: sapVariation})
		}
	}

	return variations
}

func createFlagsInLaunchDarkly(params Parameters, flags []sap.Flag) {
	client := launchdarkly.NewClient(params.ClientParams)
	failedFlags := make(map[string]error)

	for _, sapFlag := range flags {
		ldFlag := convertFlag(sapFlag)
		if err := client.CreateFlag(&ldFlag); err != nil {
			failedFlags[ldFlag.Key] = err
		} else {
			setLDTargets(sapFlag, &ldFlag)
			err = client.SetFlagRules(&ldFlag)
			if err != nil {
				failedFlags[ldFlag.Key] = err
			}
		}
	}

	allFlagsCount := len(flags)
	log.Printf("%d/%d flags created successfully\n", allFlagsCount-len(failedFlags), allFlagsCount)
	for _, err := range failedFlags {
		log.Println(err.Error())
	}
}

/*
{
    "environmentKey": "production",
    "instructions": [
        {
            "kind": "turnFlagOn"
        },
        {
            "kind": "replaceRules",
            "rules": [
                {
                    "variationId": "0adf85c4-3334-4e0e-94cd-e26332565f29",
                    "clauses": [
                        {
                            "attribute": "identifier",
                            "op": "in",
                            "values": [
                                "t1"
                            ]
                        }
                    ]
                },
                {
                    "variationId": "c7bbbcbc-5efe-463c-9a36-121c88252e3a",
                    "clauses": [
                        {
                            "attribute": "identifier",
                            "op": "in",
                            "values": [
                                "t2"
                            ]
                        }
                    ]
                }
            ]
        },
        {
            "kind": "updateFallthroughVariationOrRollout",
            "rolloutWeights": {
                "0adf85c4-3334-4e0e-94cd-e26332565f29": 50000,
                "c7bbbcbc-5efe-463c-9a36-121c88252e3a": 50000
            },
            "rolloutBucketBy": null
        }
    ]
}
*/
