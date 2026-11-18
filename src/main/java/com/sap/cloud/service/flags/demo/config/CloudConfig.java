package com.sap.cloud.service.flags.demo.config;

import java.net.URI;
import java.net.URISyntaxException;

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

	private static final String NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE = "There is no Feature Flags service instance bound to the application.";

	@Value("${VCAP_SERVICES:#{null}}")
	private String vcapServices;

	/**
	 * Creates a {@link FeatureFlagsService} for both cloud and local environment.
	 *
	 * @return instance of {@link FeatureFlagsService}
	 * @throws IllegalStateException if there is no Feature Flags service instance bound to the application
	 */

	@Bean
	public FeatureFlagsService featureFlagsService() {
		if (vcapServices == null || vcapServices.isEmpty()) {
			throw new IllegalStateException(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE);
		}

		try {
			Gson gson = new Gson();
			JsonObject vcapJson = gson.fromJson(vcapServices, JsonObject.class);

			if (!vcapJson.has("feature-flags")) {
				throw new IllegalStateException(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE);
			}

			JsonArray featureFlagsServices = vcapJson.getAsJsonArray("feature-flags");
			if (featureFlagsServices.size() == 0) {
				throw new IllegalStateException(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE);
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
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(NO_FEATURE_FLAGS_SERVICE_INSTANCE_FOUND_MESSAGE, e);
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
