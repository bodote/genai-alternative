package de.bas.bodo.genai.generation.internal;

import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.retrieval.RetrievedChunk;
import java.util.stream.Collectors;

public class FactCheckGuardrail {
	private static final String UNGROUNDED_REASON = "Answer is not grounded in provided context.";
	private static final String FACT_CHECK_MARKER = "FACT_CHECK";
	private static final org.slf4j.Logger logger =
			org.slf4j.LoggerFactory.getLogger(FactCheckGuardrail.class);
	private final GenerationClient generationClient;

	public FactCheckGuardrail(GenerationClient generationClient) {
		this.generationClient = generationClient;
	}

	public GuardrailResult validate(String answer, RetrievalResult retrievalResult) {
		logger.debug(
				"Fact-check guardrail evaluating answer (length={}) against {} chunks.",
				answer.length(),
				retrievalResult.chunks().size()
		);
		String context = buildContext(retrievalResult);
		String prompt = """
				%s
				You are a fact-checking guardrail for a RAG system.
				Decide whether the ANSWER is fully supported by the CONTEXT.
				Return only GROUNDED or UNGROUNDED.

				CONTEXT:
				%s

				ANSWER:
				%s
				""".formatted(FACT_CHECK_MARKER, context, answer);
		logger.info("Fact-check prompt:\n{}", prompt);
		String response = generationClient.generate(prompt);
		logger.info("Fact-check response:\n{}", response);
		String verdict = normalizeVerdict(response);
		if (verdict.contains("UNGROUNDED")) {
			logger.info("Fact-check guardrail blocked response: {} (chunks={}).",
					UNGROUNDED_REASON,
					retrievalResult.chunks().size()
			);
			return GuardrailResult.block(UNGROUNDED_REASON);
		}
		if (verdict.contains("GROUNDED")) {
			RetrievedChunk firstChunk = retrievalResult.chunks().stream().findFirst().orElse(null);
			if (firstChunk != null) {
				logger.info(
						"Fact-check guardrail allowed response using workId={}, index={}, score={}.",
						firstChunk.workId(),
						firstChunk.index(),
						firstChunk.score()
				);
			} else {
				logger.info("Fact-check guardrail allowed response with empty context.");
			}
			return GuardrailResult.allow();
		}
		logger.info("Fact-check guardrail blocked response due to invalid verdict (chunks={}).",
				retrievalResult.chunks().size()
		);
		return GuardrailResult.block(UNGROUNDED_REASON);
	}

	private static String buildContext(RetrievalResult retrievalResult) {
		return retrievalResult.chunks().stream()
				.map(RetrievedChunk::text)
				.collect(Collectors.joining("\n- ", "- ", ""));
	}

	private static String normalizeVerdict(String text) {
		String normalized = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);
		return normalized.replaceAll("[^A-Z]", "");
	}
}
