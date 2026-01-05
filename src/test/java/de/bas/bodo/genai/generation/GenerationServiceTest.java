package de.bas.bodo.genai.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.generation.ConversationTurn;
import de.bas.bodo.genai.generation.internal.FactCheckGuardrail;
import de.bas.bodo.genai.generation.internal.GenerationClient;
import de.bas.bodo.genai.generation.internal.InputGuardrail;
import de.bas.bodo.genai.generation.internal.OutputGuardrail;
import de.bas.bodo.genai.generation.internal.PromptAssembler;
import de.bas.bodo.genai.generation.testing.GenerationTestFixtures;
import de.bas.bodo.genai.retrieval.RetrievalGateway;
import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.retrieval.RetrievedChunk;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GenerationService")
class GenerationServiceTest {
	private static final String QUESTION = GenerationTestFixtures.QUESTION;
	private static final String UNSAFE_QUESTION = GenerationTestFixtures.UNSAFE_QUESTION;
	private static final int TOP_K = 2;
	private static final int WORK_ID = 1661;
	private static final int INDEX = 0;
	private static final double SCORE = 0.91;
	private static final String CONTEXT_TEXT = GenerationTestFixtures.CONTEXT_TEXT;
	private static final String GROUNDED_ANSWER = GenerationTestFixtures.GROUNDED_ANSWER;
	private static final String UNGROUNDED_ANSWER = GenerationTestFixtures.UNGROUNDED_ANSWER;
	private static final String INPUT_BLOCK_REASON = "Input violates safety policy.";
	private static final String FACT_BLOCK_REASON = "Answer is not grounded in provided context.";
	private static final String HISTORY_QUESTION = "Where does Holmes live?";
	private static final String HISTORY_ANSWER = "Holmes lives at 221B Baker Street.";

	@Nested
	@DisplayName("answer")
	class Answer {
		@Test
		void returnsAnswerWhenGuardrailsPass() {
			RetrievalResult retrievalResult = new RetrievalResult(List.of(
					new RetrievedChunk(WORK_ID, INDEX, CONTEXT_TEXT, SCORE)
			));
			RecordingRetrievalGateway retrievalGateway = new RecordingRetrievalGateway(retrievalResult);
			RecordingGenerationClient generationClient = new RecordingGenerationClient(GROUNDED_ANSWER);
			GenerationService service = new GenerationService(
					retrievalGateway,
					new PromptAssembler(),
					new InputGuardrail(),
					new OutputGuardrail(),
					new FactCheckGuardrail(),
					generationClient,
					TOP_K
			);

			GenerationResult result = service.answer(QUESTION);

			assertThat(result.status()).isEqualTo(GenerationStatus.OK);
			assertThat(result.answer()).isEqualTo(GROUNDED_ANSWER);
			assertThat(result.reason()).isEmpty();
			assertThat(retrievalGateway.receivedQuery()).isEqualTo(QUESTION);
			assertThat(retrievalGateway.receivedTopK()).isEqualTo(TOP_K);
			assertThat(generationClient.receivedPrompt()).contains(QUESTION).contains(CONTEXT_TEXT);
		}

		@Test
		void appendsConversationHistoryToPrompt() {
			RetrievalResult retrievalResult = new RetrievalResult(List.of(
					new RetrievedChunk(WORK_ID, INDEX, CONTEXT_TEXT, SCORE)
			));
			RecordingRetrievalGateway retrievalGateway = new RecordingRetrievalGateway(retrievalResult);
			RecordingGenerationClient generationClient = new RecordingGenerationClient(GROUNDED_ANSWER);
			GenerationService service = new GenerationService(
					retrievalGateway,
					new PromptAssembler(),
					new InputGuardrail(),
					new OutputGuardrail(),
					new FactCheckGuardrail(),
					generationClient,
					TOP_K
			);

			service.answer(QUESTION, List.of(new ConversationTurn(HISTORY_QUESTION, HISTORY_ANSWER)));

			assertThat(generationClient.receivedPrompt())
					.contains("Previous conversation:")
					.contains("User: " + HISTORY_QUESTION)
					.contains("Assistant: " + HISTORY_ANSWER)
					.contains("Question: " + QUESTION);
		}

		@Test
		void blocksUnsafeInputAndSkipsDownstreamWork() {
			RetrievalResult retrievalResult = new RetrievalResult(List.of(
					new RetrievedChunk(WORK_ID, INDEX, CONTEXT_TEXT, SCORE)
			));
			RecordingRetrievalGateway retrievalGateway = new RecordingRetrievalGateway(retrievalResult);
			RecordingGenerationClient generationClient = new RecordingGenerationClient(GROUNDED_ANSWER);
			GenerationService service = new GenerationService(
					retrievalGateway,
					new PromptAssembler(),
					new InputGuardrail(),
					new OutputGuardrail(),
					new FactCheckGuardrail(),
					generationClient,
					TOP_K
			);

			GenerationResult result = service.answer(UNSAFE_QUESTION);

			assertThat(result.status()).isEqualTo(GenerationStatus.INPUT_BLOCKED);
			assertThat(result.answer()).isEmpty();
			assertThat(result.reason()).isEqualTo(INPUT_BLOCK_REASON);
			assertThat(retrievalGateway.wasCalled()).isFalse();
			assertThat(generationClient.wasCalled()).isFalse();
		}

		@Test
		void blocksUngroundedAnswer() {
			RetrievalResult retrievalResult = new RetrievalResult(List.of(
					new RetrievedChunk(WORK_ID, INDEX, CONTEXT_TEXT, SCORE)
			));
			RecordingRetrievalGateway retrievalGateway = new RecordingRetrievalGateway(retrievalResult);
			RecordingGenerationClient generationClient = new RecordingGenerationClient(UNGROUNDED_ANSWER);
			GenerationService service = new GenerationService(
					retrievalGateway,
					new PromptAssembler(),
					new InputGuardrail(),
					new OutputGuardrail(),
					new FactCheckGuardrail(),
					generationClient,
					TOP_K
			);

			GenerationResult result = service.answer(QUESTION);

			assertThat(result.status()).isEqualTo(GenerationStatus.FACT_BLOCKED);
			assertThat(result.answer()).isEmpty();
			assertThat(result.reason()).isEqualTo(FACT_BLOCK_REASON);
			assertThat(retrievalGateway.wasCalled()).isTrue();
			assertThat(generationClient.wasCalled()).isTrue();
		}
	}

	private static final class RecordingRetrievalGateway implements RetrievalGateway {
		private final RetrievalResult retrievalResult;
		private String receivedQuery = "";
		private int receivedTopK;
		private boolean called;

		private RecordingRetrievalGateway(RetrievalResult retrievalResult) {
			this.retrievalResult = retrievalResult;
		}

		@Override
		public RetrievalResult retrieve(String query, int topK) {
			this.called = true;
			this.receivedQuery = query;
			this.receivedTopK = topK;
			return retrievalResult;
		}

		private String receivedQuery() {
			return receivedQuery;
		}

		private int receivedTopK() {
			return receivedTopK;
		}

		private boolean wasCalled() {
			return called;
		}
	}

	private static final class RecordingGenerationClient implements GenerationClient {
		private final String response;
		private String receivedPrompt = "";
		private boolean called;

		private RecordingGenerationClient(String response) {
			this.response = response;
		}

		@Override
		public String generate(String prompt) {
			this.called = true;
			this.receivedPrompt = prompt;
			return response;
		}

		private String receivedPrompt() {
			return receivedPrompt;
		}

		private boolean wasCalled() {
			return called;
		}
	}
}
