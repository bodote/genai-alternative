package de.bas.bodo.genai.retrieval;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "genai.retrieval")
public class RetrievalProperties {
	private int embeddingDimension = 1536;

	public int getEmbeddingDimension() {
		return embeddingDimension;
	}

	public void setEmbeddingDimension(int embeddingDimension) {
		this.embeddingDimension = embeddingDimension;
	}
}
