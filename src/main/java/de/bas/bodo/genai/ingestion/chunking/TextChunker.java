package de.bas.bodo.genai.ingestion.chunking;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class TextChunker {
	private static final Logger logger = LoggerFactory.getLogger(TextChunker.class);

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
		int length = text.length();
		for (int start = 0; start < length; start += maxLength - overlap) {
			int end = Math.min(start + maxLength, length);
			chunks.add(text.substring(start, end));
			if (end == length) {
				break;
			}
		}
		List<String> result = List.copyOf(chunks);
		logStats("chunk", result);
		return result;
	}

	public List<String> chunkRecursively(String text, int maxLength, int overlap) {
		return chunkRecursively(text, maxLength, overlap, 0);
	}

	public List<String> chunkRecursively(String text, int maxLength, int overlap, int minimumLength) {
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
		List<String> chunks = List.copyOf(result);
		if (minimumLength > 0) {
			chunks = mergeSmallChunks(chunks, minimumLength);
		}
		logStats("chunkRecursively", chunks);
		return chunks;
	}

	private static List<String> splitOnRegex(String text, String regex) {
		return java.util.regex.Pattern.compile(regex)
				.splitAsStream(text)
				.map(String::trim)
				.filter(part -> !part.isEmpty())
				.toList();
	}

	private static List<String> chunkWords(String text, int maxLength, int overlap) {
		List<String> words = java.util.regex.Pattern.compile("\\s+")
				.splitAsStream(text.trim())
				.toList();
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
			List<String> previousWords = java.util.regex.Pattern.compile("\\s+")
					.splitAsStream(previous)
					.toList();
			int start = Math.max(0, previousWords.size() - overlap);
			StringBuilder builder = new StringBuilder();
			for (int j = start; j < previousWords.size(); j++) {
				if (builder.length() > 0) {
					builder.append(' ');
				}
				builder.append(previousWords.get(j));
			}
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(current);
			overlapped.add(builder.toString());
		}
		return List.copyOf(overlapped);
	}

	private static List<String> mergeSmallChunks(List<String> chunks, int minimumLength) {
		if (chunks.isEmpty()) {
			return chunks;
		}
		List<String> merged = new ArrayList<>();
		int index = 0;
		while (index < chunks.size()) {
			StringBuilder builder = new StringBuilder(chunks.get(index));
			while (builder.length() < minimumLength && index + 1 < chunks.size()) {
				index++;
				builder.append(' ').append(chunks.get(index));
			}
			merged.add(builder.toString());
			index++;
		}
		return List.copyOf(merged);
	}

	private static void logStats(String method, List<String> chunks) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		if (chunks.isEmpty()) {
			logger.debug("{} produced 0 chunks (min=0, mean=0.0, max=0).", method);
			return;
		}
		java.util.IntSummaryStatistics stats = chunks.stream()
				.mapToInt(String::length)
				.summaryStatistics();
		logger.debug(
				"{} produced {} chunks (min={}, mean={}, max={}).",
				method,
				chunks.size(),
				stats.getMin(),
				String.format(java.util.Locale.ROOT, "%.1f", stats.getAverage()),
				stats.getMax()
		);
	}
}
