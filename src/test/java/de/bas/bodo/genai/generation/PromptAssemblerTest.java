package de.bas.bodo.genai.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.generation.testing.GenerationTestFixtures;
import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.generation.internal.PromptAssembler;
import de.bas.bodo.genai.retrieval.RetrievedChunk;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PromptAssembler")
class PromptAssemblerTest {
	private static final String QUESTION = GenerationTestFixtures.QUESTION;
	private static final int WORK_ID = 1661;
	private static final int FIRST_INDEX = 0;
	private static final int SECOND_INDEX = 1;
	private static final String FIRST_TEXT = GenerationTestFixtures.CONTEXT_TEXT;
	private static final String SECOND_TEXT = "Dr. Watson shares lodgings with Holmes.";
	private static final double FIRST_SCORE = 0.91;
	private static final double SECOND_SCORE = 0.88;

	@Nested
	@DisplayName("assemble")
	class Assemble {
		@Test
		void includesQuestionAndContextInOrder() {
			RetrievalResult retrievalResult = new RetrievalResult(List.of(
					new RetrievedChunk(WORK_ID, FIRST_INDEX, FIRST_TEXT, FIRST_SCORE),
					new RetrievedChunk(WORK_ID, SECOND_INDEX, SECOND_TEXT, SECOND_SCORE)
			));
			PromptAssembler assembler = new PromptAssembler();

			String prompt = assembler.assemble(QUESTION, retrievalResult);

			assertThat(prompt).contains(QUESTION);
			assertThat(prompt).contains(FIRST_TEXT).contains(SECOND_TEXT);
			assertThat(prompt.indexOf(FIRST_TEXT)).isLessThan(prompt.indexOf(SECOND_TEXT));
		}
	}
}
