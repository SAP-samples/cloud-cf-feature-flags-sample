package com.sap.cloud.service.flags.demo.config;

import java.util.Map;

import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

/**
 * Represents a {@link CloudFoundryServiceInfoCreator} for the Feature Flags
 * service.
 */

public class FeatureFlagsServiceInfoCreator extends CloudFoundryServiceInfoCreator<FeatureFlagsServiceInfo> {

	private static final String FEATURE_FLAGS_SERVICE_TAG = "flagship-flags-service";
	private static final String BASE_URI = "base-uri";
	private static final String ENV_KEY = "env-key";
	private static final String API_KEY = "api-key";

	public FeatureFlagsServiceInfoCreator() {
		super(new Tags(FEATURE_FLAGS_SERVICE_TAG));
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public FeatureFlagsServiceInfo createServiceInfo(Map<String, Object> serviceData) {
		String id = getId(serviceData);
		Map<String, Object> credentials = getCredentials(serviceData);
		String baseUri = (String) credentials.get(BASE_URI);
		String envKey = (String) credentials.get(ENV_KEY);
		String apiKey = (String) credentials.get(API_KEY);

		return new FeatureFlagsServiceInfo(id, baseUri, envKey, apiKey);
	}
}
