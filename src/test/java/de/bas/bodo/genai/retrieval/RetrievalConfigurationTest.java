package de.bas.bodo.genai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("RetrievalConfiguration")
class RetrievalConfigurationTest {
	private static final float[] MATCHING_EMBEDDING = {0.1f, 0.2f, 0.3f};
	private static final float[] WRONG_EMBEDDING = {0.1f, 0.2f};
	private static final String QUERY = "query";

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(RetrievalConfiguration.class)
			.withBean(RecordingRetrievalStore.class);

	@Test
	void createsRetrievalServiceWithConfiguredDimension() {
		EmbeddingModel embeddingModel = Mockito.mock(EmbeddingModel.class);
		Mockito.when(embeddingModel.embed(QUERY)).thenReturn(MATCHING_EMBEDDING);

		contextRunner
				.withBean(EmbeddingModel.class, () -> embeddingModel)
				.withPropertyValues("genai.retrieval.embedding-dimension=3")
				.run(context -> {
					RetrievalService service = context.getBean(RetrievalService.class);
					RecordingRetrievalStore store = context.getBean(RecordingRetrievalStore.class);

					service.retrieve(QUERY, 1);

					assertThat(store.receivedQuery()).isEqualTo(QUERY);
					assertThat(store.receivedTopK()).isEqualTo(1);
				});
	}

	@Test
	void rejectsWhenEmbeddingDimensionDoesNotMatch() {
		EmbeddingModel embeddingModel = Mockito.mock(EmbeddingModel.class);
		Mockito.when(embeddingModel.embed(QUERY)).thenReturn(WRONG_EMBEDDING);

		ApplicationContextRunner runner = new ApplicationContextRunner()
				.withUserConfiguration(RetrievalConfiguration.class)
				.withBean(EmbeddingModel.class, () -> embeddingModel)
				.withBean(RecordingRetrievalStore.class);

		runner
				.withPropertyValues("genai.retrieval.embedding-dimension=3")
				.run(context -> {
					RetrievalService service = context.getBean(RetrievalService.class);

					assertThatThrownBy(() -> service.retrieve(QUERY, 1))
							.isInstanceOf(IllegalStateException.class)
							.hasMessage("Embedding dimension mismatch: expected 3 but got 2");
				});
	}

	@Test
	void failsWhenEmbeddingModelMissing() {
		ApplicationContextRunner runner = new ApplicationContextRunner()
				.withUserConfiguration(RetrievalConfiguration.class)
				.withBean(RecordingRetrievalStore.class);

		runner.run(context -> assertThat(context).hasFailed());
	}

	static final class RecordingRetrievalStore implements RetrievalStore {
		private String receivedQuery = "";
		private int receivedTopK;

		@Override
		public void addDocuments(List<EmbeddedChunk> chunks) {
		}

		@Override
		public List<StoredChunk> search(String query, int topK) {
			this.receivedQuery = query;
			this.receivedTopK = topK;
			return List.of();
		}

		private String receivedQuery() {
			return receivedQuery;
		}

		private int receivedTopK() {
			return receivedTopK;
		}
	}
}
