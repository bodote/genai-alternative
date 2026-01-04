package de.bas.bodo.genai.generation;

public record GuardrailResult(boolean allowed, String reason) {
	static GuardrailResult allow() {
		return new GuardrailResult(true, "");
	}

	static GuardrailResult block(String reason) {
		return new GuardrailResult(false, reason);
	}
}
