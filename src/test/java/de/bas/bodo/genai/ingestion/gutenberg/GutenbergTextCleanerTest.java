package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GutenbergTextCleaner")
class GutenbergTextCleanerTest {
	private static final String START_MARKER = "*** START OF THE PROJECT GUTENBERG EBOOK THE ADVENTURES OF SHERLOCK HOLMES ***";
	private static final String END_MARKER = "*** END OF THE PROJECT GUTENBERG EBOOK THE ADVENTURES OF SHERLOCK HOLMES ***";
	private static final String BODY = "CHAPTER I.\n\nMr. Sherlock Holmes";

	@Nested
	@DisplayName("stripBoilerplate")
	class StripBoilerplate {
		@Test
		void removesHeaderAndFooter() {
			String raw = String.join("\n",
					"Header noise",
					START_MARKER,
					BODY,
					END_MARKER,
					"Footer noise"
			);

			String cleaned = new GutenbergTextCleaner().stripBoilerplate(raw);

			assertThat(cleaned).isEqualTo(BODY);
		}
	}
}
