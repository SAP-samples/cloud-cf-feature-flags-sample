package com.sap.cloud.service.flags.demo.service;

/**
 * Class used for representing the JSON response from the Flagship's Decision API
 * for a single Use Case (campaign).
 */

public class Campaign {

	private Variation variation;

	public Campaign() {
		// required for JSON encode/decode
	}

	public Campaign(Variation variation) {
		this.variation = variation;
	}

	public Variation getVariation() {
		return variation;
	}

}
