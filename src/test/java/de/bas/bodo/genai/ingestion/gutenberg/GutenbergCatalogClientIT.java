package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("GutenbergCatalogClient IT")
class GutenbergCatalogClientIT {
	private static final String AUTHOR_NAME = "Conan Doyle";

	@Test
	void fetchesWorksFromRealGutendexApi() {
		RestClient restClient = RestClient.builder()
				.defaultHeader("User-Agent", "genai-codex/it")
				.defaultHeader("Accept", "application/json, text/plain, */*")
				.build();
		GutenbergRestClient httpClient = new GutenbergRestClient(restClient);
		GutenbergCatalogClient client = new GutenbergCatalogClient(httpClient, new ObjectMapper());

		List<GutenbergWork> works = client.fetchWorksByAuthorName(AUTHOR_NAME);

		assertThat(works).isNotEmpty();
		assertThat(works.getFirst().id()).isPositive();
	}
}
