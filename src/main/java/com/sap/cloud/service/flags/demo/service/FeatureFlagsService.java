package com.sap.cloud.service.flags.demo.service;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A service class for operation with Feature Flags service.
 */

public class FeatureFlagsService {

	private static final Logger logger = LoggerFactory.getLogger(FeatureFlagsService.class);

	private static final String UNEXPECTED_STATUS_ERROR_FORMAT = "Status {} returned by server";

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
	 * Gets the flag by given ID.
	 *
	 * @param id
	 *            - ID of the feature flag
	 * @return the feature flag
	 */

	public Flag getFlag(final String id, final String identifier) {
		// @formatter:off
		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUri).path("/api/v2/evaluate/{id}");

		if (identifier != null && identifier.length() > 0) {
			urlBuilder.queryParam("identifier", identifier);
		}

		URI url = urlBuilder.buildAndExpand(id).toUri();
		// @formatter:on

		ResponseEntity<Flag> responseEntity = restOperations.getForEntity(url, Flag.class);
		return responseEntity.getBody();
	}
}
