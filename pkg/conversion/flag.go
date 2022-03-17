package conversion

import (
	"fmt"
	"log"
	"strconv"
	"strings"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/launchdarkly"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/sap"
)

func ToLDFlag(sapFlag sap.Flag) launchdarkly.Flag {
	return launchdarkly.Flag{
		Name:        sapFlag.ID,
		Key:         sapFlag.ID,
		Kind:        resolveKind(sapFlag.VariationType),
		Description: sapFlag.Description,
		Temporary:   false,
		Variations:  resolveVariations(sapFlag),
		Defaults:    resolveDefaults(sapFlag),
	}
}

func resolveKind(sapVariationType string) string {
	if sapVariationType == sap.FlagTypeBoolean || sapVariationType == sap.FlagTypeString {
		return strings.ToLower(sapVariationType)
	}
	panic(fmt.Sprintf("found unexpected variation type %s", sapVariationType))
}

func resolveVariations(sapFlag sap.Flag) []launchdarkly.Variation {
	ldVariations := make([]launchdarkly.Variation, 0, len(sapFlag.Variations))

	for _, sapVariation := range sapFlag.Variations {
		if sapFlag.VariationType == sap.FlagTypeBoolean {
			ldVariation, err := strconv.ParseBool(sapVariation)
			if err != nil {
				panic(fmt.Sprintf("found unexpected boolean variation value %s for flag %s", sapVariation, sapFlag.ID))
			}
			ldVariations = append(ldVariations, launchdarkly.Variation{Value: ldVariation})
		} else {
			ldVariations = append(ldVariations, launchdarkly.Variation{Value: sapVariation})
		}
	}

	return ldVariations
}

func resolveDefaults(sapFlag sap.Flag) launchdarkly.DefaultVariation {
	if sapFlag.ReleaseDetails != nil {
		releaseVariationIndex := sapFlag.ReleaseDetails.VariationIndex
		return launchdarkly.DefaultVariation{OffVariation: releaseVariationIndex, OnVariation: releaseVariationIndex}
	}

	if sapFlag.VariationType == sap.FlagTypeBoolean {
		return launchdarkly.DefaultVariation{OffVariation: 0, OnVariation: 1}
	}

	return launchdarkly.DefaultVariation{
		OffVariation: sapFlag.OffVariationIndex,
		OnVariation:  sapFlag.DefaultVariationIndex,
	}
}

func SetLDTargets(sapFlag sap.Flag, ldFlag *launchdarkly.Flag) {
	ldFlag.Targets.Instructions = append(ldFlag.Targets.Instructions, stateInstruction(sapFlag))
	if sapFlag.ReleaseDetails != nil {
		// no direct shipment rules or weight distribution apply in this case
		return
	}

	rules := rulesInstruction(sapFlag, *ldFlag)
	if rules != nil {
		ldFlag.Targets.Instructions = append(ldFlag.Targets.Instructions, *rules)
	}

	weights := weightsInstruction(sapFlag, *ldFlag)
	if weights != nil {
		ldFlag.Targets.Instructions = append(ldFlag.Targets.Instructions, *weights)
	}
}

func stateInstruction(sapFlag sap.Flag) launchdarkly.Instruction {
	if sapFlag.Enabled {
		return launchdarkly.Instruction{Kind: launchdarkly.TurnFlagOnInstructionKind}
	}

	if sapFlag.ReleaseDetails != nil {
		if sapFlag.ReleaseDetails.CurrentPercentage != 100 {
			log.Printf("[WARN] Flag %s is in process of release. Migrating it as fully relased flag.\n", sapFlag.ID)
		}
		return launchdarkly.Instruction{Kind: launchdarkly.TurnFlagOnInstructionKind}
	}

	return launchdarkly.Instruction{Kind: launchdarkly.TurnFlagOffInstructionKind}
}

func rulesInstruction(sapFlag sap.Flag, ldFlag launchdarkly.Flag) *launchdarkly.Instruction {
	if len(sapFlag.DirectShipments) == 0 {
		return nil
	}

	instruction := launchdarkly.Instruction{
		Kind:  launchdarkly.ReplaceRulesInstructionKind,
		Rules: make([]launchdarkly.Rule, 0, len(sapFlag.DirectShipments)),
	}

	for _, directShipment := range sapFlag.DirectShipments {
		instruction.Rules = append(instruction.Rules, launchdarkly.Rule{
			VariationID: ldFlag.GetVariationID(directShipment.VariationIndex),
			Clauses: []launchdarkly.Clause{
				{
					Attribute: "identifier",
					Op:        "in",
					Values:    directShipment.Receivers,
				},
			},
		})
	}
	return &instruction
}

func weightsInstruction(sapFlag sap.Flag, ldFlag launchdarkly.Flag) *launchdarkly.Instruction {
	if len(sapFlag.WeightedChoices) == 0 {
		return nil
	}

	instruction := launchdarkly.Instruction{
		Kind:           launchdarkly.WeightsInstructionKind,
		RolloutWeights: map[string]int{},
	}

	sum := 0
	for _, choice := range sapFlag.WeightedChoices {
		variationID := ldFlag.GetVariationID(choice.VariationIndex)
		instruction.RolloutWeights[variationID] = choice.Weight * 1000
		sum += choice.Weight
	}

	if sum != 100 {
		var defaultVariationID string
		if sapFlag.VariationType == sap.FlagTypeBoolean {
			defaultVariationID = ldFlag.GetVariationID(1) // Set default to the "true" variation which is the second
		} else {
			defaultVariationID = ldFlag.GetVariationID(ldFlag.Defaults.OnVariation)
		}
		instruction.RolloutWeights[defaultVariationID] += (100 - sum) * 1000
	}

	return &instruction
}
