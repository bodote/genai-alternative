package de.bas.bodo.genai.web.internal;

import de.bas.bodo.genai.generation.GenerationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class AskController {
	private final GenerationService generationService;

	AskController(GenerationService generationService) {
		this.generationService = generationService;
	}

	@GetMapping("/")
	String landing(Model model) {
		populate(model, "", "", "", "");
		return "index";
	}

	@PostMapping("/ask")
	String ask(@RequestParam("question") String question, Model model) {
		var result = generationService.answer(question);
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
