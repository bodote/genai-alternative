package de.bas.bodo.genai.retrieval;

public interface RetrievalGateway {
	RetrievalResult retrieve(String query, int topK);
}
