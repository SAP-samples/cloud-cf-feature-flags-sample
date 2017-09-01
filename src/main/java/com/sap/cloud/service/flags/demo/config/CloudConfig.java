package com.sap.cloud.service.flags.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;

/**
 * Represents a configuration class for creating {@link FeatureFlagsService}
 * instance for both cloud and local environment.
 */

@Configuration
@Profile({ "cloud", "default" })
public class CloudConfig extends AbstractCloudConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloudConfig.class);

	private static final String NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE = "There is no Feature Flags service instance bound to the application.";

	/**
	 * Creates a {@link FeatureFlagsService} for both cloud and local
	 * environment. If there is no Feature Flags instance bound to the
	 * application in the cloud, then {@code null} is returned.
	 * 
	 * @return instance of {@link FeatureFlagsService}
	 */

	@Bean
	public FeatureFlagsService featureFlagsService() {
		try {
			return connectionFactory().service(FeatureFlagsService.class);
		} catch (CloudException e) {
			LOGGER.error(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE, e);
			return null;
		}
	}
}
