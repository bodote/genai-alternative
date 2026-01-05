package de.bas.bodo.genai.generation.internal;

public class InputGuardrail {
	private static final String BLANK_REASON = "Input is blank.";
	private static final String UNSAFE_REASON = "Input violates safety policy.";
	private static final org.slf4j.Logger logger =
			org.slf4j.LoggerFactory.getLogger(InputGuardrail.class);

	public GuardrailResult validate(String input) {
		if (input == null || input.trim().isEmpty()) {
			logger.info("Input guardrail blocked request: {}", BLANK_REASON);
			return GuardrailResult.block(BLANK_REASON);
		}
		String normalized = input.toLowerCase(java.util.Locale.ROOT);
		if (normalized.contains("bomb")) {
			logger.info("Input guardrail blocked request: {}", UNSAFE_REASON);
			return GuardrailResult.block(UNSAFE_REASON);
		}
		logger.debug("Input guardrail allowed request (length={}).", input.length());
		return GuardrailResult.allow();
	}
}
