package com.sap.cloud.service.flags.demo.service;

/**
 * Class used for representing the 'variation' property in a campaign JSON response.
 */

public class Variation {
	private Modifications modifications;

	public Variation() {
		// required for JSON encode/decode
	}

	public Variation(Modifications modifications) {
		this.modifications = modifications;
	}

	public Modifications getModifications() {
		return modifications;
	}
}
