package de.bas.bodo.genai.generation.internal;

public class OutputGuardrail {
	private static final String UNSAFE_REASON = "Output violates safety policy.";
	private static final org.slf4j.Logger logger =
			org.slf4j.LoggerFactory.getLogger(OutputGuardrail.class);

	public GuardrailResult validate(String response) {
		String normalized = response.toLowerCase(java.util.Locale.ROOT);
		if (normalized.contains("bomb")) {
			logger.info("Output guardrail blocked response: {}", UNSAFE_REASON);
			return GuardrailResult.block(UNSAFE_REASON);
		}
		logger.debug("Output guardrail allowed response (length={}).", response.length());
		return GuardrailResult.allow();
	}
}
