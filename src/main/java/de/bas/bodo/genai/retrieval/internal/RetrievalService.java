package de.bas.bodo.genai.retrieval.internal;

import de.bas.bodo.genai.retrieval.RetrievalGateway;
import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.retrieval.RetrievedChunk;
import java.util.List;

public final class RetrievalService implements RetrievalGateway {
	private final QueryEmbeddingClient embeddingClient;
	private final InternalRetrievalStore retrievalStore;
	private final int embeddingDimension;

	public RetrievalService(QueryEmbeddingClient embeddingClient, InternalRetrievalStore retrievalStore, int embeddingDimension) {
		this.embeddingClient = embeddingClient;
		this.retrievalStore = retrievalStore;
		this.embeddingDimension = embeddingDimension;
	}

	@Override
	public RetrievalResult retrieve(String query, int topK) {
		List<Float> embedding = embeddingClient.embed(query);
		if (embedding.size() != embeddingDimension) {
			throw new IllegalStateException(
					"Embedding dimension mismatch: expected " + embeddingDimension + " but got " + embedding.size()
			);
		}
		List<StoredChunk> storedChunks = retrievalStore.search(query, topK);
		List<RetrievedChunk> chunks = storedChunks.stream()
				.map(chunk -> new RetrievedChunk(chunk.workId(), chunk.index(), chunk.text(), chunk.score()))
				.toList();
		return new RetrievalResult(chunks);
	}
}
