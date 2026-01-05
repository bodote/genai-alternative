package de.bas.bodo.genai.generation.internal;

public class OutputGuardrail {
	private static final String UNSAFE_REASON = "Output violates safety policy.";

	public GuardrailResult validate(String response) {
		String normalized = response.toLowerCase(java.util.Locale.ROOT);
		if (normalized.contains("bomb")) {
			return GuardrailResult.block(UNSAFE_REASON);
		}
		return GuardrailResult.allow();
	}
}
