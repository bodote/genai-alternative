package de.bas.bodo.genai.web.internal;

import de.bas.bodo.genai.generation.ConversationTurn;
import de.bas.bodo.genai.generation.GenerationHistorySettings;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class SessionConversationHistory {
	private static final String SESSION_KEY = "conversationHistory";

	private final int maxTurns;

	@Autowired
	public SessionConversationHistory(GenerationHistorySettings settings) {
		this(settings.maxTurns());
	}

	public SessionConversationHistory(int maxTurns) {
		this.maxTurns = maxTurns;
	}

	public List<ConversationTurn> history(HttpSession session) {
		Object stored = session.getAttribute(SESSION_KEY);
		if (stored instanceof List<?> list) {
			List<?> raw = list;
			List<ConversationTurn> turns = new ArrayList<>();
			for (Object item : raw) {
				if (item instanceof ConversationTurn turn) {
					turns.add(turn);
				}
			}
			return List.copyOf(turns);
		}
		return List.of();
	}

	public void recordSuccess(HttpSession session, String question, String answer) {
		List<ConversationTurn> updated = new ArrayList<>(history(session));
		updated.add(new ConversationTurn(question, answer));
		if (updated.size() > maxTurns) {
			updated = updated.subList(updated.size() - maxTurns, updated.size());
		}
		session.setAttribute(SESSION_KEY, List.copyOf(updated));
	}
}
