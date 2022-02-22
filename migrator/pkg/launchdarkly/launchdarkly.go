package launchdarkly

const BaseURL = "https://app.launchdarkly.com"

type Flag struct {
	Name        string           `json:"name"`
	Kind        string           `json:"kind"`
	Key         string           `json:"key"`
	Description string           `json:"description"`
	Temporary   bool             `json:"temporary"`
	Variations  []Variation      `json:"variations"`
	Defaults    DefaultVariation `json:"defaults"`
}

type Variation struct {
	Value interface{} `json:"value"`
}

type DefaultVariation struct {
	OffVariation int `json:"offVariation"`
	OnVariation  int `json:"onVariation"`
}
