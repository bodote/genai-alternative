package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.ingestion.internal.IngestionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GutenbergIngestionTextStore")
class GutenbergIngestionTextStoreTest {
	private static final int WORK_ID = 1661;
	private static final String TEXT = "Raw text";
	private static final String SOURCE_URL = "https://www.gutenberg.org/ebooks/1661.txt";
	private static final int CHUNK_SIZE = 1000;
	private static final int CHUNK_OVERLAP = 100;

	@Test
	void forwardsTextToIngestionFacadeUsingConfiguredChunking() {
		RecordingIngestionHandler handler = new RecordingIngestionHandler();
		GutenbergIngestionProperties properties = new GutenbergIngestionProperties();
		properties.setChunkSize(CHUNK_SIZE);
		properties.setChunkOverlap(CHUNK_OVERLAP);
		GutenbergIngestionTextStore textStore = new GutenbergIngestionTextStore(handler, properties);

		textStore.save(WORK_ID, TEXT, SOURCE_URL);

		assertThat(handler.workId()).isEqualTo(WORK_ID);
		assertThat(handler.text()).isEqualTo(TEXT);
		assertThat(handler.chunkSize()).isEqualTo(CHUNK_SIZE);
		assertThat(handler.chunkOverlap()).isEqualTo(CHUNK_OVERLAP);
	}

	private static final class RecordingIngestionHandler implements IngestionHandler {
		private int workId;
		private String text = "";
		private int chunkSize;
		private int chunkOverlap;

		@Override
		public void ingestRawText(int workId, String rawText, int maxLength, int overlap) {
			this.workId = workId;
			this.text = rawText;
			this.chunkSize = maxLength;
			this.chunkOverlap = overlap;
		}

		private int workId() {
			return workId;
		}

		private String text() {
			return text;
		}

		private int chunkSize() {
			return chunkSize;
		}

		private int chunkOverlap() {
			return chunkOverlap;
		}
	}
}
