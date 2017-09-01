package com.sap.cloud.service.flags.demo.service;

/**
 * Represents the values that the feature flags status can take.
 */

public enum FeatureFlagStatus {

	ENABLED, DISABLED, MISSING;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
