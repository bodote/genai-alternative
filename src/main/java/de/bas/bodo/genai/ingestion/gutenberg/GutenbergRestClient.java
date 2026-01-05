package de.bas.bodo.genai.ingestion.gutenberg;

import java.util.Optional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

final class GutenbergRestClient implements GutenbergHttpClient {
	private final RestClient restClient;

	GutenbergRestClient(RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public String get(String url) {
		String currentUrl = url;
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				var response = restClient.get().uri(currentUrl).retrieve().toEntity(String.class);
				if (response.getStatusCode().is3xxRedirection()) {
					var location = response.getHeaders().getLocation();
					if (location == null) {
						throw new IllegalStateException("Redirect without location for " + currentUrl);
					}
					currentUrl = resolveRedirect(currentUrl, location);
					continue;
				}
				if (!response.getStatusCode().is2xxSuccessful()) {
					throw new IllegalStateException(
							"Failed to fetch " + currentUrl + " (status " + response.getStatusCode() + ")"
					);
				}
				String bodyUrl = currentUrl;
				return Optional.ofNullable(response.getBody())
						.filter(body -> !body.isBlank())
						.orElseThrow(() -> new IllegalStateException("Empty response body for " + bodyUrl));
			} catch (RestClientResponseException ex) {
				throw new IllegalStateException(
						"Failed to fetch " + currentUrl + " (status " + ex.getRawStatusCode() + ")",
						ex
				);
			}
		}
		throw new IllegalStateException("Too many redirects for " + url);
	}

	private static String resolveRedirect(String currentUrl, java.net.URI location) {
		if (location.isAbsolute()) {
			return location.toString();
		}
		return java.net.URI.create(currentUrl).resolve(location).toString();
	}
}
