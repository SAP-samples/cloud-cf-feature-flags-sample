package com.sap.cloud.service.flags.demo.service;

/**
 * Represents the flag.
 */

public class Flag {

	private FlagType type;
	private String variation;

	public Flag() {
	}

	public Flag(FlagType type, String variation) {
		this.type = type;
		this.variation = variation;
	}

	public void setType(FlagType type) {
		this.type = type;
	}

	public void setVariation(String variation) {
		this.variation = variation;
	}

	public FlagType getType() {
		return this.type;
	}

	public String getVariation() {
		return this.variation;
	}

	@Override
	public String toString() {
		return "Flag [type=" + type + ", variation=" + variation + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((variation == null) ? 0 : variation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Flag other = (Flag) obj;
		if (type != other.type)
			return false;
		if (variation == null) {
			if (other.variation != null)
				return false;
		} else if (!variation.equals(other.variation))
			return false;
		return true;
	}

}
