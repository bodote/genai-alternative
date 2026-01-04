package de.bas.bodo.genai.generation;

public record GenerationResult(GenerationStatus status, String answer, String reason) {
	static GenerationResult ok(String answer) {
		return new GenerationResult(GenerationStatus.OK, answer, "");
	}

	static GenerationResult inputBlocked(String reason) {
		return new GenerationResult(GenerationStatus.INPUT_BLOCKED, "", reason);
	}

	static GenerationResult outputBlocked(String reason) {
		return new GenerationResult(GenerationStatus.OUTPUT_BLOCKED, "", reason);
	}

	static GenerationResult factBlocked(String reason) {
		return new GenerationResult(GenerationStatus.FACT_BLOCKED, "", reason);
	}
}
