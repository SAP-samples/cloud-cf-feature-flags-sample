package sap

import (
	"encoding/json"
	"log"
	"os"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/errors"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/parameters"
)

const (
	FlagTypeBoolean = "BOOLEAN"
	FlagTypeString  = "STRING"
)

type Flags struct {
	Flags []Flag `json:"flags"`
}

type Flag struct {
	ID                    string            `json:"id"`
	Description           string            `json:"description"`
	DirectShipments       []DirectShipments `json:"directShipments"`
	WeightedChoices       []WeightedChoices `json:"weightedChoices"`
	ReleaseDetails        *ReleaseDetails   `json:"releaseDetails,omitempty"`
	VariationType         string            `json:"variationType"`
	Variations            []string          `json:"variations"`
	OffVariationIndex     int               `json:"offVariationIndex"`
	DefaultVariationIndex int               `json:"defaultVariationIndex"`
	Enabled               bool              `json:"enabled"`
}

type ReleaseDetails struct {
	VariationIndex    int `json:"variationIndex"`
	CurrentPercentage int `json:"currentPercentage"`
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

func ReadFlags() Flags {
	params := parameters.Get()
	fileContent, err := os.ReadFile(params.JSONFilePath)
	errors.Check(err)

	var flags Flags
	err = json.Unmarshal(fileContent, &flags)
	errors.Check(err)
	log.Printf("[INFO] %d flags read from %s", len(flags.Flags), params.JSONFilePath)

	return flags
}
