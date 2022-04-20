package com.sap.cloud.service.flags.demo.config;

import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.UriBasedServiceInfo;
import org.springframework.util.Assert;

/**
 * Represents a {@link ServiceInfo} for the Feature Flags service.
 */

public class FeatureFlagsServiceInfo extends UriBasedServiceInfo {

	public static final String HTTPS_SCHEME = "https";

	private final String envId;
	private final String apiKey;

	public FeatureFlagsServiceInfo(String id, String baseUri, String envId, String apiKey) {
		super(id, baseUri);

		Assert.notNull(envId);
		this.envId = envId;

		Assert.notNull(apiKey);
		this.apiKey = apiKey;
	}

	public String getEnvId() {
		return this.envId;
	}

	public String getApiKey() {
		return this.apiKey;
	}

}
