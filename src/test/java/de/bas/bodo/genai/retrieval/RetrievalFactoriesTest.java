package de.bas.bodo.genai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Retrieval factories")
class RetrievalFactoriesTest {
	private static final int WORK_ID = 1661;
	private static final int FIRST_INDEX = 0;
	private static final int SECOND_INDEX = 1;
	private static final String FIRST_TEXT = "First";
	private static final String SECOND_TEXT = "Second";
	private static final double FIRST_SCORE = 0.91;
	private static final double SECOND_SCORE = 0.87;

	@Nested
	@DisplayName("RetrievedChunk.from")
	class RetrievedChunkFactory {
		@Test
		void mapsStoredChunk() {
			StoredChunk storedChunk = new StoredChunk(WORK_ID, FIRST_INDEX, FIRST_TEXT, FIRST_SCORE);

			RetrievedChunk result = RetrievedChunk.from(storedChunk);

			assertThat(result).isEqualTo(new RetrievedChunk(WORK_ID, FIRST_INDEX, FIRST_TEXT, FIRST_SCORE));
		}
	}

	@Nested
	@DisplayName("RetrievalResult.fromStored")
	class RetrievalResultFactory {
		@Test
		void mapsStoredChunksInOrder() {
			List<StoredChunk> storedChunks = List.of(
					new StoredChunk(WORK_ID, FIRST_INDEX, FIRST_TEXT, FIRST_SCORE),
					new StoredChunk(WORK_ID, SECOND_INDEX, SECOND_TEXT, SECOND_SCORE)
			);

			RetrievalResult result = RetrievalResult.fromStored(storedChunks);

			assertThat(result.chunks()).containsExactly(
					new RetrievedChunk(WORK_ID, FIRST_INDEX, FIRST_TEXT, FIRST_SCORE),
					new RetrievedChunk(WORK_ID, SECOND_INDEX, SECOND_TEXT, SECOND_SCORE)
			);
		}
	}
}
