package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GutenbergTextCleaner normalization")
class GutenbergTextCleanerNormalizationTest {
	private static final String RAW_WITHOUT_MARKERS = "Title Page\n\nBody line";
	private static final String RAW_WITH_SPACING = "Line one\r\n\r\n\tLine   two\n\n\nLine three  ";
	private static final String EXPECTED_NORMALIZED = "Line one\n\nLine two\n\nLine three";

	@Nested
	@DisplayName("stripBoilerplate")
	class StripBoilerplate {
		@Test
		void keepsTextWhenMarkersMissing() {
			String cleaned = new GutenbergTextCleaner().stripBoilerplate(RAW_WITHOUT_MARKERS);

			assertThat(cleaned).isEqualTo(RAW_WITHOUT_MARKERS);
		}
	}

	@Nested
	@DisplayName("normalizeWhitespace")
	class NormalizeWhitespace {
		@Test
		void normalizesLineEndingsAndSpacing() {
			String cleaned = new GutenbergTextCleaner().normalizeWhitespace(RAW_WITH_SPACING);

			assertThat(cleaned).isEqualTo(EXPECTED_NORMALIZED);
		}
	}
}
