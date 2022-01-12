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

	private static final String TAG_FEATURE_FLAGS_SERVICE = "feature-flags";
	private static final String TAG_USERNAME = "username";
	private static final String TAG_PASSWORD = "password";
	
	public FeatureFlagsServiceInfoCreator() {
		super(new Tags(TAG_FEATURE_FLAGS_SERVICE));
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public FeatureFlagsServiceInfo createServiceInfo(Map<String, Object> serviceData) {
		String id = getId(serviceData);
		Map<String, Object> credentials = getCredentials(serviceData);
		URI url = URI.create(getUriFromCredentials(credentials));
		String username = (String) credentials.get(TAG_USERNAME);
		String password = (String) credentials.get(TAG_PASSWORD);

		return new FeatureFlagsServiceInfo(id, url.getHost(), url.getPort(), username, password, url.getPath());
	}
}
