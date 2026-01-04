package de.bas.bodo.genai.ingestion.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfiguration {
	@Bean
	EmbeddingClient embeddingClient(EmbeddingModel embeddingModel) {
		return embeddingModel::embed;
	}
}
