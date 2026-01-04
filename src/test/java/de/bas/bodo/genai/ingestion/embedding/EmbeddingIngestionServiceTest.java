package de.bas.bodo.genai.ingestion.embedding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.ingestion.testing.RecordingEmbeddingClient;
import de.bas.bodo.genai.ingestion.testing.RecordingRetrievalStore;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmbeddingIngestionService")
class EmbeddingIngestionServiceTest {
	private static final int WORK_ID = 1661;
	private static final List<String> CHUNKS = List.of("chunk one", "chunk two");
	private static final List<Float> FIRST_EMBEDDING = List.of(0.1f, 0.2f);
	private static final List<Float> SECOND_EMBEDDING = List.of(0.3f, 0.4f);

	@Test
	void embedsChunksAndStoresResults() {
		float[] firstEmbedding = new float[] {0.1f, 0.2f};
		float[] secondEmbedding = new float[] {0.3f, 0.4f};
		RecordingEmbeddingClient embeddingClient = RecordingEmbeddingClient.fixed(List.of(firstEmbedding, secondEmbedding));
		RecordingRetrievalStore retrievalStore = new RecordingRetrievalStore();
		EmbeddingIngestionService service = new EmbeddingIngestionService(embeddingClient, retrievalStore);

		service.ingest(WORK_ID, CHUNKS);

		assertThat(embeddingClient.requestedTexts()).isEqualTo(CHUNKS);
		assertThat(retrievalStore.savedChunks())
				.extracting(EmbeddedChunk::workId, EmbeddedChunk::index, EmbeddedChunk::text)
				.containsExactly(
						tuple(WORK_ID, 0, CHUNKS.get(0)),
						tuple(WORK_ID, 1, CHUNKS.get(1))
				);
		assertThat(retrievalStore.savedChunks().get(0).embedding()).isEqualTo(FIRST_EMBEDDING);
		assertThat(retrievalStore.savedChunks().get(1).embedding()).isEqualTo(SECOND_EMBEDDING);
	}

}
