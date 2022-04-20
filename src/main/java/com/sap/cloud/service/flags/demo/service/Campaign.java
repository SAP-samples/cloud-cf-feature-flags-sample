package com.sap.cloud.service.flags.demo.service;

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
