package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("GutenbergDownloadClient IT")
class GutenbergDownloadClientIT {
	private static final int WORK_ID = 1661;

	@Test
	void downloadsTextFromRealGutendexApi() {
		RecordingTextStore textStore = new RecordingTextStore();
		RestClient restClient = RestClient.builder()
				.defaultHeader("User-Agent", "genai-codex/it")
				.defaultHeader("Accept", "application/json, text/plain, */*")
				.build();
		GutenbergRestClient httpClient = new GutenbergRestClient(restClient);
		GutenbergDownloadClient client = new GutenbergDownloadClient(httpClient, textStore, new ObjectMapper());

		client.downloadWork(WORK_ID);

		assertThat(textStore.workId()).isEqualTo(WORK_ID);
		assertThat(textStore.rawText()).contains("PROJECT GUTENBERG");
		assertThat(textStore.sourceUrl()).startsWith("https://www.gutenberg.org/");
	}

	private static final class RecordingTextStore implements GutenbergTextStore {
		private int workId;
		private String rawText = "";
		private String sourceUrl = "";

		@Override
		public void save(int workId, String text, String sourceUrl) {
			this.workId = workId;
			this.rawText = text;
			this.sourceUrl = sourceUrl;
		}

		private int workId() {
			return workId;
		}

		private String rawText() {
			return rawText;
		}

		private String sourceUrl() {
			return sourceUrl;
		}
	}
}
