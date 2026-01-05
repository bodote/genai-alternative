package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GutenbergCatalogClient")
class GutenbergCatalogClientTest {
	private static final String AUTHOR_NAME = "Conan Doyle";
	private static final String EXPECTED_URL = "https://gutendex.com/books?search=Conan+Doyle&languages=en";
	private static final String SHERLOCK_TITLE = "The Adventures of Sherlock Holmes";
	private static final String HOUND_TITLE = "The Hound of the Baskervilles";
	private static final String SAMPLE_JSON = """
			{
			  "count": 2,
			  "results": [
			    { "id": 1661, "title": "The Adventures of Sherlock Holmes" },
			    { "id": 2852, "title": "The Hound of the Baskervilles" }
			  ]
			}
			""";
	private static final String EMPTY_JSON = """
			{
			  "count": 0,
			  "results": []
			}
			""";
	private static final String FALLBACK_URL = "https://gutendex.com/books?search=Arthur+Conan+Doyle&languages=en";
	private static final String FALLBACK_AUTHOR = "Conan Doyle, Arthur";
	private static final String PRIMARY_FALLBACK_URL = "https://gutendex.com/books?search=Conan+Doyle%2C+Arthur&languages=en";

	@Test
	void fetchesWorksForAuthor() {
		RecordingGutenbergHttpClient httpClient = RecordingGutenbergHttpClient.fixedResponse(SAMPLE_JSON);
		GutenbergCatalogClient client = new GutenbergCatalogClient(httpClient, new ObjectMapper());

		List<GutenbergWork> works = client.fetchWorksByAuthorName(AUTHOR_NAME);

		assertThat(httpClient.lastUrl()).isEqualTo(EXPECTED_URL);
		assertThat(works)
				.extracting(GutenbergWork::id, GutenbergWork::title)
				.containsExactly(
						tuple(1661, SHERLOCK_TITLE),
						tuple(2852, HOUND_TITLE)
				);
	}

	@Test
	void retriesWithFallbackWhenSearchIsEmpty() {
		RecordingGutenbergHttpClient httpClient = RecordingGutenbergHttpClient.sequence(List.of(EMPTY_JSON, SAMPLE_JSON));
		GutenbergCatalogClient client = new GutenbergCatalogClient(httpClient, new ObjectMapper());

		List<GutenbergWork> works = client.fetchWorksByAuthorName(FALLBACK_AUTHOR);

		assertThat(httpClient.requestedUrls())
				.containsExactly(PRIMARY_FALLBACK_URL, FALLBACK_URL);
		assertThat(works)
				.extracting(GutenbergWork::id, GutenbergWork::title)
				.containsExactly(
						tuple(1661, SHERLOCK_TITLE),
						tuple(2852, HOUND_TITLE)
				);
	}
}
