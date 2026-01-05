package de.bas.bodo.genai.ingestion.internal;

public interface IngestionHandler {
	void ingestRawText(int workId, String rawText, int maxLength, int overlap);
}
