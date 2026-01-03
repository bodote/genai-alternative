package de.bas.bodo.genai.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import de.bas.bodo.genai.ingestion.embedding.EmbeddingClient;
import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode;

@ApplicationModuleTest(module = "ingestion", mode = BootstrapMode.DIRECT_DEPENDENCIES)
@DisplayName("Ingestion module")
class IngestionModuleTest {
	private static final int WORK_ID = 1661;
	private static final int MAX_LENGTH = 12;
	private static final int OVERLAP = 0;
	private static final String START_MARKER = "*** START OF THE PROJECT GUTENBERG EBOOK THE ADVENTURES OF SHERLOCK HOLMES ***";
	private static final String END_MARKER = "*** END OF THE PROJECT GUTENBERG EBOOK THE ADVENTURES OF SHERLOCK HOLMES ***";
	private static final List<String> EXPECTED_CHUNKS = List.of("Line one.", "Line two.");

	@Autowired
	private IngestionFacade ingestionFacade;

	@Autowired
	private RecordingEmbeddingClient embeddingClient;

	@Autowired
	private RecordingRetrievalStore retrievalStore;

	@Test
	void ingestsRawTextThroughPublicApi() {
		String raw = String.join("\n",
				"Header noise",
				START_MARKER,
				"Line one.",
				"",
				"Line two.",
				END_MARKER,
				"Footer noise"
		);

		ingestionFacade.ingestRawText(WORK_ID, raw, MAX_LENGTH, OVERLAP);

		assertThat(embeddingClient.requestedTexts()).isEqualTo(EXPECTED_CHUNKS);
		assertThat(retrievalStore.savedChunks())
				.extracting(EmbeddedChunk::workId, EmbeddedChunk::index, EmbeddedChunk::text)
				.containsExactly(
					tuple(WORK_ID, 0, EXPECTED_CHUNKS.get(0)),
					tuple(WORK_ID, 1, EXPECTED_CHUNKS.get(1))
				);
		assertThat(retrievalStore.savedChunks().get(0).embedding())
				.isEqualTo(embeddingClient.providedEmbeddings().get(0));
		assertThat(retrievalStore.savedChunks().get(1).embedding())
				.isEqualTo(embeddingClient.providedEmbeddings().get(1));
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		RecordingEmbeddingClient embeddingClient() {
			return new RecordingEmbeddingClient();
		}

		@Bean
		RecordingRetrievalStore retrievalStore() {
			return new RecordingRetrievalStore();
		}
	}

	static final class RecordingEmbeddingClient implements EmbeddingClient {
		private final List<List<Float>> providedEmbeddings = new ArrayList<>();
		private List<String> requestedTexts = List.of();

		@Override
		public List<float[]> embedAll(List<String> texts) {
			requestedTexts = List.copyOf(texts);
			providedEmbeddings.clear();
			List<float[]> embeddings = new ArrayList<>();
			for (int i = 0; i < texts.size(); i++) {
				float[] embedding = new float[] {i + 1.0f, i + 2.0f};
				embeddings.add(embedding);
				providedEmbeddings.add(List.of(embedding[0], embedding[1]));
			}
			return embeddings;
		}

		private List<String> requestedTexts() {
			return requestedTexts;
		}

		private List<List<Float>> providedEmbeddings() {
			return List.copyOf(providedEmbeddings);
		}
	}

	static final class RecordingRetrievalStore implements RetrievalStore {
		private final List<EmbeddedChunk> savedChunks = new ArrayList<>();

		@Override
		public void addDocuments(List<EmbeddedChunk> chunks) {
			savedChunks.addAll(chunks);
		}

		private List<EmbeddedChunk> savedChunks() {
			return List.copyOf(savedChunks);
		}
	}
}
