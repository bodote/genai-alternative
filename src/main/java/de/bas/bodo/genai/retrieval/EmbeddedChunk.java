package de.bas.bodo.genai.retrieval;

import java.util.List;

public record EmbeddedChunk(int workId, int index, String text, List<Float> embedding) {
	public EmbeddedChunk {
		embedding = List.copyOf(embedding);
	}
}
