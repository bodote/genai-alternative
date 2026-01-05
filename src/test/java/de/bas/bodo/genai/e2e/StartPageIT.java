package de.bas.bodo.genai.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import de.bas.bodo.genai.ingestion.IngestionFacade;
import de.bas.bodo.genai.ingestion.testing.IngestionTestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;

@DisplayName("Start page")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"spring.ai.vectorstore.type=pgvector",
				"spring.ai.vectorstore.pgvector.initialize-schema=true",
				"spring.ai.vectorstore.pgvector.dimensions=1536",
				"genai.retrieval.embedding-dimension=1536",
				"genai.generation.top-k=1"
		}
)
@Import({StartPageIT.TestConfig.class, IngestionTestcontainersConfiguration.class})
@TestInstance(Lifecycle.PER_CLASS)
class StartPageIT {
	private static final String EXPECTED_TITLE = "Sherlock Holmes RAG Assistant";
	private static final String ROOT_PATH = "/";
	private static final String QUESTION = "Lucy noticed a number on the ceiling when taking breakfast. which number was written into the ceiling?";
	private static final String ANSWER = "Lucy noticed the number 28 written into the ceiling with black charcoal.";
	private static final int WORK_ID = 28;
	private static final int CHUNK_SIZE = 500;
	private static final int CHUNK_OVERLAP = 0;

	@LocalServerPort
	private int port;

	@Autowired
	private IngestionFacade ingestionFacade;

	@BeforeAll
	void ingestTestCorpus() {
		ingestionFacade.ingestRawText(
				WORK_ID,
				"Lucy noticed the number 28 written into the ceiling with black charcoal.",
				CHUNK_SIZE,
				CHUNK_OVERLAP
		);
	}

	@Test
	void rendersLandingPage() {
		try (Playwright playwright = Playwright.create()) {
			Browser browser = playwright.chromium().launch(
					new BrowserType.LaunchOptions().setHeadless(true)
			);
			Page page = browser.newPage();
			page.navigate("http://localhost:" + port + ROOT_PATH);

			assertThat(page.title()).isEqualTo(EXPECTED_TITLE);
			assertThat(page.locator("h1").textContent()).contains(EXPECTED_TITLE);

			browser.close();
		}
	}

	@Test
	void answersLucyQuestion() {
		try (Playwright playwright = Playwright.create()) {
			Browser browser = playwright.chromium().launch(
					new BrowserType.LaunchOptions().setHeadless(true)
			);
			Page page = browser.newPage();
			page.navigate("http://localhost:" + port + ROOT_PATH);

			page.fill("#question", QUESTION);
			page.click("button[type='submit']");
			page.waitForSelector(".answer");

			assertThat(page.locator(".answer").textContent()).contains("28");

			browser.close();
		}
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		EmbeddingModel embeddingModel() {
			return new FixedEmbeddingModel();
		}

		@Bean
		@Primary
		ChatModel chatModel() {
			ChatModel chatModel = Mockito.mock(ChatModel.class);
			Mockito.when(chatModel.call(Mockito.anyString())).thenReturn(ANSWER);
			return chatModel;
		}
	}

	private static final class FixedEmbeddingModel implements EmbeddingModel {
		private final float[] embedding;

		private FixedEmbeddingModel() {
			this.embedding = new float[1536];
			this.embedding[0] = 1.0f;
		}

		@Override
		public EmbeddingResponse call(EmbeddingRequest request) {
			List<String> instructions = request.getInstructions();
			List<Embedding> embeddings = new ArrayList<>(instructions.size());
			for (int i = 0; i < instructions.size(); i++) {
				embeddings.add(new Embedding(embedding, i));
			}
			return new EmbeddingResponse(embeddings);
		}

		@Override
		public float[] embed(Document document) {
			return embedding;
		}
	}
}
