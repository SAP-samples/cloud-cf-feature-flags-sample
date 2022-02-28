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
		mockMvc.perform(get("/evaluate/feature-flag"))
				.andExpect(status().isOk())
				.andExpect(content().string(
						allOf(containsString("Feature flag with name <strong>feature-flag</strong>."),
								containsString("Type is <strong>BOOLEAN</strong>."),
								containsString("Variation is <strong>true</strong>."))
				));
	}

	@Test
	public void testEvaluate_EvaluatesBooleanFeatureFlag_WithEmptyIdentifier() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", "")).thenReturn(booleanFlag);
		mockMvc.perform(get("/evaluate/feature-flag?identifier="))
				.andExpect(status().isOk())
				.andExpect(content().string(
						allOf(containsString("Feature flag with name <strong>feature-flag</strong>."),
								containsString("Type is <strong>BOOLEAN</strong>."),
								containsString("Variation is <strong>true</strong>."))
				));
	}

	@Test
	public void testEvaluate_EvaluatesBooleanFeatureFlag_WithIdentifier() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", "my-identifier")).thenReturn(booleanFlag);
		mockMvc.perform(get("/evaluate/feature-flag?identifier=my-identifier"))
		.andExpect(status().isOk())
		.andExpect(content().string(
				allOf(containsString("Feature flag with name <strong>feature-flag</strong>."),
						containsString("Type is <strong>BOOLEAN</strong>."),
						containsString("Variation is <strong>true</strong>."))
		));
	}

	@Test
	public void testEvaluate_EvaluatesStringFeatureFlag() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", null)).thenReturn(stringFlag);
		mockMvc.perform(get("/evaluate/feature-flag"))
		.andExpect(status().isOk())
		.andExpect(content().string(
				allOf(containsString("Feature flag with name <strong>feature-flag</strong>."),
						containsString("Type is <strong>STRING</strong>."),
						containsString("Variation is <strong>variation-1</strong>."))
				));
	}

	@Test
	public void testEvaluate_EvaluatesMissingFeatureFlag() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", null)).thenReturn(null);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Feature flag with name <strong>feature-flag</strong> is <strong>missing</strong>")));
	}

	@Test
	public void testEvaluate_WhenEvaluationFails() throws Exception {
		when(featureFlagsService.getFlag("feature-flag", null)).thenThrow(new EvaluationException("Evaluation failed"));
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Evaluation failed")));
	}

	@Test
	public void testEvaluate_DetectsMissingServiceInstance() throws Exception {
		when(demoController.hasBoundServiceInstance()).thenReturn(false);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(
				content().string(containsString("There is no Feature Flags service instance bound to the application.")));
	}
}
