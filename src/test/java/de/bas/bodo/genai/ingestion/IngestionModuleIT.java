package de.bas.bodo.genai.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import de.bas.bodo.genai.ingestion.testing.RecordingEmbeddingClient;
import de.bas.bodo.genai.ingestion.testing.RecordingRetrievalStore;
import de.bas.bodo.genai.ingestion.testing.IngestionTestcontainersConfiguration;
import de.bas.bodo.genai.ingestion.internal.IngestionFacade;
import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode;

@ApplicationModuleTest(module = "ingestion", mode = BootstrapMode.DIRECT_DEPENDENCIES)
@Import(IngestionTestcontainersConfiguration.class)
@DisplayName("Ingestion module")
class IngestionModuleIT {
	private static final int WORK_ID = 1661;
	private static final int MAX_LENGTH = 12;
	private static final int OVERLAP = 0;
	private static final String START_MARKER = "*** START OF THE PROJECT GUTENBERG EBOOK THE ADVENTURES OF SHERLOCK HOLMES ***";
	private static final String END_MARKER = "*** END OF THE PROJECT GUTENBERG EBOOK THE ADVENTURES OF SHERLOCK HOLMES ***";
	private static final List<String> EXPECTED_CHUNKS = List.of("Line one.", "Line two.");

	private final IngestionFacade ingestionFacade;

	private final RecordingEmbeddingClient embeddingClient;

	private final RecordingRetrievalStore retrievalStore;

	@Autowired
	IngestionModuleIT(
			IngestionFacade ingestionFacade,
			RecordingEmbeddingClient embeddingClient,
			RecordingRetrievalStore retrievalStore
	) {
		this.ingestionFacade = ingestionFacade;
		this.embeddingClient = embeddingClient;
		this.retrievalStore = retrievalStore;
	}

	@BeforeEach
	void resetFakes() {
		embeddingClient.reset();
		retrievalStore.clear();
	}

	@Test
	void ingestsRawTextThroughPublicApi() {
		String raw = String.join("\n",
				"Header noise",
				START_MARKER,
				"Line one.",
				"",
				"Line two.",
				END_MARKER,
				"Footer noise"
		);

		ingestionFacade.ingestRawText(WORK_ID, raw, MAX_LENGTH, OVERLAP);

		assertThat(embeddingClient.requestedTexts()).isEqualTo(EXPECTED_CHUNKS);
		assertThat(retrievalStore.savedChunks())
				.extracting(EmbeddedChunk::workId, EmbeddedChunk::index, EmbeddedChunk::text)
				.containsExactly(
					tuple(WORK_ID, 0, EXPECTED_CHUNKS.get(0)),
					tuple(WORK_ID, 1, EXPECTED_CHUNKS.get(1))
				);
		assertThat(retrievalStore.savedChunks().get(0).embedding())
				.isEqualTo(embeddingClient.providedEmbeddings().get(0));
		assertThat(retrievalStore.savedChunks().get(1).embedding())
				.isEqualTo(embeddingClient.providedEmbeddings().get(1));
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		RecordingEmbeddingClient testEmbeddingClient() {
			return RecordingEmbeddingClient.incremental();
		}

		@Bean
		@Primary
		RecordingRetrievalStore retrievalStore() {
			return new RecordingRetrievalStore();
		}
	}
}
