package de.bas.bodo.genai.ingestion.gutenberg;

interface GutenbergTextStore {
	void save(int workId, String text, String sourceUrl);
}
