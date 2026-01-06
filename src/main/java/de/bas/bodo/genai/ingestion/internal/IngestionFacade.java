package de.bas.bodo.genai.ingestion.internal;

import de.bas.bodo.genai.ingestion.chunking.TextChunker;
import de.bas.bodo.genai.ingestion.embedding.EmbeddingIngestionService;
import de.bas.bodo.genai.ingestion.gutenberg.GutenbergTextCleaner;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class IngestionFacade implements IngestionHandler {
	private final GutenbergTextCleaner textCleaner;
	private final TextChunker textChunker;
	private final EmbeddingIngestionService embeddingIngestionService;

	public IngestionFacade(
			GutenbergTextCleaner textCleaner,
			TextChunker textChunker,
			EmbeddingIngestionService embeddingIngestionService
	) {
		this.textCleaner = textCleaner;
		this.textChunker = textChunker;
		this.embeddingIngestionService = embeddingIngestionService;
	}

	@Override
	public void ingestRawText(int workId, String rawText, int maxLength, int overlap, int minimumLength) {
		String cleaned = textCleaner.stripBoilerplate(rawText);
		String normalized = textCleaner.normalizeWhitespace(cleaned);
		List<String> chunks = textChunker.chunkRecursively(normalized, maxLength, overlap, minimumLength);
		embeddingIngestionService.ingest(workId, chunks);
	}
}
