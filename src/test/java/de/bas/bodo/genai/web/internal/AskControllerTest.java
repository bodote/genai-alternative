package de.bas.bodo.genai.web.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import de.bas.bodo.genai.generation.ConversationTurn;
import de.bas.bodo.genai.generation.GenerationHistorySettings;
import de.bas.bodo.genai.generation.GenerationResult;
import de.bas.bodo.genai.generation.GenerationService;
import de.bas.bodo.genai.generation.testing.GenerationTestFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("AskController")
@WebMvcTest(AskController.class)
@Import(AskControllerTest.TestConfig.class)
class AskControllerTest {
	private static final String QUESTION = GenerationTestFixtures.QUESTION;
	private static final String ANSWER = GenerationTestFixtures.GROUNDED_ANSWER;
	private static final String BLOCK_REASON = "Input violates safety policy.";
	private static final String VIEW_NAME = "index";
	private static final int HISTORY_MAX_TURNS = 20;
	private static final String FIRST_QUESTION = "Who is Sherlock Holmes?";
	private static final String FIRST_ANSWER = "Sherlock Holmes is a detective.";
	private static final String FOLLOW_UP_QUESTION = GenerationTestFixtures.QUESTION;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SessionConversationHistory conversationHistory;

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
		when(generationService.answer(eq(QUESTION), anyList())).thenReturn(GenerationResult.ok(ANSWER));
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/ask")
					.session(session)
					.param("question", QUESTION))
				.andExpect(status().isOk())
				.andExpect(view().name(VIEW_NAME))
				.andExpect(model().attribute("status", "OK"))
				.andExpect(model().attribute("answer", ANSWER))
				.andExpect(model().attribute("reason", ""))
				.andExpect(model().attribute("question", QUESTION));

		assertThat(conversationHistory.history(session)).containsExactly(
				new ConversationTurn(QUESTION, ANSWER)
		);
	}

	@Test
	void returnsGuardrailStatusWhenBlocked() throws Exception {
		when(generationService.answer(eq(QUESTION), anyList())).thenReturn(GenerationResult.inputBlocked(BLOCK_REASON));
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/ask")
					.session(session)
					.param("question", QUESTION))
				.andExpect(status().isOk())
				.andExpect(view().name(VIEW_NAME))
				.andExpect(model().attribute("status", "INPUT_BLOCKED"))
				.andExpect(model().attribute("answer", ""))
				.andExpect(model().attribute("reason", BLOCK_REASON))
				.andExpect(model().attribute("question", QUESTION));

		assertThat(conversationHistory.history(session)).isEmpty();
	}

	@Test
	void keepsPreviousSuccessfulAnswersInHistoryAfterBlockedAttempt() throws Exception {
		when(generationService.answer(eq(FIRST_QUESTION), anyList())).thenReturn(GenerationResult.ok(FIRST_ANSWER));
		when(generationService.answer(eq(FOLLOW_UP_QUESTION), anyList()))
				.thenReturn(GenerationResult.inputBlocked(BLOCK_REASON));
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/ask")
					.session(session)
					.param("question", FIRST_QUESTION))
				.andExpect(status().isOk());

		mockMvc.perform(post("/ask")
					.session(session)
					.param("question", FOLLOW_UP_QUESTION))
				.andExpect(status().isOk())
				.andExpect(view().name(VIEW_NAME))
				.andExpect(model().attribute("status", "INPUT_BLOCKED"))
				.andExpect(model().attribute("answer", ""))
				.andExpect(model().attribute("reason", BLOCK_REASON))
				.andExpect(model().attribute("question", FOLLOW_UP_QUESTION))
				.andExpect(model().attribute("history", List.of(new ConversationTurn(FIRST_QUESTION, FIRST_ANSWER))));
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		GenerationHistorySettings generationHistorySettings() {
			return new GenerationHistorySettings(HISTORY_MAX_TURNS);
		}

		@Bean
		SessionConversationHistory sessionConversationHistory(GenerationHistorySettings settings) {
			return new SessionConversationHistory(settings.maxTurns());
		}
	}
}
