package de.bas.bodo.genai.ingestion.chunking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TextChunker")
class TextChunkerTest {
	private static final String TEXT = "abcdefghij";
	private static final String PARAGRAPHS = "First para.\n\nSecond para.";
	private static final String LONG_SENTENCE = "Alpha beta gamma delta";
	private static final String SENTENCES = "Alpha beta. Gamma delta.";
	private static final String BLANK_PARAGRAPHS = "First para.\n\n   \n\nSecond para.";
	private static final String WHITESPACE_ONLY = "   \n\t  ";
	private static final String SHORT_TEXT = "Short line.";
	private static final String LONG_WORD = "Supercalifragilistic";

	@Nested
	@DisplayName("chunk")
	class Chunk {
		@Test
		void returnsEmptyListForEmptyInput() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunk("", 4, 0);

			assertThat(chunks).isEmpty();
		}

		@Test
		void rejectsNonPositiveMaxLength() {
			TextChunker chunker = new TextChunker();

			assertThatThrownBy(() -> chunker.chunk(TEXT, 0, 0))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void rejectsNegativeOverlap() {
			TextChunker chunker = new TextChunker();

			assertThatThrownBy(() -> chunker.chunk(TEXT, 4, -1))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void rejectsOverlapGreaterThanOrEqualToChunkSize() {
			TextChunker chunker = new TextChunker();

			assertThatThrownBy(() -> chunker.chunk(TEXT, 4, 4))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void splitsWithOverlap() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunk(TEXT, 4, 1);

			assertThat(chunks).containsExactly("abcd", "defg", "ghij");
		}

		@Test
		void splitsWithoutOverlap() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunk(TEXT, 4, 0);

			assertThat(chunks).containsExactly("abcd", "efgh", "ij");
		}
	}

	@Nested
	@DisplayName("chunkRecursively")
	class ChunkRecursively {
		@Test
		void splitsByParagraphsFirst() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunkRecursively(PARAGRAPHS, 20, 0);

			assertThat(chunks).containsExactly("First para.", "Second para.");
		}

		@Test
		void returnsEmptyListForWhitespaceOnly() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunkRecursively(WHITESPACE_ONLY, 10, 0);

			assertThat(chunks).isEmpty();
		}

		@Test
		void returnsSingleChunkWhenBelowMaxLength() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunkRecursively(SHORT_TEXT, 50, 0);

			assertThat(chunks).containsExactly(SHORT_TEXT);
		}

		@Test
		void skipsBlankParagraphs() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunkRecursively(BLANK_PARAGRAPHS, 12, 0);

			assertThat(chunks).containsExactly("First para.", "Second para.");
		}

		@Test
		void splitsBySentencesWhenParagraphTooLarge() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunkRecursively(SENTENCES, 12, 0);

			assertThat(chunks).containsExactly("Alpha beta.", "Gamma delta.");
		}

		@Test
		void fallsBackToWordsWhenSentenceTooLarge() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunkRecursively(LONG_SENTENCE, 10, 0);

			assertThat(chunks).containsExactly("Alpha beta", "gamma", "delta");
		}

		@Test
		void appliesWordOverlapWhenConfigured() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunkRecursively(LONG_SENTENCE, 10, 1);

			assertThat(chunks).containsExactly("Alpha beta", "beta gamma", "gamma delta");
		}

		@Test
		void keepsSingleLongWordWhenOverlapIsConfigured() {
			TextChunker chunker = new TextChunker();

			List<String> chunks = chunker.chunkRecursively(LONG_WORD, 10, 2);

			assertThat(chunks).containsExactly(LONG_WORD);
		}
	}
}
