package com.sap.cloud.service.flags.demo.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.WebApplicationContext;

import com.sap.cloud.service.flags.demo.service.Flag;
import com.sap.cloud.service.flags.demo.service.FlagType;
import com.sap.cloud.service.flags.demo.FeatureFlagsDemoApplication;
import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = FeatureFlagsDemoApplication.class)
public class DemoControllerTest {

	static {
		System.setProperty(LocalConfigConnector.PROPERTIES_FILE_PROPERTY, "src/test/resources/config.properties");
	}

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private Flag booleanFlag;
	private Flag stringFlag;

	@MockBean
	private FeatureFlagsService featureFlagsService;

	@SpyBean
	private DemoController demoController;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		this.booleanFlag = new Flag(FlagType.BOOLEAN, "true");
		this.stringFlag = new Flag(FlagType.STRING, "variation-1");
	}

	@Test
	public void testGetIndex_ReturnsIndexPage() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(containsString("<title>Feature Flags Demo Application</title>")));
	}

	@Test
	public void testEvaluate_EvaluatesBooleanFeatureFlag_WithoutIdentifier() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", null)).thenReturn(booleanFlag);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString(
						"Feature flag with name <strong>feature-flag</strong> is <strong>BOOLEAN</strong>. Variation is <strong>true</strong>.")));
	}

	@Test
	public void testEvaluate_EvaluatesBooleanFeatureFlag_WithEmptyIdentifier() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", "")).thenReturn(booleanFlag);
		mockMvc.perform(get("/evaluate/feature-flag?identifier=")).andExpect(status().isOk()).andExpect(content().string(
				containsString(
						"Feature flag with name <strong>feature-flag</strong> is <strong>BOOLEAN</strong>. Variation is <strong>true</strong>.")));
	}

	@Test
	public void testEvaluate_EvaluatesBooleanFeatureFlag_WithIdentifier() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", "my-identifier")).thenReturn(booleanFlag);
		mockMvc.perform(get("/evaluate/feature-flag?identifier=my-identifier")).andExpect(status().isOk()).andExpect(content().string(
				containsString(
						"Feature flag with name <strong>feature-flag</strong> is <strong>BOOLEAN</strong>. Variation is <strong>true</strong>.")));
	}

	@Test
	public void testEvaluate_EvaluatesStringFeatureFlag() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", null)).thenReturn(stringFlag);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Feature flag with name <strong>feature-flag</strong> is <strong>STRING</strong>. Variation is <strong>variation-1</strong>.")));
	}

	@Test
	public void testEvaluate_EvaluatesMissingFeatureFlag() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", null)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Feature flag with name <strong>feature-flag</strong> is <strong>missing</strong>")));
	}

	@Test
	public void testEvaluate_EvaluatesFeatureFlag_WithoutRequiredIdentifier() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", null)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Status 400 returned by Service")));
	}

	@Test
	public void testEvaluate_WhenUnexpectedErrorOccurs() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", null)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Status 500 returned by Service")));
	}

	@Test
	public void testEvaluate_DetectsMissingServiceInstance() throws Exception {
		when(demoController.hasBoundServiceInstance()).thenReturn(false);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(
				content().string(containsString("There is no Feature Flags service instance bound to the application.")));
	}
}
