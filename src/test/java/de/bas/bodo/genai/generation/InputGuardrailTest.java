package de.bas.bodo.genai.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.generation.testing.GenerationTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("InputGuardrail")
class InputGuardrailTest {
	private static final String SAFE_QUERY = GenerationTestFixtures.QUESTION;
	private static final String BLANK_QUERY = "  \n\t ";
	private static final String UNSAFE_QUERY = GenerationTestFixtures.UNSAFE_QUESTION;
	private static final String BLANK_REASON = "Input is blank.";
	private static final String UNSAFE_REASON = "Input violates safety policy.";

	@Nested
	@DisplayName("validate")
	class Validate {
		@Test
		void allowsSafeInput() {
			InputGuardrail guardrail = new InputGuardrail();

			GuardrailResult result = guardrail.validate(SAFE_QUERY);

			assertThat(result.allowed()).isTrue();
			assertThat(result.reason()).isEmpty();
		}

		@Test
		void rejectsBlankInput() {
			InputGuardrail guardrail = new InputGuardrail();

			GuardrailResult result = guardrail.validate(BLANK_QUERY);

			assertThat(result.allowed()).isFalse();
			assertThat(result.reason()).isEqualTo(BLANK_REASON);
		}

		@Test
		void rejectsUnsafeInput() {
			InputGuardrail guardrail = new InputGuardrail();

			GuardrailResult result = guardrail.validate(UNSAFE_QUERY);

			assertThat(result.allowed()).isFalse();
			assertThat(result.reason()).isEqualTo(UNSAFE_REASON);
		}
	}
}
