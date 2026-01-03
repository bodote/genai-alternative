package de.bas.bodo.genai.ingestion.gutenberg;

import java.util.Objects;
import org.springframework.web.client.RestClient;

final class GutenbergRestClient implements GutenbergHttpClient {
	private final RestClient restClient;

	GutenbergRestClient(RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public String get(String url) {
		return Objects.requireNonNull(restClient.get().uri(url).retrieve().body(String.class));
	}
}
