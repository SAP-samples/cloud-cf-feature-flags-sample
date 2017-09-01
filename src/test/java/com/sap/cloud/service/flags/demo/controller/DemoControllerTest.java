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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.sap.cloud.service.flags.demo.FeatureFlagsDemoApplication;
import com.sap.cloud.service.flags.demo.service.FeatureFlagStatus;
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
		mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(containsString("<title>Feature Flags Demo Application</title>")));
	}

	@Test
	public void testEvaluate_EvaluatesEnabledFeatureFlag() throws Exception {
		when(featureFlagsService.getFeatureFlagStatus("feature-flag")).thenReturn(FeatureFlagStatus.ENABLED);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Feature flag with name <strong>feature-flag</strong> is <strong>enabled</strong>")));
	}

	@Test
	public void testEvaluate_EvaluatesDisabledFeatureFlag() throws Exception {
		when(featureFlagsService.getFeatureFlagStatus("feature-flag")).thenReturn(FeatureFlagStatus.DISABLED);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Feature flag with name <strong>feature-flag</strong> is <strong>disabled</strong>")));
	}

	@Test
	public void testEvaluate_EvaluatesMissingFeatureFlag() throws Exception {
		when(featureFlagsService.getFeatureFlagStatus("feature-flag")).thenReturn(FeatureFlagStatus.MISSING);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(content().string(
				containsString("Feature flag with name <strong>feature-flag</strong> is <strong>missing</strong>")));
	}

	@Test
	public void testEvaluate_DetectsMissingServiceInstance() throws Exception {
		when(demoController.hasBoundServiceInstance()).thenReturn(false);
		mockMvc.perform(get("/evaluate/feature-flag")).andExpect(status().isOk()).andExpect(
				content().string(containsString("There is no Feature Flags service instance bound to the application.")));
	}
}
