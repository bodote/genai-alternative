package de.bas.bodo.genai.ingestion.testing;

import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import java.util.ArrayList;
import java.util.List;

public final class RecordingRetrievalStore implements RetrievalStore {
	private final List<EmbeddedChunk> savedChunks = new ArrayList<>();

	@Override
	public void addDocuments(List<EmbeddedChunk> chunks) {
		savedChunks.addAll(chunks);
	}

	public List<EmbeddedChunk> savedChunks() {
		return List.copyOf(savedChunks);
	}

	public void clear() {
		savedChunks.clear();
	}
}
