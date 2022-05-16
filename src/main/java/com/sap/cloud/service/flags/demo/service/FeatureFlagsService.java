package com.sap.cloud.service.flags.demo.service;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.cloud.service.flags.demo.config.FeatureFlagsServiceInfo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A service class for operation with Feature Flags service.
 */

public class FeatureFlagsService {

	private static final String API_KEY_HEADER = "x-api-key";
	private static final String VISITOR_ID_PROPERTY =	"visitor_id";

	private URI baseUri;
	private FeatureFlagsServiceInfo info;
	private RestOperations restOperations;

	/**
	 * Constructs a new {@link FeatureFlagsService} object with given {@link FeatureFlagsServiceInfo} and
	 * {@link RestOperations}.
	 *
	 * @param info
	 *            - object containing information on how to connect to the service.
	 * @param restOperations
	 *            - a configured {@link RestOperation} object
	 *            for communication with the Feature Flags service.
	 */

	public FeatureFlagsService(final FeatureFlagsServiceInfo info, final RestOperations restOperations) {
		this.baseUri = URI.create(info.getUri());
		this.info = info;
		this.restOperations = restOperations;
	}

	/**
	 * Gets the flag by given campaign, flag name and visitor id.
	 *
	 * @param campaignId
	 *            - name of the Use Case (slug) from the Flagship dashboard, used as campaign in the Decision API.
	 * @param flagName
	 * 						- name of the flag from the campaign to retrieve.
	 * @param visitorId
	 * 						- unique identifier of the current visitor of the demo application.
	 *
	 * @return the feature flag
	 */

	public Flag getFlag(final String campaignId, final String flagName, final String visitorId) throws EvaluationException {
		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUri).path("/v2/{envId}/campaigns/{campaignId}");
		URI url = urlBuilder.buildAndExpand(info.getEnvId(), campaignId).toUri();
		HttpEntity<Map<String, Object>> requestEntity = prepareRequestEntity(info.getApiKey(), visitorId);

		try {
			ResponseEntity<Campaign> responseEntity = restOperations.postForEntity(url, requestEntity, Campaign.class);
			Campaign campaign = responseEntity.getBody();
			Modifications modifications = campaign.getVariation().getModifications();

			if (!modifications.haveFlag(flagName)) {
				return null;
			}

			return new Flag(modifications.getFlagType(flagName), modifications.getFlagValue(flagName));
		} catch (HttpStatusCodeException e) {
			if (e.getStatusCode() == HttpStatus.NO_CONTENT) {
				String message = String.format("The current visitor '%s' is not being targeted by any variation/scenario", visitorId);
				throw new EvaluationException(message);
			}

			if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
				throw new EvaluationException(e.getResponseBodyAsString());
			}

			String message = String.format("Flagship returned status %d.", e.getStatusCode().value());
			throw new EvaluationException(message);
		}
	}

	private HttpEntity<Map<String, Object>> prepareRequestEntity(String apiKey, String visitorId) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(API_KEY_HEADER, apiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		Map<String, Object> map = new HashMap<>();
		map.put(VISITOR_ID_PROPERTY, visitorId);

		return new HttpEntity<>(map, headers);
	}
}
