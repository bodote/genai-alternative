package de.bas.bodo.genai.ingestion.embedding;

import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public final class EmbeddingIngestionService {
	private final EmbeddingClient embeddingClient;
	private final RetrievalStore retrievalStore;
	private final EmbeddingIngestionProperties properties;

	public EmbeddingIngestionService(
			EmbeddingClient embeddingClient,
			RetrievalStore retrievalStore,
			EmbeddingIngestionProperties properties
	) {
		this.embeddingClient = embeddingClient;
		this.retrievalStore = retrievalStore;
		this.properties = properties;
	}

	public void ingest(int workId, List<String> chunks) {
		List<float[]> embeddings = embedInBatches(chunks, properties.getBatchSize());
		List<EmbeddedChunk> embeddedChunks = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i++) {
			embeddedChunks.add(new EmbeddedChunk(workId, i, chunks.get(i), toFloatList(embeddings.get(i))));
		}
		retrievalStore.addDocuments(List.copyOf(embeddedChunks));
	}

	private List<float[]> embedInBatches(List<String> chunks, int batchSize) {
		if (batchSize <= 0) {
			throw new IllegalArgumentException("Embedding batch size must be positive");
		}
		List<float[]> embeddings = new ArrayList<>(chunks.size());
		for (int start = 0; start < chunks.size(); start += batchSize) {
			int end = Math.min(chunks.size(), start + batchSize);
			List<String> batch = chunks.subList(start, end);
			List<float[]> batchEmbeddings = embeddingClient.embedAll(batch);
			if (batchEmbeddings.size() != batch.size()) {
				throw new IllegalStateException("Embedding count does not match chunk count");
			}
			embeddings.addAll(batchEmbeddings);
		}
		return embeddings;
	}

	private static List<Float> toFloatList(float[] embedding) {
		return IntStream.range(0, embedding.length)
				.mapToObj(index -> embedding[index])
				.toList();
	}
}
