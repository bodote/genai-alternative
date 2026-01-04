package de.bas.bodo.genai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RetrievalService")
class RetrievalServiceTest {
	private static final String QUERY = "Where does Holmes live?";
	private static final int TOP_K = 2;
	private static final int EMBEDDING_DIMENSION = 3;
	private static final List<Float> MATCHING_EMBEDDING = List.of(0.1f, 0.2f, 0.3f);
	private static final List<Float> WRONG_EMBEDDING = List.of(0.1f, 0.2f);
	private static final int WORK_ID = 1661;
	private static final int FIRST_INDEX = 0;
	private static final int SECOND_INDEX = 1;
	private static final String FIRST_TEXT = "First";
	private static final String SECOND_TEXT = "Second";
	private static final double FIRST_SCORE = 0.91;
	private static final double SECOND_SCORE = 0.87;

	@Nested
	@DisplayName("retrieve")
	class Retrieve {
		@Test
		void embedsQueryAndPassesEmbeddingToStore() {
			RecordingEmbeddingClient embeddingClient = new RecordingEmbeddingClient(MATCHING_EMBEDDING);
			RecordingRetrievalStore retrievalStore = new RecordingRetrievalStore(List.of());
			RetrievalService service = new RetrievalService(embeddingClient, retrievalStore, EMBEDDING_DIMENSION);

			RetrievalResult result = service.retrieve(QUERY, TOP_K);

			assertThat(embeddingClient.requestedQuery()).isEqualTo(QUERY);
			assertThat(retrievalStore.receivedQuery()).isEqualTo(QUERY);
			assertThat(retrievalStore.receivedTopK()).isEqualTo(TOP_K);
			assertThat(result.chunks()).isEmpty();
		}

		@Test
		void rejectsUnexpectedEmbeddingDimension() {
			RecordingEmbeddingClient embeddingClient = new RecordingEmbeddingClient(WRONG_EMBEDDING);
			RecordingRetrievalStore retrievalStore = new RecordingRetrievalStore(List.of());
			RetrievalService service = new RetrievalService(embeddingClient, retrievalStore, EMBEDDING_DIMENSION);

			assertThatThrownBy(() -> service.retrieve(QUERY, TOP_K))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("Embedding dimension mismatch: expected 3 but got 2");
		}

		@Test
		void mapsStoredChunksInOrder() {
			List<StoredChunk> storedChunks = List.of(
					new StoredChunk(WORK_ID, FIRST_INDEX, FIRST_TEXT, FIRST_SCORE),
					new StoredChunk(WORK_ID, SECOND_INDEX, SECOND_TEXT, SECOND_SCORE)
			);
			RecordingEmbeddingClient embeddingClient = new RecordingEmbeddingClient(MATCHING_EMBEDDING);
			RecordingRetrievalStore retrievalStore = new RecordingRetrievalStore(storedChunks);
			RetrievalService service = new RetrievalService(embeddingClient, retrievalStore, EMBEDDING_DIMENSION);

			RetrievalResult result = service.retrieve(QUERY, TOP_K);

			assertThat(result.chunks()).containsExactly(
					new RetrievedChunk(WORK_ID, FIRST_INDEX, FIRST_TEXT, FIRST_SCORE),
					new RetrievedChunk(WORK_ID, SECOND_INDEX, SECOND_TEXT, SECOND_SCORE)
			);
		}
	}

	private static final class RecordingEmbeddingClient implements QueryEmbeddingClient {
		private final List<Float> embedding;
		private String requestedQuery = "";

		private RecordingEmbeddingClient(List<Float> embedding) {
			this.embedding = embedding;
		}

		@Override
		public List<Float> embed(String query) {
			this.requestedQuery = query;
			return embedding;
		}

		private String requestedQuery() {
			return requestedQuery;
		}
	}

	private static final class RecordingRetrievalStore implements RetrievalStore {
		private final List<StoredChunk> storedChunks;
		private String receivedQuery = "";
		private int receivedTopK;

		private RecordingRetrievalStore(List<StoredChunk> storedChunks) {
			this.storedChunks = new ArrayList<>(storedChunks);
		}

		@Override
		public List<StoredChunk> search(String query, int topK) {
			this.receivedQuery = query;
			this.receivedTopK = topK;
			return List.copyOf(storedChunks);
		}

		@Override
		public void addDocuments(List<EmbeddedChunk> chunks) {
		}

		private String receivedQuery() {
			return receivedQuery;
		}

		private int receivedTopK() {
			return receivedTopK;
		}
	}
}
