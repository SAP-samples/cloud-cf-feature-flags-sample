package com.sap.cloud.service.flags.demo.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;

/**
 * Represents a controller that uses the feature toggling practice.
 */

@Controller
public class DemoController {

	private Optional<FeatureFlagsService> featureFlagsServiceOptional;

	/**
	 * Constructs a new {@link DemoController} with given
	 * {@link FeatureFlagsService}.
	 * 
	 * @param featureFlagsServiceOptional
	 */

	public DemoController(final Optional<FeatureFlagsService> featureFlagsServiceOptional) {
		this.featureFlagsServiceOptional = featureFlagsServiceOptional;
	}

	/**
	 * Returns the homepage view.
	 * 
	 * @param modelMap
	 *            - the {@link ModelMap}
	 * 
	 * @return the homepage view
	 */

	@GetMapping("/")
	public String getIndex(final ModelMap modelMap) {
		return "index";
	}

	/**
	 * Returns the evaluation view performing the real feature toggling.
	 * Depending the feature flag status an appropriate message is being
	 * displayed.
	 * 
	 * @param id
	 *            - ID of the feature flag
	 * @param modelMap
	 *            - the {@link ModelMap}
	 * 
	 * @return the evaluation view
	 */

	@GetMapping("/evaluate/{id}")
	public String evaluate(@PathVariable final String id, final ModelMap modelMap) {
		String status;
		if (!hasBoundServiceInstance()) {
			status = "missing-service-instance";
		} else {
			status = featureFlagsServiceOptional.get().getFeatureFlagStatus(id).toString();
		}

		modelMap.addAttribute("status", status);

		return "evaluation";
	}

	protected boolean hasBoundServiceInstance() {
		return featureFlagsServiceOptional.isPresent();
	}
}
