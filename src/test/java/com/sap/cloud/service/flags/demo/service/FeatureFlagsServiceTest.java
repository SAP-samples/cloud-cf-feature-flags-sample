package com.sap.cloud.service.flags.demo.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;

@RunWith(SpringRunner.class)
public class FeatureFlagsServiceTest {

	private static final URI BASE_URI = URI.create("https://feature-flags.cfapps.region.hana.ondemand.com");
	private static final URI EVALUATION_URI = BASE_URI.resolve("/api/v1/evaluate/feature-flag");

	private FeatureFlagsService featureFlagsService;

	@MockBean
	private RestOperations restOperations;

	@Before
	public void setUp() {
		featureFlagsService = new FeatureFlagsService(BASE_URI, restOperations);
	}

	@Test
	public void testGetFeatureFlagStatus_ReturnsEnabled_WhenRestOperationsReturnsHttpOk() {
		ResponseEntity<Object> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restOperations.getForEntity(EVALUATION_URI, Object.class)).thenReturn(responseEntity);

		FlagStatus actual = featureFlagsService.getFlagStatus("feature-flag");
		assertEquals(FlagStatus.ENABLED, actual);
	}

	@Test
	public void testGetFeatureFlagsStatus_ReturnsDisabledState_WhenRestOperationsReturnsHttpNoContent() {
		ResponseEntity<Object> responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
		when(restOperations.getForEntity(EVALUATION_URI, Object.class)).thenReturn(responseEntity);

		FlagStatus actual = featureFlagsService.getFlagStatus("feature-flag");
		assertEquals(FlagStatus.DISABLED, actual);
	}

	@Test
	public void testGetFeatureFlagsStatus_ReturnsMissingState_WhenRestOperationsReturnsHttpNotFound() {
		HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND);
		when(restOperations.getForEntity(EVALUATION_URI, Object.class)).thenThrow(exception);

		FlagStatus actual = featureFlagsService.getFlagStatus("feature-flag");
		assertEquals(FlagStatus.MISSING, actual);
	}

	@Test
	public void testGetFeatureFlagsState_RethrowsException_WhenExceptionStatusIsDifferentFromNotFound() {
		HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
		when(restOperations.getForEntity(EVALUATION_URI, Object.class)).thenThrow(exception);

		FlagStatus actual = featureFlagsService.getFlagStatus("feature-flag");
		assertEquals(FlagStatus.DISABLED, actual);
	}
}
