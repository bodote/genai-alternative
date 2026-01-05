package de.bas.bodo.genai.retrieval;

import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Component
public final class VectorStoreRetrievalStore implements RetrievalStore {
	public static final String METADATA_WORK_ID = "workId";
	public static final String METADATA_CHUNK_INDEX = "chunkIndex";

	private final VectorStore vectorStore;

	public VectorStoreRetrievalStore(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	@Override
	public void addDocuments(List<EmbeddedChunk> chunks) {
		List<Document> documents = chunks.stream()
				.map(VectorStoreRetrievalStore::toDocument)
				.toList();
		vectorStore.add(documents);
	}

	@Override
	public List<StoredChunk> search(String query, int topK) {
		SearchRequest request = SearchRequest.builder()
				.query(query)
				.topK(topK)
				.build();
		return vectorStore.similaritySearch(request).stream()
				.map(VectorStoreRetrievalStore::toStoredChunk)
				.toList();
	}

	private static Document toDocument(EmbeddedChunk chunk) {
		return Document.builder()
				.text(chunk.text())
				.metadata(Map.of(
						METADATA_WORK_ID, chunk.workId(),
						METADATA_CHUNK_INDEX, chunk.index()
				))
				.build();
	}

	private static StoredChunk toStoredChunk(Document document) {
		Map<String, Object> metadata = document.getMetadata();
		int workId = readInt(metadata, METADATA_WORK_ID);
		int chunkIndex = readInt(metadata, METADATA_CHUNK_INDEX);
		double score = document.getScore() == null ? 0.0 : document.getScore();
		return new StoredChunk(workId, chunkIndex, document.getText(), score);
	}

		private static int readInt(Map<String, Object> metadata, String key) {
			Object value = metadata.get(key);
			if (value instanceof Number number) {
				return number.intValue();
			}
			throw new IllegalStateException("Missing numeric metadata for " + key);
		}
	}
