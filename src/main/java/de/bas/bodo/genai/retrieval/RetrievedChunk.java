package de.bas.bodo.genai.retrieval;

public record RetrievedChunk(int workId, int index, String text, double score) {
	public static RetrievedChunk from(StoredChunk storedChunk) {
		return new RetrievedChunk(storedChunk.workId(), storedChunk.index(), storedChunk.text(), storedChunk.score());
	}
}
