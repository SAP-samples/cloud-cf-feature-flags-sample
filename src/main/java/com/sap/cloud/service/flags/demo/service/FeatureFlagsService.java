package com.sap.cloud.service.flags.demo.service;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A service class for operation with Feature Flags service.
 */

public class FeatureFlagsService {

	private static final Logger logger = LoggerFactory.getLogger(FeatureFlagsService.class);
	
	private static final String ERROR_EVALUATION_MESSAGE = "An exception occurred during evaluation of a feature flag.";
	
	private URI baseUri;
	private RestOperations restOperations;

	/**
	 * Constructs a new {@link QuotaCheck} object with give base URI and
	 * {@link RestOperations}.
	 * 
	 * @param baseUri
	 *            - the base URI of Feature Flags service <i>(e.g.
	 *            https://feature-flags.cfapps.us10.hana.ondemand.com/)</i>
	 * @param restOperations
	 *            - a configured {@link RestOperation} with Basic authentication
	 *            for communication with Feature Flags service
	 */

	public FeatureFlagsService(final URI baseUri, final RestOperations restOperations) {
		this.baseUri = baseUri;
		this.restOperations = restOperations;
	}

	/**
	 * Gets the status of a feature flag by given ID.
	 * 
	 * @param id
	 *            - ID of the feature flag
	 * @return the feature flag status
	 */

	public FeatureFlagStatus getFeatureFlagStatus(final String id) {
		// @formatter:off
		URI url = UriComponentsBuilder
					.fromUri(baseUri)
					.path("/api/v1/evaluate/{id}")
					.buildAndExpand(id).toUri();
		// @formatter:on

		FeatureFlagStatus status = FeatureFlagStatus.DISABLED;
		try {
			ResponseEntity<?> responseEntity = restOperations.getForEntity(url, Object.class);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				status = FeatureFlagStatus.ENABLED;
			}
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
				logger.error(ERROR_EVALUATION_MESSAGE, e);
				status = FeatureFlagStatus.DISABLED;
			} else {
				status = FeatureFlagStatus.MISSING;				
			}
		}

		return status;
	}
}
