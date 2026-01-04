package de.bas.bodo.genai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("RetrievalConfiguration")
class RetrievalConfigurationTest {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(RetrievalConfiguration.class)
			.withBean(QueryEmbeddingClient.class, () -> query -> List.of(0.1f, 0.2f, 0.3f))
			.withBean(RecordingRetrievalStore.class);

	@Test
	void createsRetrievalServiceWithConfiguredDimension() {
		contextRunner
				.withPropertyValues("genai.retrieval.embedding-dimension=3")
				.run(context -> {
					RetrievalService service = context.getBean(RetrievalService.class);
					RecordingRetrievalStore store = context.getBean(RecordingRetrievalStore.class);

					service.retrieve("query", 1);

					assertThat(store.receivedQuery()).isEqualTo("query");
					assertThat(store.receivedTopK()).isEqualTo(1);
				});
	}

	@Test
	void rejectsWhenEmbeddingDimensionDoesNotMatch() {
		ApplicationContextRunner runner = new ApplicationContextRunner()
				.withUserConfiguration(RetrievalConfiguration.class)
				.withBean(QueryEmbeddingClient.class, () -> query -> List.of(0.1f, 0.2f))
				.withBean(RecordingRetrievalStore.class);

		runner
				.withPropertyValues("genai.retrieval.embedding-dimension=3")
				.run(context -> {
					RetrievalService service = context.getBean(RetrievalService.class);

					assertThatThrownBy(() -> service.retrieve("query", 1))
							.isInstanceOf(IllegalStateException.class)
							.hasMessage("Embedding dimension mismatch: expected 3 but got 2");
				});
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
