package de.bas.bodo.genai.ingestion.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "genai.ingestion.embedding")
public class EmbeddingIngestionProperties {
	private int batchSize = 500;

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
