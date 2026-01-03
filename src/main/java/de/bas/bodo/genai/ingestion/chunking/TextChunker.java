package de.bas.bodo.genai.ingestion.chunking;

import java.util.ArrayList;
import java.util.List;

public final class TextChunker {
	public List<String> chunk(String text, int maxLength, int overlap) {
		if (text.isEmpty()) {
			return List.of();
		}
		if (maxLength <= 0) {
			throw new IllegalArgumentException("maxLength must be positive");
		}
		if (overlap < 0 || overlap >= maxLength) {
			throw new IllegalArgumentException("overlap must be between 0 and maxLength - 1");
		}

		List<String> chunks = new ArrayList<>();
		int start = 0;
		int length = text.length();
		while (start < length) {
			int end = Math.min(start + maxLength, length);
			chunks.add(text.substring(start, end));
			if (end == length) {
				break;
			}
			start = end - overlap;
		}
		return List.copyOf(chunks);
	}

	public List<String> chunkRecursively(String text, int maxLength, int overlap) {
		String trimmed = text.trim();
		if (trimmed.isEmpty()) {
			return List.of();
		}
		if (trimmed.length() <= maxLength) {
			return List.of(trimmed);
		}

		List<String> result = new ArrayList<>();
		List<String> paragraphs = splitOnRegex(trimmed, "\\n\\n+");
		for (String paragraph : paragraphs) {
			if (paragraph.length() <= maxLength) {
				result.add(paragraph);
				continue;
			}
			List<String> sentences = splitOnRegex(paragraph, "(?<=[.!?])\\s+");
			for (String sentence : sentences) {
				if (sentence.length() <= maxLength) {
					result.add(sentence);
				} else {
					result.addAll(chunkWords(sentence, maxLength, overlap));
				}
			}
		}
		return List.copyOf(result);
	}

	private static List<String> splitOnRegex(String text, String regex) {
		String[] parts = java.util.regex.Pattern.compile(regex).split(text);
		List<String> result = new ArrayList<>();
		for (String part : parts) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				result.add(trimmed);
			}
		}
		return result;
	}

	private static List<String> chunkWords(String text, int maxLength, int overlap) {
		String[] words = java.util.regex.Pattern.compile("\\s+").split(text.trim());
		List<String> chunks = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (String word : words) {
			if (current.length() == 0) {
				current.append(word);
				continue;
			}
			if (current.length() + 1 + word.length() <= maxLength) {
				current.append(' ').append(word);
			} else {
				chunks.add(current.toString());
				current.setLength(0);
				current.append(word);
			}
		}
		if (current.length() > 0) {
			chunks.add(current.toString());
		}
		if (overlap <= 0 || chunks.size() <= 1) {
			return List.copyOf(chunks);
		}
		return applyOverlap(chunks, overlap);
	}

	private static List<String> applyOverlap(List<String> chunks, int overlap) {
		List<String> overlapped = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i++) {
			if (i == 0) {
				overlapped.add(chunks.get(i));
				continue;
			}
			String previous = chunks.get(i - 1);
			String current = chunks.get(i);
			String[] previousWords = java.util.regex.Pattern.compile("\\s+").split(previous);
			int start = Math.max(0, previousWords.length - overlap);
			StringBuilder builder = new StringBuilder();
			for (int j = start; j < previousWords.length; j++) {
				if (builder.length() > 0) {
					builder.append(' ');
				}
				builder.append(previousWords[j]);
			}
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(current);
			overlapped.add(builder.toString());
		}
		return List.copyOf(overlapped);
	}
}
