package com.sap.cloud.service.flags.demo.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;

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
	public void testGetFeatureFlag_ReturnsFeatureFlag_WhenRestOperationsReturnsHttpOk() {
		ResponseEntity<Flag> responseEntity = new ResponseEntity<Flag>(booleanTrueFlag, HttpStatus.OK);
		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenReturn(responseEntity);

		Flag actual = featureFlagsService.getFlag("feature-flag");
		assertEquals(booleanTrueFlag, actual);
	}

	@Test
	public void testGetFeatureFlag_ReturnsNull_WhenRestOperationsReturnsHttpNotFound() {
		HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND);
		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenThrow(exception);

		Flag actual = featureFlagsService.getFlag("feature-flag");
		assertEquals(null, actual);
	}

	@Test
	public void testGetFeatureFlag_RethrowsException_WhenExceptionStatusIsDifferentFromNotFound() {
		exceptionRule.expect(HttpStatusCodeException.class);
		exceptionRule.expectMessage("500 INTERNAL_SERVER_ERROR");

		HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenThrow(exception);

		featureFlagsService.getFlag("feature-flag");
	}
}
