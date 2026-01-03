package de.bas.bodo.genai;

import de.bas.bodo.genai.ingestion.embedding.EmbeddingClient;
import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(GenaiApplicationTests.TestConfig.class)
class GenaiApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		EmbeddingClient embeddingClient() {
			return texts -> List.of();
		}

		@Bean
		RetrievalStore retrievalStore() {
			return new RetrievalStore() {
				@Override
				public void addDocuments(List<EmbeddedChunk> chunks) {
				}
			};
		}
	}
}
