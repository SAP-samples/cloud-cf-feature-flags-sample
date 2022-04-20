package com.sap.cloud.service.flags.demo.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.cloud.service.flags.demo.config.FeatureFlagsServiceInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringRunner.class)
public class FeatureFlagsServiceTest {

    private static final String FFS_INSTANCE_ID = "123";
	private static final URI BASE_URI = URI.create("https://decision.flagship.io");
    private static final String ENV_KEY = "456";
    private static final String API_KEY = "789";
    private static final String CAMPAIGN_ID = "campaign-1";
	private static final URI EVALUATION_URI = BASE_URI.resolve(String.format("/v2/%s/campaigns/%s", ENV_KEY, CAMPAIGN_ID));
    private static final String API_KEY_HEADER = "x-api-key";
    private static final String VISITOR_ID_PROPERTY = "visitor_id";
    private static final String VISITOR_ID = "visitor-1";
    private static final String FLAG_NAME = "feature-flag";

	private FeatureFlagsService featureFlagsService;
	private Flag booleanTrueFlag;
    private Campaign campaignWithBoolFlag;
    private HttpEntity<Map<String, Object>> httpEntity;

	@MockBean
	private RestOperations restOperations;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Before
	public void setUp() {
        FeatureFlagsServiceInfo ffsInfo = new FeatureFlagsServiceInfo(FFS_INSTANCE_ID, BASE_URI.toString(), ENV_KEY, API_KEY);
		featureFlagsService = new FeatureFlagsService(ffsInfo, restOperations);
		booleanTrueFlag = new Flag(FlagType.BOOLEAN, "true");
        campaignWithBoolFlag = buildCampaign(booleanTrueFlag);

        httpEntity = buildHttpEntity();
	}

	@Test
	public void testGetFeatureFlag_ReturnsFeatureFlag() {
		URI serviceUri = UriComponentsBuilder.fromUri(EVALUATION_URI).build().toUri();

		ResponseEntity<Campaign> responseEntity = new ResponseEntity<>(campaignWithBoolFlag, HttpStatus.OK);
		when(restOperations.postForEntity(serviceUri, httpEntity, Campaign.class)).thenReturn(responseEntity);

		Flag actual = featureFlagsService.getFlag(CAMPAIGN_ID, FLAG_NAME, VISITOR_ID);
		assertEquals(booleanTrueFlag, actual);
	}

//	@Test
//	public void testGetFeatureFlag_ReturnsNull_WhenHttpNotFound() {
//		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
//
//		Flag actual = featureFlagsService.getFlag("feature-flag", null);
//		assertEquals(null, actual);
//	}

//	@Test
//	public void testGetFeatureFlag_ThrowsEvaluationException_WhenHttpBadRequest() {
//		exceptionRule.expect(EvaluationException.class);
//		exceptionRule.expectMessage("Missing identifier");
//
//		HttpStatus status = HttpStatus.BAD_REQUEST;
//		HttpStatusCodeException exc = new HttpClientErrorException(status, status.getReasonPhrase(), "Missing identifier".getBytes(), StandardCharsets.UTF_8);
//		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenThrow(exc);
//
//		featureFlagsService.getFlag("feature-flag", null);
//	}

//	@Test
//	public void testGetFeatureFlag_ThrowsEvaluationException_WhenHttpInternalServerError() {
//		exceptionRule.expect(EvaluationException.class);
//		exceptionRule.expectMessage("Feature Flags Service returned status 500.");
//
//		when(restOperations.getForEntity(EVALUATION_URI, Flag.class)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
//
//		featureFlagsService.getFlag("feature-flag", null);
//	}

    private Campaign buildCampaign(Flag flag) {
        Modifications mods = new Modifications(FLAG_NAME, flag.getValue());
        Variation variation = new Variation(mods);
        return new Campaign(variation);
    }

    private HttpEntity<Map<String, Object>> buildHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(API_KEY_HEADER, API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> map = new HashMap<>();
        map.put(VISITOR_ID_PROPERTY, VISITOR_ID);

        return new HttpEntity<>(map, headers);
    }
}
