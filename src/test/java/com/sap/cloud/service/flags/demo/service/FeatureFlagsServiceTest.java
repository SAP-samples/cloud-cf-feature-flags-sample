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
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

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
}
