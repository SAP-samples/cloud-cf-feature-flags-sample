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
}

func setLDTargets(sapFlag sap.Flag, ldFlag *launchdarkly.Flag) {
	// Set Flag enabled/disabled
	var turnFlagInstruction string
	if sapFlag.Enabled || sapFlag.ReleaseDetails != nil {
		turnFlagInstruction = launchdarkly.TurnFlagOnInstructionKind
	} else {
		turnFlagInstruction = launchdarkly.TurnFlagOffInstructionKind
	}
	ldFlag.Targets.Instructions = append(ldFlag.Targets.Instructions, launchdarkly.Instruction{
		Kind: turnFlagInstruction,
	})
	if sapFlag.ReleaseDetails != nil {
		if sapFlag.ReleaseDetails.CurrentPercentage != 100 {
			log.Printf("Flag %s is in process of release. Setting flag to released\n", sapFlag.ID)
		}
		return
	}

	// Set Flag rules
	if len(sapFlag.DirectShipments) > 0 {
		rulesInstruction := launchdarkly.Instruction{
			Kind:  launchdarkly.ReplaceRulesInstructionKind,
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
			Kind:           launchdarkly.WeightsInstructionKind,
			RolloutWeights: map[string]int{},
		}
		sum := 0
		for _, choice := range sapFlag.WeightedChoices {
			variationID := ldFlag.Variations[choice.VariationIndex].ID
			weightInstruction.RolloutWeights[variationID] = choice.Weight * 1000
			sum += choice.Weight
		}
		if sum != 100000 {
			var defaultVariationID string
			if sapFlag.VariationType == "BOOLEAN" {
				defaultVariationID = ldFlag.Variations[1].ID // Set default to the "true" variation which is the second
			} else {
				defaultVariationID = ldFlag.Variations[ldFlag.Defaults.OnVariation].ID
			}
			weightInstruction.RolloutWeights[defaultVariationID] += 100000 - sum*1000
		}
		ldFlag.Targets.Instructions = append(ldFlag.Targets.Instructions, weightInstruction)
	}
}

func convertFlag(sapFlag sap.Flag) launchdarkly.Flag {
	defaults := launchdarkly.DefaultVariation{
		OffVariation: sapFlag.OffVariationIndex,
		OnVariation:  sapFlag.DefaultVariationIndex,
	}
	if sapFlag.ReleaseDetails != nil {
		releaseVariationIndex := sapFlag.ReleaseDetails.VariationIndex
		defaults.OnVariation = releaseVariationIndex
		defaults.OnVariation = releaseVariationIndex
	}
	return launchdarkly.Flag{
		Name:        sapFlag.ID,
		Kind:        resolveKind(sapFlag.VariationType),
		Key:         sapFlag.ID,
		Description: sapFlag.Description,
		Temporary:   false,
		Variations:  resolveVariations(sapFlag),
		Defaults:    defaults,
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
