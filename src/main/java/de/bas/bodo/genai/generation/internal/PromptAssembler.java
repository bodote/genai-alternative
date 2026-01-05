package de.bas.bodo.genai.generation.internal;

import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.retrieval.RetrievedChunk;

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
}
