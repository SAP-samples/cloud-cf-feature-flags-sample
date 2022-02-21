package com.sap.cloud.service.flags.demo.service;

import static org.junit.Assert.assertEquals;

import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.UserAttribute;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.integrations.TestData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class FeatureFlagsServiceTest {

	private TestData testData;
	private FeatureFlagsService featureFlagsService;
	private Flag booleanTrueFlag;
	private Flag booleanFalseFlag;
	private Flag stringFlag;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();;

	@Before
	public void setUp() {
		testData = TestData.dataSource();
		LDClient client = new LDClient("sdk-key", new LDConfig.Builder().dataSource(testData).build());
		featureFlagsService = new FeatureFlagsService(client);
		booleanTrueFlag = new Flag(FlagType.BOOLEAN, "true");
		booleanFalseFlag = new Flag(FlagType.BOOLEAN, "false");
		stringFlag = new Flag(FlagType.STRING, "variation-1");
	}

	@Test
	public void testGetFeatureFlag_ReturnsNull_WhenFlagIsNotDefined() {
		Flag actual = featureFlagsService.getFlag("feature-flag", null);
		assertEquals(null, actual);
	}

	@Test
	public void testGetFeatureFlag_ReturnsFeatureFlag_WithoutIdentifier() {
		testData.update(testData.flag("feature-flag")
			  .ifMatch(UserAttribute.ANONYMOUS)
			  .thenReturn(true));

		Flag actual = featureFlagsService.getFlag("feature-flag", null);
		assertEquals(booleanTrueFlag, actual);
	}

	@Test
	public void testGetFeatureFlag_ReturnsFeatureFlag_WithEmptyIdentifier() {
		testData.update(testData.flag("feature-flag")
			  .ifMatch(UserAttribute.ANONYMOUS)
			  .thenReturn(true));

		Flag actual = featureFlagsService.getFlag("feature-flag", "");
		assertEquals(booleanTrueFlag, actual);
	}

	@Test
	public void testGetFeatureFlag_ReturnsFeatureFlag_WithIdentifier() {
		testData.update(testData.flag("feature-flag")
			  .ifMatch(UserAttribute.forName("identifier"), LDValue.of("my-identifier"))
			  .thenReturn(false));

		Flag actual = featureFlagsService.getFlag("feature-flag", "my-identifier");
		assertEquals(booleanFalseFlag, actual);

		actual = featureFlagsService.getFlag("feature-flag", "some-other-identifier");
		assertEquals(booleanTrueFlag, actual);
	}

	@Test
	public void testGetFeatureFlag_ReturnsStringFeatureFlag() {
		testData.update(testData.flag("feature-flag")
				.variations(LDValue.of("variation-1"), LDValue.of("variation-0"), LDValue.of("variation-2"))
				.ifMatch(UserAttribute.ANONYMOUS)
			  .thenReturn(1));

		Flag actual = featureFlagsService.getFlag("feature-flag", null);
		assertEquals(stringFlag, actual);
	}

	@Test
	public void testGetFeatureFlag_Throws_WhenFlagIsOfDifferentType() {
		exceptionRule.expect(EvaluationException.class);
		exceptionRule.expectMessage("Cannot process flag with type NUMBER");
		testData.update(testData.flag("feature-flag").valueForAllUsers(LDValue.of(12.3)));

		featureFlagsService.getFlag("feature-flag", null);
	}

}
