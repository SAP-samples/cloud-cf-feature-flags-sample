package com.sap.cloud.service.flags.demo.config;

import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.BaseServiceInfo;
import org.springframework.util.Assert;

/**
 * Represents a {@link ServiceInfo} for the Feature Flags service.
 */

public class FeatureFlagsServiceInfo extends BaseServiceInfo {

	public static final String HTTPS_SCHEME = "https";

	private String sdkKey;

	public FeatureFlagsServiceInfo(String id, String sdkKey) {
		super(id);
		Assert.notNull(sdkKey);
		this.sdkKey = sdkKey;
	}

	public String getSdkKey() {
		return this.sdkKey;
	}
}
