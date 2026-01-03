package de.bas.bodo.genai.retrieval;

import java.util.List;

public interface RetrievalStore {
	void addDocuments(List<EmbeddedChunk> chunks);
}
