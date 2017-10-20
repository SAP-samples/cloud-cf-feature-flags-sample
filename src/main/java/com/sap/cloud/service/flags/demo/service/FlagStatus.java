package com.sap.cloud.service.flags.demo.service;

/**
 * Represents the flag status.
 */

public enum FlagStatus {

	ENABLED, DISABLED, MISSING;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
