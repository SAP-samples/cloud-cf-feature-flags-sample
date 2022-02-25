package launchdarkly

const (
	BaseURL                     = "https://app.launchdarkly.com"
	WeightsInstructionKind      = "updateFallthroughVariationOrRollout"
	TurnFlagOnInstructionKind   = "turnFlagOn"
	TurnFlagOffInstructionKind  = "turnFlagOff"
	ReplaceRulesInstructionKind = "replaceRules"
)

type Flag struct {
	Name        string           `json:"name"`
	Kind        string           `json:"kind"`
	Key         string           `json:"key"`
	Description string           `json:"description"`
	Temporary   bool             `json:"temporary"`
	Variations  []Variation      `json:"variations"`
	Defaults    DefaultVariation `json:"defaults"`

	Targets Targets `json:"-"`
}

func (f *Flag) GetVariationID(index int) string {
	return f.Variations[index].ID
}

type Clause struct {
	Attribute string   `json:"attribute"`
	Op        string   `json:"op"`
	Values    []string `json:"values"`
}

type Rule struct {
	VariationID string   `json:"variationId"`
	Clauses     []Clause `json:"clauses"`
}

type Instruction struct {
	Kind           string         `json:"kind"`
	Rules          []Rule         `json:"rules,omitempty"`
	RolloutWeights RolloutWeights `json:"rolloutWeights,omitempty"`
}

type RolloutWeights map[string]int

type Targets struct {
	Instructions []Instruction `json:"instructions"`
}

type Variation struct {
	ID    string      `json:"_id,omitempty"`
	Value interface{} `json:"value"`
}

type DefaultVariation struct {
	OffVariation int `json:"offVariation"`
	OnVariation  int `json:"onVariation"`
}
