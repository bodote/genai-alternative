package de.bas.bodo.genai.generation;

import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.retrieval.RetrievedChunk;

public class FactCheckGuardrail {
	private static final String UNGROUNDED_REASON = "Answer is not grounded in provided context.";

	public GuardrailResult validate(String answer, RetrievalResult retrievalResult) {
		String normalizedAnswer = normalize(answer);
		for (RetrievedChunk chunk : retrievalResult.chunks()) {
			String normalizedContext = normalize(chunk.text());
			if (normalizedContext.contains(normalizedAnswer)) {
				return GuardrailResult.allow();
			}
		}
		return GuardrailResult.block(UNGROUNDED_REASON);
	}

	private static String normalize(String text) {
		String normalized = text.toLowerCase(java.util.Locale.ROOT);
		return normalized.replaceAll("[^a-z0-9 ]", "");
	}
}
