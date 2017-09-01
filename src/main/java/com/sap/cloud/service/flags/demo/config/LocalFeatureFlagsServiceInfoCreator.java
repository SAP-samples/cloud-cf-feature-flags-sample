package com.sap.cloud.service.flags.demo.config;

import org.springframework.cloud.localconfig.LocalConfigServiceInfoCreator;

/**
 * Represents a {@link LocalConfigServiceInfoCreator} for the Feature Flags
 * service.
 */

public class LocalFeatureFlagsServiceInfoCreator extends LocalConfigServiceInfoCreator<FeatureFlagsServiceInfo> {

	public LocalFeatureFlagsServiceInfoCreator() {
		super(FeatureFlagsServiceInfo.HTTPS_SCHEME);
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public FeatureFlagsServiceInfo createServiceInfo(String id, String uri) {
		return new FeatureFlagsServiceInfo(id, uri);
	}
}
