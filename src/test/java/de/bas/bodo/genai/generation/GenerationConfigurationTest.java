package de.bas.bodo.genai.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.generation.GenerationHistorySettings;
import de.bas.bodo.genai.generation.internal.GenerationConfiguration;
import de.bas.bodo.genai.retrieval.RetrievalGateway;
import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.retrieval.RetrievedChunk;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("GenerationConfiguration")
class GenerationConfigurationTest {
	private static final String QUESTION = "Who is Holmes?";
	private static final String ANSWER = "Holmes is a detective.";
	private static final String GROUNDED_RESPONSE = "GROUNDED";
	private static final String FACT_CHECK_MARKER = "FACT_CHECK";
	private static final int TOP_K = 4;
	private static final RetrievedChunk CONTEXT_CHUNK = new RetrievedChunk(1, 0, ANSWER, 0.9);
	private static final RetrievalResult RETRIEVAL_RESULT = new RetrievalResult(List.of(CONTEXT_CHUNK));
	private static final int HISTORY_MAX_TURNS = 20;

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(GenerationConfiguration.class);

	@Test
	void createsGenerationServiceWithConfiguredTopK() {
		ChatModel chatModel = Mockito.mock(ChatModel.class);
		Mockito.when(chatModel.call(Mockito.anyString())).thenAnswer(invocation -> {
			String prompt = invocation.getArgument(0);
			if (prompt.contains(FACT_CHECK_MARKER)) {
				return GROUNDED_RESPONSE;
			}
			return ANSWER;
		});
		RecordingRetrievalGateway retrievalGateway = new RecordingRetrievalGateway(RETRIEVAL_RESULT);

		contextRunner
				.withPropertyValues("genai.generation.top-k=" + TOP_K)
				.withBean(ChatModel.class, () -> chatModel)
				.withBean(RetrievalGateway.class, () -> retrievalGateway)
				.run(context -> {
					GenerationService service = context.getBean(GenerationService.class);

					GenerationResult result = service.answer(QUESTION);

					assertThat(result.status()).isEqualTo(GenerationStatus.OK);
					assertThat(result.answer()).isEqualTo(ANSWER);
					assertThat(retrievalGateway.recordedTopK()).isEqualTo(TOP_K);
				});
	}

	@Test
	void exposesHistorySettingsFromConfiguration() {
		ChatModel chatModel = Mockito.mock(ChatModel.class);
		Mockito.when(chatModel.call(Mockito.anyString())).thenAnswer(invocation -> {
			String prompt = invocation.getArgument(0);
			if (prompt.contains(FACT_CHECK_MARKER)) {
				return GROUNDED_RESPONSE;
			}
			return ANSWER;
		});
		RecordingRetrievalGateway retrievalGateway = new RecordingRetrievalGateway(RETRIEVAL_RESULT);

		contextRunner
				.withPropertyValues("genai.generation.history-max-turns=" + HISTORY_MAX_TURNS)
				.withBean(ChatModel.class, () -> chatModel)
				.withBean(RetrievalGateway.class, () -> retrievalGateway)
				.run(context -> {
					GenerationHistorySettings settings = context.getBean(GenerationHistorySettings.class);

					assertThat(settings.maxTurns()).isEqualTo(HISTORY_MAX_TURNS);
				});
	}

	private static final class RecordingRetrievalGateway implements RetrievalGateway {
		private final RetrievalResult retrievalResult;
		private int recordedTopK;

		private RecordingRetrievalGateway(RetrievalResult retrievalResult) {
			this.retrievalResult = retrievalResult;
		}

		@Override
		public RetrievalResult retrieve(String query, int topK) {
			this.recordedTopK = topK;
			return retrievalResult;
		}

		private int recordedTopK() {
			return recordedTopK;
		}
	}
}
