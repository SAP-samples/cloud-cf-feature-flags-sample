package com.sap.cloud.service.flags.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.sap.cloud.service.flags.demo.FeatureFlagsDemoApplication;

@SpringBootTest(classes = FeatureFlagsDemoApplication.class)
@AutoConfigureMockMvc
public class VcapServicesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testGetVcapServices_ReturnsEmptyJson_WhenEnvVariableIsMissing() throws Exception {
		mockMvc.perform(get("/vcap_services")).andExpect(status().isOk()).andExpect(content().json("{}"));
	}
}
