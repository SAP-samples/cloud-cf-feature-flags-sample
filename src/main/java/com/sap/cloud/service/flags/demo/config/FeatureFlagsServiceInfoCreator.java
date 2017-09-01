package com.sap.cloud.service.flags.demo.config;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

/**
 * Represents a {@link CloudFoundryServiceInfoCreator} for the Feature Flags
 * service.
 */

public class FeatureFlagsServiceInfoCreator extends CloudFoundryServiceInfoCreator<FeatureFlagsServiceInfo> {

	private static final String FEATURE_FLAGS_SERVICE_TAG = "feature-flags";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	
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
		URI url = URI.create(getUriFromCredentials(credentials));
		String username = (String) credentials.get(USERNAME);
		String password = (String) credentials.get(PASSWORD);

		return new FeatureFlagsServiceInfo(id, url.getHost(), url.getPort(), username, password, url.getPath());
	}
}
