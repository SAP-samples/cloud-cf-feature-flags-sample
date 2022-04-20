package com.sap.cloud.service.flags.demo.service;

import java.util.HashMap;
import java.util.Map;

public class Modifications {
	private Map<String, Object> value;

	public Modifications() {
		// required for JSON encode/decode
	}

	public Modifications(String key, Object value) {
		this.value = new HashMap<>();
		this.value.put(key, value);
	}

	public Map<String, Object> getValue() {
		return value;
	}

	public boolean haveFlag(String flagName) {
		return value.containsKey(flagName);
	}

	public FlagType getFlagType(String flagName) {
		Object flagValue = value.get(flagName);
		if (flagValue instanceof Boolean) {
			return FlagType.BOOLEAN;
		}
		if (flagValue instanceof String) {
			return FlagType.STRING;
		}
		throw new IllegalStateException(String.format("Flag with name '%s' is of type '%s'", flagName, flagValue.getClass().getName()));
	}

	public Object getFlagValue(String flagName) {
		return value.get(flagName);
	}
}
