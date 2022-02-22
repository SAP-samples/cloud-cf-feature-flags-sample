package sap

import (
	"encoding/json"
	"log"
	"os"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/errors"
)

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
	OffVariationIndex     int               `json:"offVariationIndex"`
	DefaultVariationIndex int               `json:"defaultVariationIndex"`
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

func ReadFlags(jsonFilePath string) Flags {
	fileContent, err := os.ReadFile(jsonFilePath)
	errors.Check(err)

	var flags Flags
	err = json.Unmarshal(fileContent, &flags)
	errors.Check(err)
	log.Printf("%d flags read from %s", len(flags.Flags), jsonFilePath)

	return flags
}
