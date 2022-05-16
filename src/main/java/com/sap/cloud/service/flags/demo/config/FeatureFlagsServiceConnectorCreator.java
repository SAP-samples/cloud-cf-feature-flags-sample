package com.sap.cloud.service.flags.demo.config;

import org.springframework.cloud.service.AbstractServiceConnectorCreator;
import org.springframework.cloud.service.ServiceConnectorConfig;
import org.springframework.cloud.service.ServiceConnectorCreator;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;

/**
 * Represents a {@link ServiceConnectorCreator} for the Feature Flags service.
 */

public class FeatureFlagsServiceConnectorCreator
		extends AbstractServiceConnectorCreator<FeatureFlagsService, FeatureFlagsServiceInfo> {

	/**
	 * Creates a {@link FeatureFlagsService} by given
	 * {@link FeatureFlagsServiceInfo} and {@link ServiceConnectorConfig}.
	 */

	@Override
	public FeatureFlagsService create(FeatureFlagsServiceInfo serviceInfo,
			ServiceConnectorConfig serviceConnectorConfig) {
		return new FeatureFlagsService(serviceInfo, new RestTemplate());
	}
}
