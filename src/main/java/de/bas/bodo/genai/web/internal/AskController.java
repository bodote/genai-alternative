package de.bas.bodo.genai.web.internal;

import de.bas.bodo.genai.generation.GenerationResult;
import de.bas.bodo.genai.generation.GenerationService;
import de.bas.bodo.genai.generation.GenerationStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class AskController {
	private final GenerationService generationService;
	private final SessionConversationHistory conversationHistory;

	AskController(GenerationService generationService, SessionConversationHistory conversationHistory) {
		this.generationService = generationService;
		this.conversationHistory = conversationHistory;
	}

	@GetMapping("/")
	String landing(Model model) {
		populate(model, "", "", "", "");
		return "index";
	}

	@PostMapping("/ask")
	String ask(@RequestParam("question") String question, HttpSession session, Model model) {
		var history = conversationHistory.history(session);
		GenerationResult result = generationService.answer(question, history);
		if (result.status() == GenerationStatus.OK) {
			conversationHistory.recordSuccess(session, question, result.answer());
		}
		populate(model, question, result.status().name(), result.answer(), result.reason());
		return "index";
	}

	private void populate(Model model, String question, String status, String answer, String reason) {
		model.addAttribute("question", question);
		model.addAttribute("status", status);
		model.addAttribute("answer", answer);
		model.addAttribute("reason", reason);
	}
}
