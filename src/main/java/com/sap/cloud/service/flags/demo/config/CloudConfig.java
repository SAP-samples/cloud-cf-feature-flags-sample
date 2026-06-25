package com.sap.cloud.service.flags.demo.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;

/**
 * Represents a configuration class for creating {@link FeatureFlagsService}
 * instance for both cloud and local environment.
 */

@Configuration
@Profile({ "cloud", "default" })
public class CloudConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloudConfig.class);

	private static final String NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE = "There is no Feature Flags service instance bound to the application.";

	@Value("${VCAP_SERVICES:#{null}}")
	private String vcapServices;

	/**
	 * Creates a {@link FeatureFlagsService} for both cloud and local
	 * environment. If there is no Feature Flags instance bound to the
	 * application in the cloud, then {@code null} is returned.
	 *
	 * @return instance of {@link FeatureFlagsService}
	 */

	@Bean
	public FeatureFlagsService featureFlagsService() {
		try {
			if (vcapServices == null || vcapServices.isEmpty()) {
				LOGGER.warn(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE);
				return null;
			}

			Gson gson = new Gson();
			JsonObject vcapJson = gson.fromJson(vcapServices, JsonObject.class);

			if (!vcapJson.has("feature-flags")) {
				LOGGER.warn(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE);
				return null;
			}

			JsonArray featureFlagsServices = vcapJson.getAsJsonArray("feature-flags");
			if (featureFlagsServices.size() == 0) {
				LOGGER.warn(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE);
				return null;
			}

			JsonObject service = featureFlagsServices.get(0).getAsJsonObject();
			JsonObject credentials = service.getAsJsonObject("credentials");

			String uri = credentials.getAsJsonPrimitive("uri").getAsString();
			JsonObject x509 = credentials.getAsJsonObject("x509");

			String certUrl = x509.getAsJsonPrimitive("certurl").getAsString();
			String certificate = x509.getAsJsonPrimitive("certificate").getAsString();
			String key = x509.getAsJsonPrimitive("key").getAsString();
			String clientId = x509.getAsJsonPrimitive("clientid").getAsString();

			URI baseUri = createBaseUri(uri);
			OAuthTokenProvider tokenProvider = new OAuthTokenProvider(createTokenEndpoint(certUrl), certificate, key,
					clientId);
			RestOperations restOperations = createRestOperations(tokenProvider);

			return new FeatureFlagsService(baseUri, restOperations);
		} catch (Exception e) {
			LOGGER.error(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE, e);
			return null;
		}
	}

	private String createTokenEndpoint(String certUrl) {
		return certUrl.endsWith("/") ? certUrl + "oauth/token" : certUrl + "/oauth/token";
	}

	private URI createBaseUri(String serviceInfoUri) {
		try {
			URI basicAuthEncodedUri = URI.create(serviceInfoUri);
			return new URI(basicAuthEncodedUri.getScheme(), basicAuthEncodedUri.getHost(),
					basicAuthEncodedUri.getPath(), basicAuthEncodedUri.getFragment());
		} catch (URISyntaxException x) {
			throw new IllegalArgumentException(x.getMessage(), x);
		}
	}

	private RestOperations createRestOperations(OAuthTokenProvider tokenProvider) {
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestInterceptor oauthInterceptor = (request, body, execution) -> {
			request.getHeaders().setBearerAuth(tokenProvider.getAccessToken());
			return execution.execute(request, body);
		};
		restTemplate.getInterceptors().add(oauthInterceptor);
		return restTemplate;
	}
}
