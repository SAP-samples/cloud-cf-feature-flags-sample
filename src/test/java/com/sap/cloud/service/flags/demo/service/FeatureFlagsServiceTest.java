package com.sap.cloud.service.flags.demo.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@RunWith(SpringRunner.class)
public class FeatureFlagsServiceTest {

	private static final URI BASE_URI = URI.create("https://feature-flags.cfapps.region.hana.ondemand.com");
	private static final URI EVALUATION_URI = BASE_URI.resolve("/api/v2/evaluate/feature-flag");

	private FeatureFlagsService featureFlagsService;
	private Flag booleanTrueFlag;

	@MockBean
	private RestOperations restOperations;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Before
	public void setUp() {
		featureFlagsService = new FeatureFlagsService(BASE_URI, restOperations);
		booleanTrueFlag = new Flag(FlagType.BOOLEAN, "true");
	}

	@Test
	public void testGetFeatureFlag_ReturnsFeatureFlag_WithoutIdentifier() {
		ResponseEntity<Flag> responseEntity = new ResponseEntity<Flag>(booleanTrueFlag, HttpStatus.OK);
		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenReturn(responseEntity);

		Flag actual = featureFlagsService.getFlag("feature-flag", null);
		assertEquals(booleanTrueFlag, actual);
	}

	@Test
	public void testGetFeatureFlag_ReturnsFeatureFlag_WithEmptyIdentifier() {
		ResponseEntity<Flag> responseEntity = new ResponseEntity<Flag>(booleanTrueFlag, HttpStatus.OK);
		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenReturn(responseEntity);

		Flag actual = featureFlagsService.getFlag("feature-flag", "");
		assertEquals(booleanTrueFlag, actual);
	}

	@Test
	public void testGetFeatureFlag_ReturnsFeatureFlag_WithIdentifier() {
		URI serviceUri = UriComponentsBuilder.fromUri(EVALUATION_URI).queryParam("identifier", "my-identifier")
				.build().toUri();

		ResponseEntity<Flag> responseEntity = new ResponseEntity<Flag>(booleanTrueFlag, HttpStatus.OK);
		when(restOperations.getForEntity(serviceUri, Flag.class)).thenReturn(responseEntity);

		Flag actual = featureFlagsService.getFlag("feature-flag", "my-identifier");
		assertEquals(booleanTrueFlag, actual);
	}

	@Test
	public void testGetFeatureFlag_ReturnsNull_WhenHttpNotFound() {
		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

		Flag actual = featureFlagsService.getFlag("feature-flag", null);
		assertEquals(null, actual);
	}

	@Test
	public void testGetFeatureFlag_ThrowsEvaluationException_WhenHttpBadRequest() {
		exceptionRule.expect(EvaluationException.class);
		exceptionRule.expectMessage("Missing identifier");

		HttpStatus status = HttpStatus.BAD_REQUEST;
		HttpStatusCodeException exc = new HttpClientErrorException(status, status.getReasonPhrase(), "Missing identifier".getBytes(), StandardCharsets.UTF_8);
		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenThrow(exc);

		featureFlagsService.getFlag("feature-flag", null);
	}

	@Test
	public void testGetFeatureFlag_ThrowsEvaluationException_WhenHttpInternalServerError() {
		exceptionRule.expect(EvaluationException.class);
		exceptionRule.expectMessage("Feature Flags Service returned status 500.");

		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

		featureFlagsService.getFlag("feature-flag", null);
	}
}
