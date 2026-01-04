package de.bas.bodo.genai.retrieval;

import java.util.List;

public interface RetrievalStore {
	void addDocuments(List<EmbeddedChunk> chunks);

	default List<StoredChunk> search(String query, int topK) {
		throw new UnsupportedOperationException("Search not implemented");
	}
}
