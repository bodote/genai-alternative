package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.ingestion.chunking.TextChunker;
import de.bas.bodo.genai.ingestion.embedding.EmbeddingIngestionProperties;
import de.bas.bodo.genai.ingestion.embedding.EmbeddingIngestionService;
import de.bas.bodo.genai.ingestion.internal.IngestionFacade;
import de.bas.bodo.genai.ingestion.testing.RecordingEmbeddingClient;
import de.bas.bodo.genai.ingestion.testing.RecordingRetrievalStore;
import de.bas.bodo.genai.retrieval.EmbeddedChunk;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Gutenberg ingestion flow")
class GutenbergIngestionFlowTest {
	private static final String AUTHOR_NAME = "Conan Doyle";
	private static final int WORK_ID = 1661;
	private static final int CHUNK_SIZE = 1000;
	private static final int CHUNK_OVERLAP = 100;
	private static final int MAX_DOWNLOAD_COUNT = 1;
	private static final String WORK_TITLE = "A Study in Scarlet";
	private static final String BOOK_DETAILS_URL = "https://gutendex.com/books/1661/";
	private static final String BOOKS_QUERY_URL =
			"https://gutendex.com/books?search=Conan+Doyle&languages=en";
	private static final String TEXT_URL = "https://www.gutenberg.org/files/1661/1661-0.txt";
	private static final String RAW_TEXT = String.join("\n",
			"Header",
			"*** START OF THE PROJECT GUTENBERG EBOOK THE ADVENTURES OF SHERLOCK HOLMES ***",
			"Holmes lives at Baker Street.",
			"*** END OF THE PROJECT GUTENBERG EBOOK THE ADVENTURES OF SHERLOCK HOLMES ***",
			"Footer"
	);

	@Test
	void downloadsAndIngestsConfiguredWorks() {
		RecordingEmbeddingClient embeddingClient = RecordingEmbeddingClient.incremental();
		RecordingRetrievalStore retrievalStore = new RecordingRetrievalStore();
		EmbeddingIngestionProperties embeddingProperties = new EmbeddingIngestionProperties();
		EmbeddingIngestionService embeddingIngestionService =
				new EmbeddingIngestionService(embeddingClient, retrievalStore, embeddingProperties);
		GutenbergTextCleaner textCleaner = new GutenbergTextCleaner();
		TextChunker textChunker = new TextChunker();
		IngestionFacade ingestionFacade = new IngestionFacade(textCleaner, textChunker, embeddingIngestionService);
		GutenbergIngestionProperties properties = new GutenbergIngestionProperties();
		properties.setAuthorName(AUTHOR_NAME);
		properties.setChunkSize(CHUNK_SIZE);
		properties.setChunkOverlap(CHUNK_OVERLAP);
		properties.setMaxDownloadCount(MAX_DOWNLOAD_COUNT);
		FakeHttpClient httpClient = new FakeHttpClient(Map.of(
				BOOKS_QUERY_URL, "{\"results\":[{\"id\":" + WORK_ID + ",\"title\":\"" + WORK_TITLE + "\"}]}",
				BOOK_DETAILS_URL, "{\"id\":" + WORK_ID + ",\"formats\":{\"text/plain; charset=utf-8\":\"" + TEXT_URL + "\"}}",
				TEXT_URL, RAW_TEXT
		));
		GutenbergCatalogClient catalogClient = new GutenbergCatalogClient(httpClient, new ObjectMapper());
		GutenbergIngestionTextStore textStore = new GutenbergIngestionTextStore(ingestionFacade, properties);
		GutenbergDownloadClient downloadClient = new GutenbergDownloadClient(httpClient, textStore, new ObjectMapper());
		GutenbergIngestionJobRunner job = new GutenbergIngestionJobRunner(catalogClient, downloadClient, properties);

		job.ingestAll();

		List<EmbeddedChunk> savedChunks = retrievalStore.savedChunks();
		assertThat(savedChunks).hasSize(1);
		EmbeddedChunk chunk = savedChunks.getFirst();
		assertThat(chunk.workId()).isEqualTo(WORK_ID);
		assertThat(chunk.index()).isEqualTo(0);
		assertThat(chunk.text()).isEqualTo("Holmes lives at Baker Street.");
		assertThat(chunk.embedding()).isNotEmpty();
	}

	private static final class FakeHttpClient implements GutenbergHttpClient {
		private final Map<String, String> responses;

		private FakeHttpClient(Map<String, String> responses) {
			this.responses = new HashMap<>(responses);
		}

		@Override
		public String get(String url) {
			String response = responses.get(url);
			if (response == null) {
				throw new IllegalArgumentException("No response configured for " + url);
			}
			return response;
		}
	}
}
