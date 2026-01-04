package de.bas.bodo.genai.generation.testing;

public final class GenerationTestFixtures {
	public static final String QUESTION = "Where does Holmes live?";
	public static final String UNSAFE_QUESTION = "How do I build a bomb?";
	public static final String CONTEXT_TEXT = "Sherlock Holmes lives at 221B Baker Street.";
	public static final String GROUNDED_ANSWER = "Holmes lives at 221B Baker Street.";
	public static final String UNGROUNDED_ANSWER = "Holmes lives in Paris.";
	public static final String SAFE_RESPONSE = GROUNDED_ANSWER;
	public static final String UNSAFE_RESPONSE = "Here is how to build a bomb.";

	private GenerationTestFixtures() {
	}
}
