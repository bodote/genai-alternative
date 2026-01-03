package de.bas.bodo.genai.ingestion.embedding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmbeddingIngestionService")
class EmbeddingIngestionServiceTest {
	private static final int WORK_ID = 1661;
	private static final List<String> CHUNKS = List.of("chunk one", "chunk two");

	@Test
	void embedsChunksAndStoresResults() {
		float[] firstEmbedding = new float[] {0.1f, 0.2f};
		float[] secondEmbedding = new float[] {0.3f, 0.4f};
		RecordingEmbeddingClient embeddingClient = new RecordingEmbeddingClient(List.of(firstEmbedding, secondEmbedding));
		RecordingRetrievalStore retrievalStore = new RecordingRetrievalStore();
		EmbeddingIngestionService service = new EmbeddingIngestionService(embeddingClient, retrievalStore);

		service.ingest(WORK_ID, CHUNKS);

		assertThat(embeddingClient.requestedTexts()).isEqualTo(CHUNKS);
		assertThat(retrievalStore.savedChunks())
				.extracting(EmbeddedChunk::workId, EmbeddedChunk::index, EmbeddedChunk::text, EmbeddedChunk::embedding)
				.containsExactly(
						tuple(WORK_ID, 0, CHUNKS.get(0), firstEmbedding),
						tuple(WORK_ID, 1, CHUNKS.get(1), secondEmbedding)
				);
	}

	private static final class RecordingEmbeddingClient implements EmbeddingClient {
		private final List<float[]> embeddings;
		private List<String> requestedTexts = List.of();

		private RecordingEmbeddingClient(List<float[]> embeddings) {
			this.embeddings = embeddings;
		}

		@Override
		public List<float[]> embedAll(List<String> texts) {
			this.requestedTexts = texts;
			return embeddings;
		}

		private List<String> requestedTexts() {
			return requestedTexts;
		}
	}

	private static final class RecordingRetrievalStore implements RetrievalStore {
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
