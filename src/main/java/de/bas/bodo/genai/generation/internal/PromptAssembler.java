package de.bas.bodo.genai.generation.internal;

import de.bas.bodo.genai.generation.ConversationTurn;
import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.retrieval.RetrievedChunk;
import java.util.List;

public class PromptAssembler {
	public String assemble(String question, RetrievalResult retrievalResult) {
		StringBuilder builder = new StringBuilder();
		builder.append("Question: ").append(question).append('\n');
		builder.append("Context:\n");
		for (RetrievedChunk chunk : retrievalResult.chunks()) {
			builder.append("- ").append(chunk.text()).append('\n');
		}
		return builder.toString();
	}

	public String assemble(String question, RetrievalResult retrievalResult, List<ConversationTurn> history) {
		StringBuilder builder = new StringBuilder();
		if (!history.isEmpty()) {
			builder.append("Previous conversation:\n");
			for (ConversationTurn turn : history) {
				builder.append("User: ").append(turn.question()).append('\n');
				builder.append("Assistant: ").append(turn.answer()).append('\n');
			}
		}
		builder.append("Question: ").append(question).append('\n');
		builder.append("Context:\n");
		for (RetrievedChunk chunk : retrievalResult.chunks()) {
			builder.append("- ").append(chunk.text()).append('\n');
		}
		return builder.toString();
	}
}
