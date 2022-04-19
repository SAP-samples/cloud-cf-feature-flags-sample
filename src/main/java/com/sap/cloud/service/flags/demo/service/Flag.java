package com.sap.cloud.service.flags.demo.service;

/**
 * Represents the flag.
 */

public class Flag {

	private FlagType type;
	private Object value;

	public Flag(FlagType type, Object value) {
		this.type = type;
		this.value = value;
	}

	public void setType(FlagType type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public FlagType getType() {
		return this.type;
	}

	public Object getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "Flag [type=" + type + ", value=" + value + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
