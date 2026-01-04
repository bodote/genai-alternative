package de.bas.bodo.genai.ingestion.testing;

import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import de.bas.bodo.genai.retrieval.StoredChunk;
import java.util.ArrayList;
import java.util.List;

public final class RecordingRetrievalStore implements RetrievalStore {
	private final List<EmbeddedChunk> savedChunks = new ArrayList<>();

	@Override
	public void addDocuments(List<EmbeddedChunk> chunks) {
		savedChunks.addAll(chunks);
	}

	@Override
	public List<StoredChunk> search(String query, int topK) {
		throw new UnsupportedOperationException("Search not supported in recording store");
	}

	public List<EmbeddedChunk> savedChunks() {
		return List.copyOf(savedChunks);
	}

	public void clear() {
		savedChunks.clear();
	}
}
