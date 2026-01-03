package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GutenbergCatalogClient")
class GutenbergCatalogClientTest {
	private static final int AUTHOR_ID = 69;
	private static final String EXPECTED_URL = "https://www.gutenberg.org/ebooks/author/69";
	private static final String SHERLOCK_TITLE = "The Adventures of Sherlock Holmes";
	private static final String HOUND_TITLE = "The Hound of the Baskervilles";
	private static final String SAMPLE_HTML = """
			<html>
				<body>
					<ul>
						<li class=\"booklink\">
							<a href=\"/ebooks/1661\">
								<span class=\"title\">The Adventures of Sherlock Holmes</span>
							</a>
						</li>
						<li class=\"booklink\">
							<a href=\"/ebooks/2852\">
								<span class=\"title\">The Hound of the Baskervilles</span>
							</a>
						</li>
					</ul>
				</body>
			</html>
			""";

	@Test
	void fetchesWorksForAuthor() {
		RecordingGutenbergHttpClient httpClient = RecordingGutenbergHttpClient.fixedResponse(SAMPLE_HTML);
		GutenbergCatalogClient client = new GutenbergCatalogClient(httpClient);

		List<GutenbergWork> works = client.fetchWorksByAuthorId(AUTHOR_ID);

		assertThat(httpClient.lastUrl()).isEqualTo(EXPECTED_URL);
		assertThat(works)
				.extracting(GutenbergWork::id, GutenbergWork::title)
				.containsExactly(
						tuple(1661, SHERLOCK_TITLE),
						tuple(2852, HOUND_TITLE)
				);
	}
}
