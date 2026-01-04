package de.bas.bodo.genai.retrieval;

import java.util.List;

public record RetrievalResult(List<RetrievedChunk> chunks) {
	public static RetrievalResult fromStored(List<StoredChunk> storedChunks) {
		List<RetrievedChunk> chunks = storedChunks.stream()
				.map(RetrievedChunk::from)
				.toList();
		return new RetrievalResult(chunks);
	}
}
