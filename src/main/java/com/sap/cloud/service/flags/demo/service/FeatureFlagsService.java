package com.sap.cloud.service.flags.demo.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.LDValueType;
import com.launchdarkly.sdk.LDUser.Builder;
import com.launchdarkly.sdk.server.FeatureFlagsState;
import com.launchdarkly.sdk.server.LDClient;

/**
 * A service class for operation with Feature Flags service.
 */

public class FeatureFlagsService {

	private LDClient ldClient;

	/**
	 * Constructs a new {@link FeatureFlagsService} object with given {@link LDClient}.
	 *
	 * @param client
	 *            - LaunchDarkly SDK client {@link LDClient}
	 */

	public FeatureFlagsService(final LDClient client) {
		this.ldClient = client;
	}

	/**
	 * Gets the flag by given ID and identifier.
	 *
	 * @param id
	 *            - ID of the feature flag
	 * @param identifier
	 * 						- optional identifier for evaluating the feature flag
	 * @return the feature flag
	 */

	public Flag getFlag(final String id, final String identifier) throws EvaluationException {
		if (!ldClient.isFlagKnown(id)) {
			return null;
		}

		Builder builder = new LDUser.Builder("").anonymous(true);
		if (identifier != null && identifier.length() > 0) {
			builder.custom("identifier", identifier);
		}
		LDUser user = builder.build();

		FeatureFlagsState state = ldClient.allFlagsState(user);
		LDValue flag = state.getFlagValue(id);
		LDValueType type = flag.getType();

		if (type == LDValueType.BOOLEAN) {
			return new Flag(FlagType.BOOLEAN, String.valueOf(flag.booleanValue()));
		}

		if (type == LDValueType.STRING) {
			return new Flag(FlagType.STRING, flag.stringValue());
		}

		throw new EvaluationException(String.format("Cannot process flag with type %s", type.name()));
	}
}
