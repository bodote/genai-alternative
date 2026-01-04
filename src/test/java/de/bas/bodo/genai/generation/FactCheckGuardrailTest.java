package de.bas.bodo.genai.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.generation.testing.GenerationTestFixtures;
import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.retrieval.RetrievedChunk;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FactCheckGuardrail")
class FactCheckGuardrailTest {
	private static final int WORK_ID = 1661;
	private static final int INDEX = 0;
	private static final double SCORE = 0.91;
	private static final String CONTEXT_TEXT = "Sherlock Holmes lives at 221B Baker Street in London.";
	private static final String GROUNDED_ANSWER = GenerationTestFixtures.GROUNDED_ANSWER;
	private static final String UNGROUNDED_ANSWER = GenerationTestFixtures.UNGROUNDED_ANSWER;
	private static final String UNGROUNDED_REASON = "Answer is not grounded in provided context.";

	@Nested
	@DisplayName("validate")
	class Validate {
		@Test
		void allowsGroundedAnswer() {
			FactCheckGuardrail guardrail = new FactCheckGuardrail();
			RetrievalResult retrievalResult = new RetrievalResult(List.of(
					new RetrievedChunk(WORK_ID, INDEX, CONTEXT_TEXT, SCORE)
			));

			GuardrailResult result = guardrail.validate(GROUNDED_ANSWER, retrievalResult);

			assertThat(result.allowed()).isTrue();
			assertThat(result.reason()).isEmpty();
		}

		@Test
		void rejectsUngroundedAnswer() {
			FactCheckGuardrail guardrail = new FactCheckGuardrail();
			RetrievalResult retrievalResult = new RetrievalResult(List.of(
					new RetrievedChunk(WORK_ID, INDEX, CONTEXT_TEXT, SCORE)
			));

			GuardrailResult result = guardrail.validate(UNGROUNDED_ANSWER, retrievalResult);

			assertThat(result.allowed()).isFalse();
			assertThat(result.reason()).isEqualTo(UNGROUNDED_REASON);
		}
	}
}
