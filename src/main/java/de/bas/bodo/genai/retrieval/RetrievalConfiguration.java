package de.bas.bodo.genai.retrieval;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires retrieval beans for the module.
 *
 * <p>Creates a {@link QueryEmbeddingClient} adapter around Spring AI's {@link EmbeddingModel}
 * so the retrieval layer can work with plain float vectors. It also builds the
 * {@link RetrievalService} using the configured embedding dimension from
 * {@link RetrievalProperties} and the module-owned {@link RetrievalStore}.</p>
 */
@Configuration
@EnableConfigurationProperties(RetrievalProperties.class)
public class RetrievalConfiguration {
	@Bean
	QueryEmbeddingClient queryEmbeddingClient(EmbeddingModel embeddingModel) {
		return query -> toFloatList(embeddingModel.embed(query));
	}

	@Bean
	RetrievalService retrievalService(
			QueryEmbeddingClient embeddingClient,
			RetrievalStore retrievalStore,
			RetrievalProperties properties
	) {
		return new RetrievalService(embeddingClient, retrievalStore, properties.getEmbeddingDimension());
	}

	private static java.util.List<Float> toFloatList(float[] embedding) {
		java.util.List<Float> values = new java.util.ArrayList<>(embedding.length);
		for (float value : embedding) {
			values.add(value);
		}
		return java.util.List.copyOf(values);
	}
}
