package de.bas.bodo.genai.ingestion.embedding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("EmbeddingConfiguration")
class EmbeddingConfigurationTest {
	private static final List<String> CHUNKS = List.of("First chunk", "Second chunk");
	private static final float[] FIRST_EMBEDDING = {0.1f, 0.2f};
	private static final float[] SECOND_EMBEDDING = {0.3f, 0.4f};
	private static final List<float[]> EMBEDDINGS = List.of(FIRST_EMBEDDING, SECOND_EMBEDDING);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(EmbeddingConfiguration.class);

	@Test
	void createsEmbeddingClientFromEmbeddingModel() {
		EmbeddingModel embeddingModel = Mockito.mock(EmbeddingModel.class);
		Mockito.when(embeddingModel.embed(CHUNKS)).thenReturn(EMBEDDINGS);

		contextRunner
				.withBean(EmbeddingModel.class, () -> embeddingModel)
				.run(context -> {
					EmbeddingClient embeddingClient = context.getBean(EmbeddingClient.class);

					List<float[]> result = embeddingClient.embedAll(CHUNKS);

					assertThat(result).containsExactly(FIRST_EMBEDDING, SECOND_EMBEDDING);
				});
	}

	@Test
	void failsWhenEmbeddingModelMissing() {
		contextRunner.run(context -> assertThat(context).hasFailed());
	}
}
