package com.sap.cloud.service.flags.demo.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sap.cloud.service.flags.demo.service.EvaluationException;
import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;
import com.sap.cloud.service.flags.demo.service.Flag;

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
	 * Depending the feature flag status and appropriate message is being
	 * displayed.
	 *
	 * @param campaign
	 *            - name of the Use Case (slug) from the Flagship dashboard, used as campaign in the Decision API.
	 * @param flagName
	 * 						- name of the flag from the campaign to retrieve.
	 * @param visitorId
	 * 						- unique identifier of the current visitor of the demo application.
	 * @param modelMap
	 *            - the {@link ModelMap}
	 *
	 * @return the evaluation view
	 */

	@GetMapping("/evaluate")
	public String evaluate(@RequestParam("campaign") final String campaign, @RequestParam("flagName") final String flagName,
			@RequestParam("visitorId") final String visitorId, final ModelMap modelMap) {

		String template = "evaluation";

		boolean credentialsAvailable = hasBoundServiceInstance();
		modelMap.addAttribute("credentialsAvailable", credentialsAvailable);

		if (!credentialsAvailable) {
			return template;
		}

		modelMap.addAttribute("campaign", campaign);
		modelMap.addAttribute("flagName", flagName);

		Flag flag;
		try {
			flag = featureFlagsServiceOptional.get().getFlag(campaign, flagName, visitorId);
		} catch (EvaluationException e) {
			modelMap.addAttribute("backendError", e.getMessage());
			return template;
		}

		boolean flagAvailable = flag != null;
		modelMap.addAttribute("flagAvailable", flagAvailable);
		if (flagAvailable) {
			modelMap.addAttribute("type", flag.getType());
			modelMap.addAttribute("value", flag.getValue());
		}

		return template;
	}

	protected boolean hasBoundServiceInstance() {
		return featureFlagsServiceOptional.isPresent();
	}
}
