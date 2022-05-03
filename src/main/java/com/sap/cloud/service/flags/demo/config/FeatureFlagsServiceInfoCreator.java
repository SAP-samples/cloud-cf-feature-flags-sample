package com.sap.cloud.service.flags.demo.config;

import java.util.Map;

import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

/**
 * Represents a {@link CloudFoundryServiceInfoCreator} for the Feature Flags
 * service.
 */

public class FeatureFlagsServiceInfoCreator extends CloudFoundryServiceInfoCreator<FeatureFlagsServiceInfo> {

	private static final String FEATURE_FLAGS_SERVICE_TAG = "launchdarkly-flags-service";
	private static final String SDK_KEY = "sdk-key";

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
		String sdkKey = (String) credentials.get(SDK_KEY);

		return new FeatureFlagsServiceInfo(id, sdkKey);
	}
}
