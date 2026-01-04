package de.bas.bodo.genai.retrieval;

import java.util.List;

public interface QueryEmbeddingClient {
	List<Float> embed(String query);
}
