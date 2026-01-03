package de.bas.bodo.genai.ingestion.embedding;

import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import java.util.ArrayList;
import java.util.List;

public final class EmbeddingIngestionService {
	private final EmbeddingClient embeddingClient;
	private final RetrievalStore retrievalStore;

	public EmbeddingIngestionService(EmbeddingClient embeddingClient, RetrievalStore retrievalStore) {
		this.embeddingClient = embeddingClient;
		this.retrievalStore = retrievalStore;
	}

	public void ingest(int workId, List<String> chunks) {
		List<float[]> embeddings = embeddingClient.embedAll(chunks);
		if (embeddings.size() != chunks.size()) {
			throw new IllegalStateException("Embedding count does not match chunk count");
		}
		List<EmbeddedChunk> embeddedChunks = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i++) {
			embeddedChunks.add(new EmbeddedChunk(workId, i, chunks.get(i), embeddings.get(i)));
		}
		retrievalStore.addDocuments(List.copyOf(embeddedChunks));
	}
}
