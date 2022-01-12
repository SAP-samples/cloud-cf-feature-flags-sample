package com.sap.cloud.service.flags.demo.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.cloud.service.AbstractServiceConnectorCreator;
import org.springframework.cloud.service.ServiceConnectorConfig;
import org.springframework.cloud.service.ServiceConnectorCreator;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.service.flags.demo.service.FeatureFlagsService;

/**
 * Represents a {@link ServiceConnectorCreator} for the Feature Flags service.
 */

public class FeatureFlagsServiceConnectorCreator
		extends AbstractServiceConnectorCreator<FeatureFlagsService, FeatureFlagsServiceInfo> {

	/**
	 * Creates a {@link FeatureFlagsService} by given
	 * {@link FeatureFlagsServiceInfo} and {@link ServiceConnectorConfig}.
	 */

	@Override
	public FeatureFlagsService create(FeatureFlagsServiceInfo serviceInfo,
			ServiceConnectorConfig serviceConnectorConfig) {
		URI baseUri = createBaseUri(serviceInfo.getUri());
		RestOperations restOperations = createRestOperations(serviceInfo.getUserName(), serviceInfo.getPassword());

		return new FeatureFlagsService(baseUri, restOperations);
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

	private RestOperations createRestOperations(String username, String password) {
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestInterceptor basicAuthInterceptor = new BasicAuthenticationInterceptor(username, password);
		restTemplate.getInterceptors().add(basicAuthInterceptor);

		return restTemplate;
	}
}
