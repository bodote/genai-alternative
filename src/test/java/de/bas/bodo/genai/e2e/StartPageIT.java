package de.bas.bodo.genai.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import de.bas.bodo.genai.generation.GenerationService;
import de.bas.bodo.genai.ingestion.embedding.EmbeddingClient;
import de.bas.bodo.genai.retrieval.RetrievalStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DisplayName("Start page")
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
				"spring.ai.vectorstore.type=none"
		}
)
@Import(StartPageIT.TestConfig.class)
class StartPageIT {
	private static final String EXPECTED_TITLE = "Sherlock Holmes RAG Assistant";
	private static final String ROOT_PATH = "/";

	@LocalServerPort
	private int port;

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

	@TestConfiguration
	static class TestConfig {
		@Bean
		GenerationService generationService() {
			return Mockito.mock(GenerationService.class);
		}

		@Bean
		EmbeddingClient embeddingClient() {
			return Mockito.mock(EmbeddingClient.class);
		}

		@Bean
		RetrievalStore retrievalStore() {
			return Mockito.mock(RetrievalStore.class);
		}

		@Bean
		VectorStore vectorStore() {
			return Mockito.mock(VectorStore.class);
		}
	}
}
