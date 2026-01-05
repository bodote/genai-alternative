package de.bas.bodo.genai.retrieval.internal;

import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import java.util.List;

public interface InternalRetrievalStore extends RetrievalStore {
	List<StoredChunk> search(String query, int topK);
}
