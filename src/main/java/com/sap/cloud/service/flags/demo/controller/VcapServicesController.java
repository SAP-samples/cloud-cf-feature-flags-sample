package com.sap.cloud.service.flags.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Represents a controller that operates with the {@code VCAP_SERVICES}
 * environment variable.
 */

@RestController
@RequestMapping("vcap_services")
public class VcapServicesController {

	@Value("${VCAP_SERVICES:{}}")
	private String services;

	/**
	 * Returns the value of {@code VCAP_SERVICES} environment variable. If it's
	 * missing, an empty JSON is returned.
	 * 
	 * @return the value of {@code VCAP_SERVICES} environment variable
	 */

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String getVcapServices() {
		return services;
	}
}
