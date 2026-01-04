package de.bas.bodo.genai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

@DisplayName("VectorStoreRetrievalStore")
class VectorStoreRetrievalStoreTest {
	private static final String QUERY = "Baker Street";
	private static final int TOP_K = 3;
	private static final int WORK_ID = 1661;
	private static final int CHUNK_INDEX = 2;
	private static final String TEXT = "Sherlock Holmes lives on Baker Street.";
	private static final List<Float> EMBEDDING = List.of(0.12f, 0.34f);
	private static final double SCORE = 0.92;

	@Test
	void mapsSearchResultsFromVectorStoreMetadata() {
		Document document = Document.builder()
				.text(TEXT)
				.metadata(Map.of(
						VectorStoreRetrievalStore.METADATA_WORK_ID, WORK_ID,
						VectorStoreRetrievalStore.METADATA_CHUNK_INDEX, CHUNK_INDEX
				))
				.score(SCORE)
				.build();
		RecordingVectorStore vectorStore = new RecordingVectorStore(List.of(document));
		VectorStoreRetrievalStore store = new VectorStoreRetrievalStore(vectorStore);

		List<StoredChunk> results = store.search(QUERY, TOP_K);

		assertThat(vectorStore.lastRequest().getQuery()).isEqualTo(QUERY);
		assertThat(vectorStore.lastRequest().getTopK()).isEqualTo(TOP_K);
		assertThat(results).containsExactly(new StoredChunk(WORK_ID, CHUNK_INDEX, TEXT, SCORE));
	}

	@Test
	void mapsEmbeddedChunksToDocuments() {
		RecordingVectorStore vectorStore = new RecordingVectorStore(List.of());
		VectorStoreRetrievalStore store = new VectorStoreRetrievalStore(vectorStore);

		store.addDocuments(List.of(new EmbeddedChunk(WORK_ID, CHUNK_INDEX, TEXT, EMBEDDING)));

		assertThat(vectorStore.addedDocuments).hasSize(1);
		Document added = vectorStore.addedDocuments.getFirst();
		assertThat(added.getText()).isEqualTo(TEXT);
		assertThat(added.getMetadata()).containsEntry(VectorStoreRetrievalStore.METADATA_WORK_ID, WORK_ID)
				.containsEntry(VectorStoreRetrievalStore.METADATA_CHUNK_INDEX, CHUNK_INDEX);
	}

	private static final class RecordingVectorStore implements VectorStore {
		private final List<Document> documents;
		private SearchRequest lastRequest = SearchRequest.builder().query("").topK(0).build();
		private List<Document> addedDocuments = List.of();

		private RecordingVectorStore(List<Document> documents) {
			this.documents = documents;
		}

		@Override
		public List<Document> similaritySearch(SearchRequest request) {
			this.lastRequest = request;
			return documents;
		}

		@Override
		public void add(List<Document> documents) {
			this.addedDocuments = documents;
		}

		@Override
		public void delete(List<String> ids) {
		}

		@Override
		public void delete(Filter.Expression filterExpression) {
		}

		private SearchRequest lastRequest() {
			return lastRequest;
		}
	}
}
