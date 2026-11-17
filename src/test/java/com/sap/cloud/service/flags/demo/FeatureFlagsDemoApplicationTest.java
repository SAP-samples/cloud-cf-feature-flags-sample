package com.sap.cloud.service.flags.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;

@SpringBootTest(classes = FeatureFlagsDemoApplication.class)
public class FeatureFlagsDemoApplicationTest {

	@MockitoBean
	private FeatureFlagsService featureFlagsService;

	@Test
	public void testApplicationLoads() {
		// empty body
	}
}
