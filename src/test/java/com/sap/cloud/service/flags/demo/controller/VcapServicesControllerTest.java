package com.sap.cloud.service.flags.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.sap.cloud.service.flags.demo.FeatureFlagsDemoApplication;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = FeatureFlagsDemoApplication.class)
public class VcapServicesControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void testGetVcapServices_ReturnsEmptyJson_WhenEnvVariableIsMissing() throws Exception {
		mockMvc.perform(get("/vcap_services")).andExpect(status().isOk()).andExpect(content().json("{}"));
	}
}
