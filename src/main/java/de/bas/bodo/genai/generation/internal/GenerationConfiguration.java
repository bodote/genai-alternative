package de.bas.bodo.genai.generation.internal;

import de.bas.bodo.genai.generation.GenerationHistorySettings;
import de.bas.bodo.genai.generation.GenerationService;
import de.bas.bodo.genai.retrieval.RetrievalGateway;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GenerationProperties.class)
public class GenerationConfiguration {
	@Bean
	PromptAssembler promptAssembler() {
		return new PromptAssembler();
	}

	@Bean
	InputGuardrail inputGuardrail() {
		return new InputGuardrail();
	}

	@Bean
	OutputGuardrail outputGuardrail() {
		return new OutputGuardrail();
	}

	@Bean
	FactCheckGuardrail factCheckGuardrail(GenerationClient generationClient) {
		return new FactCheckGuardrail(generationClient);
	}

	@Bean
	GenerationClient generationClient(ChatModel chatModel) {
		return chatModel::call;
	}

	@Bean
	GenerationService generationService(
			RetrievalGateway retrievalGateway,
			PromptAssembler promptAssembler,
			InputGuardrail inputGuardrail,
			OutputGuardrail outputGuardrail,
			FactCheckGuardrail factCheckGuardrail,
			GenerationClient generationClient,
			GenerationProperties properties
	) {
		return new GenerationService(
				retrievalGateway,
				promptAssembler,
				inputGuardrail,
				outputGuardrail,
				factCheckGuardrail,
				generationClient,
				properties.getTopK()
		);
	}

	@Bean
	GenerationHistorySettings generationHistorySettings(GenerationProperties properties) {
		return new GenerationHistorySettings(properties.getHistoryMaxTurns());
	}
}
