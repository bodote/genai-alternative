package de.bas.bodo.genai.generation.internal;

public class InputGuardrail {
	private static final String BLANK_REASON = "Input is blank.";
	private static final String UNSAFE_REASON = "Input violates safety policy.";

	public GuardrailResult validate(String input) {
		if (input == null || input.trim().isEmpty()) {
			return GuardrailResult.block(BLANK_REASON);
		}
		String normalized = input.toLowerCase(java.util.Locale.ROOT);
		if (normalized.contains("bomb")) {
			return GuardrailResult.block(UNSAFE_REASON);
		}
		return GuardrailResult.allow();
	}
}
