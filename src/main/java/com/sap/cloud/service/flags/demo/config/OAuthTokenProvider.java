package com.sap.cloud.service.flags.demo.config;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.pem.PemSslStoreBundle;
import org.springframework.boot.ssl.pem.PemSslStoreDetails;

public final class OAuthTokenProvider {

	private static final long TOKEN_REFRESH_SKEW_SECONDS = 30L;

	private final String tokenEndpoint;
	private final String certificatePem;
	private final String privateKeyPem;
	private final String clientId;

	private volatile AccessToken cachedToken;

	public OAuthTokenProvider(String tokenEndpoint, String certificatePem, String privateKeyPem, String clientId) {
		this.tokenEndpoint = tokenEndpoint;
		this.certificatePem = certificatePem;
		this.privateKeyPem = privateKeyPem;
		this.clientId = clientId;
	}

	public synchronized String getAccessToken() {
		if (cachedToken != null && Instant.now().isBefore(cachedToken.validUntil())) {
			return cachedToken.value();
		}

		cachedToken = requestAccessToken();
		return cachedToken.value();
	}

	private AccessToken requestAccessToken() {
		try {
			SSLContext sslContext = createMutualTlsContext(certificatePem, privateKeyPem);
			URL tokenUrl = URI.create(tokenEndpoint).toURL();
			HttpsURLConnection connection = (HttpsURLConnection) tokenUrl.openConnection();
			connection.setSSLSocketFactory(sslContext.getSocketFactory());
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8);
			String body = "grant_type=client_credentials&client_id=" + encodedClientId;
			byte[] payload = body.getBytes(StandardCharsets.UTF_8);
			connection.setFixedLengthStreamingMode(payload.length);

			try (OutputStream outputStream = connection.getOutputStream()) {
				outputStream.write(payload);
			}

			int statusCode = connection.getResponseCode();
			InputStream responseStream = statusCode >= 200 && statusCode < 300
					? connection.getInputStream()
					: connection.getErrorStream();
			String responseBody = responseStream == null
					? ""
					: new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);

			if (statusCode < 200 || statusCode >= 300) {
				throw new IllegalStateException("Token endpoint request failed with status " + statusCode
						+ ". Response: " + responseBody);
			}

			JsonObject tokenJson = new Gson().fromJson(responseBody, JsonObject.class);
			String accessToken = tokenJson.get("access_token").getAsString();
			if (!tokenJson.has("expires_in") || tokenJson.get("expires_in").isJsonNull()) {
				throw new IllegalStateException("Token response does not contain required field: expires_in");
			}

			long expiresIn = tokenJson.get("expires_in").getAsLong();
			long validFor = Math.max(1L, expiresIn - TOKEN_REFRESH_SKEW_SECONDS);
			return new AccessToken(accessToken, Instant.now().plusSeconds(validFor));
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to fetch OAuth2 access token.", ex);
		}
	}

	private SSLContext createMutualTlsContext(String certificatePem, String privateKeyPem) {
		try {
			PemSslStoreDetails keyStoreDetails = PemSslStoreDetails.forCertificates(certificatePem)
					.withPrivateKey(privateKeyPem);
			PemSslStoreDetails trustStoreDetails = PemSslStoreDetails.forCertificates(certificatePem);
			PemSslStoreBundle storeBundle = new PemSslStoreBundle(keyStoreDetails, trustStoreDetails);
			SslBundle sslBundle = SslBundle.of(storeBundle);
			return sslBundle.createSslContext();
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to initialize mTLS context.", ex);
		}
	}

	private record AccessToken(String value, Instant validUntil) {
	}
}
