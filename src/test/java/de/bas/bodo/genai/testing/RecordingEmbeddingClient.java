package de.bas.bodo.genai.testing;

import de.bas.bodo.genai.ingestion.embedding.EmbeddingClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class RecordingEmbeddingClient implements EmbeddingClient {
	private final Function<List<String>, List<float[]>> generator;
	private List<String> requestedTexts = List.of();
	private List<List<Float>> providedEmbeddings = List.of();

	private RecordingEmbeddingClient(Function<List<String>, List<float[]>> generator) {
		this.generator = generator;
	}

	public static RecordingEmbeddingClient fixed(List<float[]> embeddings) {
		return new RecordingEmbeddingClient(texts -> embeddings);
	}

	public static RecordingEmbeddingClient incremental() {
		return new RecordingEmbeddingClient(texts -> {
			List<float[]> embeddings = new ArrayList<>();
			for (int i = 0; i < texts.size(); i++) {
				embeddings.add(new float[] {i + 1.0f, i + 2.0f});
			}
			return embeddings;
		});
	}

	@Override
	public List<float[]> embedAll(List<String> texts) {
		requestedTexts = List.copyOf(texts);
		List<float[]> embeddings = generator.apply(requestedTexts);
		providedEmbeddings = embeddings.stream()
				.map(RecordingEmbeddingClient::toFloatList)
				.toList();
		return embeddings;
	}

	public List<String> requestedTexts() {
		return requestedTexts;
	}

	public List<List<Float>> providedEmbeddings() {
		return List.copyOf(providedEmbeddings);
	}

	public void reset() {
		requestedTexts = List.of();
		providedEmbeddings = List.of();
	}

	private static List<Float> toFloatList(float[] embedding) {
		List<Float> values = new ArrayList<>(embedding.length);
		for (float value : embedding) {
			values.add(value);
		}
		return List.copyOf(values);
	}
}
