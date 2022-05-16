package com.sap.cloud.service.flags.demo.controller;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.localconfig.LocalConfigConnector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.sap.cloud.service.flags.demo.service.Flag;
import com.sap.cloud.service.flags.demo.service.FlagType;
import com.sap.cloud.service.flags.demo.service.EvaluationException;
import com.sap.cloud.service.flags.demo.FeatureFlagsDemoApplication;
import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = FeatureFlagsDemoApplication.class)
public class DemoControllerTest {

	private static final String CAMPAIGN_ID = "campaign-1";
	private static final String FLAG_NAME = "flag-1";
	private static final String VISITOR_ID = "visitor-1";
	private static final String EVALUATION_URL = String.format("/evaluate?campaign=%s&flagName=%s&visitorId=%s",
			CAMPAIGN_ID, FLAG_NAME, VISITOR_ID);
	private static final String HOME_URL = "/";

	static {
		System.setProperty(LocalConfigConnector.PROPERTIES_FILE_PROPERTY, "src/test/resources/config.properties");
	}

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private FeatureFlagsService featureFlagsService;

	@SpyBean
	private DemoController demoController;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void testGetIndex_ReturnsIndexPage() throws Exception {
		String expectedResponse = "<title>Feature Flags Demo Application</title>";
		mockMvc.perform(get(HOME_URL)).andExpect(status().isOk())
				.andExpect(content().string(containsString(expectedResponse)));
	}

	@Test
	public void testEvaluate_WhenEvaluationFails() throws Exception {
		String mockExceptionMessage = "Evaluation failed";
		Exception mockException = new EvaluationException(mockExceptionMessage);
		when(featureFlagsService.getFlag(CAMPAIGN_ID, FLAG_NAME, VISITOR_ID)).thenThrow(mockException);
		mockMvc.perform(get(EVALUATION_URL)).andExpect(status().isOk()).andExpect(content().string(
				containsString(mockExceptionMessage)));
	}

	@Test
	public void testEvaluate_EvaluatesMissingFeatureFlag() throws Exception {
		when(featureFlagsService.getFlag(CAMPAIGN_ID, FLAG_NAME, VISITOR_ID)).thenReturn(null);
		String expectedResponse = String.format("Feature flag with name <strong>%s</strong> is <strong>missing</strong>", FLAG_NAME);
		mockMvc.perform(get(EVALUATION_URL)).andExpect(status().isOk()).andExpect(content().string(
				containsString(expectedResponse)));
	}


	@Test
	public void testEvaluate_EvaluatesBooleanFeatureFlag() throws Exception {
		Flag booleanFlag = new Flag(FlagType.BOOLEAN, true);
		when(featureFlagsService.getFlag(CAMPAIGN_ID, FLAG_NAME, VISITOR_ID)).thenReturn(booleanFlag);
		assertEvaluation(CAMPAIGN_ID, FLAG_NAME, FlagType.BOOLEAN, true);
	}

	@Test
	public void testEvaluate_EvaluatesStringFeatureFlag() throws Exception {
		Flag booleanFlag = new Flag(FlagType.STRING, "value");
		when(featureFlagsService.getFlag(CAMPAIGN_ID, FLAG_NAME, VISITOR_ID)).thenReturn(booleanFlag);
		assertEvaluation(CAMPAIGN_ID, FLAG_NAME, FlagType.STRING, "value");
	}

	@Test
	public void testEvaluate_MissingServiceInstance() throws Exception {
		when(demoController.hasBoundServiceInstance()).thenReturn(false);

		String expectedResponse = "There is no Feature Flags service instance bound to the application.";
		mockMvc.perform(get(EVALUATION_URL)).andExpect(status().isOk())
			.andExpect(content().string(containsString(expectedResponse)));
	}

	private void assertEvaluation(String campaign, String flagName, FlagType type, Object value) throws Exception {
		String url = String.format("/evaluate?campaign=%s&flagName=%s&visitorId=%s", campaign, flagName, VISITOR_ID);
		String expectedCampaign = String.format("Campaign <strong>%s</strong>.", campaign);
		String expectedFlagName = String.format("Feature flag with name <strong>%s</strong>.", flagName);
		String expectedType = String.format("Type is <strong>%s</strong>.", type);
		String expectedValue = String.format("Value is <strong>%s</strong>.", value);

		mockMvc.perform(get(url))
			.andExpect(status().isOk())
			.andExpect(content().string(allOf(
				containsString(expectedCampaign),
				containsString(expectedFlagName),
				containsString(expectedType),
				containsString(expectedValue)
			))
		);
	}
}
