package de.bas.bodo.genai.generation;

import de.bas.bodo.genai.retrieval.RetrievalGateway;
import de.bas.bodo.genai.retrieval.RetrievalResult;
import de.bas.bodo.genai.generation.internal.FactCheckGuardrail;
import de.bas.bodo.genai.generation.internal.GenerationClient;
import de.bas.bodo.genai.generation.internal.GuardrailResult;
import de.bas.bodo.genai.generation.internal.InputGuardrail;
import de.bas.bodo.genai.generation.internal.OutputGuardrail;
import de.bas.bodo.genai.generation.internal.PromptAssembler;
import java.util.List;

public class GenerationService {
	private final RetrievalGateway retrievalGateway;
	private final PromptAssembler promptAssembler;
	private final InputGuardrail inputGuardrail;
	private final OutputGuardrail outputGuardrail;
	private final FactCheckGuardrail factCheckGuardrail;
	private final GenerationClient generationClient;
	private final int topK;

	public GenerationService(
			RetrievalGateway retrievalGateway,
			PromptAssembler promptAssembler,
			InputGuardrail inputGuardrail,
			OutputGuardrail outputGuardrail,
			FactCheckGuardrail factCheckGuardrail,
			GenerationClient generationClient,
			int topK
	) {
		this.retrievalGateway = retrievalGateway;
		this.promptAssembler = promptAssembler;
		this.inputGuardrail = inputGuardrail;
		this.outputGuardrail = outputGuardrail;
		this.factCheckGuardrail = factCheckGuardrail;
		this.generationClient = generationClient;
		this.topK = topK;
	}

	public GenerationResult answer(String question) {
		return answer(question, List.of());
	}

	public GenerationResult answer(String question, List<ConversationTurn> history) {
		GuardrailResult inputResult = inputGuardrail.validate(question);
		if (!inputResult.allowed()) {
			return GenerationResult.inputBlocked(inputResult.reason());
		}
		RetrievalResult retrievalResult = retrievalGateway.retrieve(question, topK);
		String prompt = promptAssembler.assemble(question, retrievalResult, history);
		String response = generationClient.generate(prompt);
		GuardrailResult outputResult = outputGuardrail.validate(response);
		if (!outputResult.allowed()) {
			return GenerationResult.outputBlocked(outputResult.reason());
		}
		GuardrailResult factResult = factCheckGuardrail.validate(response, retrievalResult);
		if (!factResult.allowed()) {
			return GenerationResult.factBlocked(factResult.reason());
		}
		return GenerationResult.ok(response);
	}
}
