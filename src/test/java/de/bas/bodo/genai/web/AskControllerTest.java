package de.bas.bodo.genai.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import de.bas.bodo.genai.generation.GenerationResult;
import de.bas.bodo.genai.generation.GenerationService;
import de.bas.bodo.genai.generation.testing.GenerationTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("AskController")
@WebMvcTest(AskController.class)
class AskControllerTest {
	private static final String QUESTION = GenerationTestFixtures.QUESTION;
	private static final String ANSWER = GenerationTestFixtures.GROUNDED_ANSWER;
	private static final String BLOCK_REASON = "Input violates safety policy.";
	private static final String VIEW_NAME = "index";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GenerationService generationService;

	@Test
	void rendersLandingPage() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name(VIEW_NAME));
	}

	@Test
	void returnsAnswerPayloadForSuccessfulResponse() throws Exception {
		when(generationService.answer(QUESTION)).thenReturn(GenerationResult.ok(ANSWER));

		mockMvc.perform(post("/ask")
					.param("question", QUESTION))
				.andExpect(status().isOk())
				.andExpect(view().name(VIEW_NAME))
				.andExpect(model().attribute("status", "OK"))
				.andExpect(model().attribute("answer", ANSWER))
				.andExpect(model().attribute("reason", ""))
				.andExpect(model().attribute("question", QUESTION));
	}

	@Test
	void returnsGuardrailStatusWhenBlocked() throws Exception {
		when(generationService.answer(QUESTION)).thenReturn(GenerationResult.inputBlocked(BLOCK_REASON));

		mockMvc.perform(post("/ask")
					.param("question", QUESTION))
				.andExpect(status().isOk())
				.andExpect(view().name(VIEW_NAME))
				.andExpect(model().attribute("status", "INPUT_BLOCKED"))
				.andExpect(model().attribute("answer", ""))
				.andExpect(model().attribute("reason", BLOCK_REASON))
				.andExpect(model().attribute("question", QUESTION));
	}
}
