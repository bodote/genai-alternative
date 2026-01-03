package de.bas.bodo.genai.ingestion.embedding;

import java.util.List;

interface EmbeddingClient {
	List<float[]> embedAll(List<String> texts);
}
