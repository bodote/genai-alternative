package de.bas.bodo.genai;

import de.bas.bodo.genai.ingestion.embedding.EmbeddingClient;
import de.bas.bodo.genai.generation.GenerationService;
import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import de.bas.bodo.genai.testing.TestcontainersConfiguration;
import java.util.List;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "genai.ingestion.startup.enabled=false")
@Import({GenaiApplicationIT.TestConfig.class, TestcontainersConfiguration.class})
class GenaiApplicationIT {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		EmbeddingClient testEmbeddingClient() {
			return texts -> List.of();
		}

		@Bean
		@Primary
		GenerationService testGenerationService() {
			return Mockito.mock(GenerationService.class);
		}

		@Bean
		@Primary
		RetrievalStore retrievalStore() {
			return new RetrievalStore() {
				@Override
				public void addDocuments(List<EmbeddedChunk> chunks) {
				}
			};
		}
	}
}
