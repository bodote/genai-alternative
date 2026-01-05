package de.bas.bodo.genai.web.internal;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.generation.ConversationTurn;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

@DisplayName("SessionConversationHistory")
class SessionConversationHistoryTest {
	private static final int MAX_TURNS = 20;
	private static final String FIRST_QUESTION = "Where does Holmes live?";
	private static final String FIRST_ANSWER = "Holmes lives at 221B Baker Street.";
	private static final String SECOND_QUESTION = "Who is Watson?";
	private static final String SECOND_ANSWER = "Watson is Holmes' friend and companion.";

	@Nested
	@DisplayName("recordSuccess")
	class RecordSuccess {
		@Test
		void storesConversationTurnsInSessionOrder() {
			SessionConversationHistory history = new SessionConversationHistory(MAX_TURNS);
			MockHttpSession session = new MockHttpSession();

			history.recordSuccess(session, FIRST_QUESTION, FIRST_ANSWER);
			history.recordSuccess(session, SECOND_QUESTION, SECOND_ANSWER);

			assertThat(history.history(session)).containsExactly(
					new ConversationTurn(FIRST_QUESTION, FIRST_ANSWER),
					new ConversationTurn(SECOND_QUESTION, SECOND_ANSWER)
			);
		}

		@Test
		void trimsHistoryToMaxTurnsKeepingMostRecent() {
			SessionConversationHistory history = new SessionConversationHistory(MAX_TURNS);
			MockHttpSession session = new MockHttpSession();
			for (int i = 0; i < MAX_TURNS; i++) {
				history.recordSuccess(session, "Q" + i, "A" + i);
			}

			history.recordSuccess(session, FIRST_QUESTION, FIRST_ANSWER);

			List<ConversationTurn> updated = history.history(session);
			assertThat(updated).hasSize(MAX_TURNS);
			assertThat(updated.getFirst()).isEqualTo(new ConversationTurn("Q1", "A1"));
			assertThat(updated.getLast()).isEqualTo(new ConversationTurn(FIRST_QUESTION, FIRST_ANSWER));
		}
	}

	@Nested
	@DisplayName("history")
	class History {
		@Test
		void returnsEmptyHistoryWhenSessionHasNone() {
			SessionConversationHistory history = new SessionConversationHistory(MAX_TURNS);
			MockHttpSession session = new MockHttpSession();

			assertThat(history.history(session)).isEmpty();
		}
	}
}
