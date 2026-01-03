package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GutenbergDownloadClient")
class GutenbergDownloadClientTest {
	private static final int WORK_ID = 1661;
	private static final String WORK_PAGE_URL = "https://www.gutenberg.org/ebooks/1661";
	private static final String TEXT_URL = "https://www.gutenberg.org/files/1661/1661-0.txt";
	private static final String WORK_PAGE_HTML = """
			<html>
				<body>
					<a href=\"/files/1661/1661-0.txt\">Plain Text UTF-8</a>
				</body>
			</html>
			""";
	private static final String TEXT_BODY = "Sherlock Holmes";

	@Test
	void downloadsCanonicalTextAndStoresIt() {
		RecordingGutenbergHttpClient httpClient = RecordingGutenbergHttpClient.withResponses(Map.of(
				WORK_PAGE_URL, WORK_PAGE_HTML,
				TEXT_URL, TEXT_BODY
		));
		RecordingTextStore store = new RecordingTextStore();
		GutenbergDownloadClient client = new GutenbergDownloadClient(httpClient, store);

		client.downloadWork(WORK_ID);

		assertThat(httpClient.requestedUrls()).containsExactly(WORK_PAGE_URL, TEXT_URL);
		assertThat(store.savedWorkId()).isEqualTo(WORK_ID);
		assertThat(store.savedText()).isEqualTo(TEXT_BODY);
		assertThat(store.savedSourceUrl()).isEqualTo(TEXT_URL);
	}

	private static final class RecordingTextStore implements GutenbergTextStore {
		private int savedWorkId;
		private String savedText = "";
		private String savedSourceUrl = "";

		@Override
		public void save(int workId, String text, String sourceUrl) {
			this.savedWorkId = workId;
			this.savedText = text;
			this.savedSourceUrl = sourceUrl;
		}

		private int savedWorkId() {
			return savedWorkId;
		}

		private String savedText() {
			return savedText;
		}

		private String savedSourceUrl() {
			return savedSourceUrl;
		}
	}
}
