package de.bas.bodo.genai.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.generation.testing.GenerationTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OutputGuardrail")
class OutputGuardrailTest {
	private static final String SAFE_RESPONSE = GenerationTestFixtures.SAFE_RESPONSE;
	private static final String UNSAFE_RESPONSE = GenerationTestFixtures.UNSAFE_RESPONSE;
	private static final String UNSAFE_REASON = "Output violates safety policy.";

	@Nested
	@DisplayName("validate")
	class Validate {
		@Test
		void allowsSafeResponse() {
			OutputGuardrail guardrail = new OutputGuardrail();

			GuardrailResult result = guardrail.validate(SAFE_RESPONSE);

			assertThat(result.allowed()).isTrue();
			assertThat(result.reason()).isEmpty();
		}

		@Test
		void rejectsUnsafeResponse() {
			OutputGuardrail guardrail = new OutputGuardrail();

			GuardrailResult result = guardrail.validate(UNSAFE_RESPONSE);

			assertThat(result.allowed()).isFalse();
			assertThat(result.reason()).isEqualTo(UNSAFE_REASON);
		}
	}
}
