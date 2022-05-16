package com.sap.cloud.service.flags.demo.service;

/**
 * Represents the flag type.
 */

public enum FlagType {

	BOOLEAN, STRING;

	@Override
	public String toString() {
		return this.name();
	}
}
