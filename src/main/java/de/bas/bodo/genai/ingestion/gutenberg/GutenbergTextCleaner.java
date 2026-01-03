package de.bas.bodo.genai.ingestion.gutenberg;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class GutenbergTextCleaner {
	private static final String START_PREFIX = "*** START OF THE PROJECT GUTENBERG EBOOK";
	private static final String END_PREFIX = "*** END OF THE PROJECT GUTENBERG EBOOK";

	public String stripBoilerplate(String rawText) {
		String[] lines = rawText.split("\n", -1);
		List<String> bodyLines = new ArrayList<>();
		boolean inBody = false;
		boolean foundStart = false;

		for (String line : lines) {
			if (line.startsWith(START_PREFIX)) {
				inBody = true;
				foundStart = true;
				continue;
			}
			if (line.startsWith(END_PREFIX)) {
				break;
			}
			if (inBody) {
				bodyLines.add(line);
			}
		}

		if (!foundStart) {
			return rawText;
		}

		return String.join("\n", bodyLines).trim();
	}

	public String normalizeWhitespace(String text) {
		String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
		normalized = normalized.replace("\t", " ");
		normalized = normalized.replaceAll("[ ]{2,}", " ");
		String[] lines = normalized.split("\n", -1);
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
		}
		normalized = String.join("\n", lines);
		normalized = normalized.replaceAll("\\n{3,}", "\n\n");
		return normalized.trim();
	}
}
