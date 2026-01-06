package de.bas.bodo.genai.ingestion.gutenberg;

import de.bas.bodo.genai.ingestion.internal.IngestionHandler;

final class GutenbergIngestionTextStore implements GutenbergTextStore {
	private final IngestionHandler ingestionHandler;
	private final GutenbergIngestionProperties properties;

	GutenbergIngestionTextStore(IngestionHandler ingestionHandler, GutenbergIngestionProperties properties) {
		this.ingestionHandler = ingestionHandler;
		this.properties = properties;
	}

	@Override
	public void save(int workId, String text, String sourceUrl) {
		ingestionHandler.ingestRawText(
				workId,
				text,
				properties.getChunkSize(),
				properties.getChunkOverlap(),
				properties.getChunkSizeMinimum()
		);
	}
}
