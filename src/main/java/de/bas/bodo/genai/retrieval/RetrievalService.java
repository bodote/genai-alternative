package de.bas.bodo.genai.retrieval;

import java.util.List;

public final class RetrievalService {
	private final QueryEmbeddingClient embeddingClient;
	private final RetrievalStore retrievalStore;
	private final int embeddingDimension;

	public RetrievalService(QueryEmbeddingClient embeddingClient, RetrievalStore retrievalStore, int embeddingDimension) {
		this.embeddingClient = embeddingClient;
		this.retrievalStore = retrievalStore;
		this.embeddingDimension = embeddingDimension;
	}

	public RetrievalResult retrieve(String query, int topK) {
		List<Float> embedding = embeddingClient.embed(query);
		if (embedding.size() != embeddingDimension) {
			throw new IllegalStateException(
					"Embedding dimension mismatch: expected " + embeddingDimension + " but got " + embedding.size()
			);
		}
		List<StoredChunk> storedChunks = retrievalStore.search(query, topK);
		return RetrievalResult.fromStored(storedChunks);
	}
}
