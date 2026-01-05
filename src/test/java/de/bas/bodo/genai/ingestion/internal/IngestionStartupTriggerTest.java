package de.bas.bodo.genai.ingestion.internal;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.retrieval.RetrievalCatalog;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.ApplicationArguments;

@DisplayName("IngestionStartupTrigger")
class IngestionStartupTriggerTest {
	@Nested
	@DisplayName("run")
	class Run {
		@Test
		void ingestsWhenCatalogIsEmpty() {
			RecordingIngestionJob ingestionJob = new RecordingIngestionJob();
			StubRetrievalCatalog retrievalCatalog = new StubRetrievalCatalog(List.of());
			IngestionStartupTrigger trigger = new IngestionStartupTrigger(retrievalCatalog, ingestionJob);

			ApplicationArguments args = Mockito.mock(ApplicationArguments.class);

			trigger.run(args);

			assertThat(ingestionJob.invocations()).isEqualTo(1);
		}

		@Test
		void skipsWhenCatalogHasEntries() {
			RecordingIngestionJob ingestionJob = new RecordingIngestionJob();
			StubRetrievalCatalog retrievalCatalog = new StubRetrievalCatalog(List.of(1661));
			IngestionStartupTrigger trigger = new IngestionStartupTrigger(retrievalCatalog, ingestionJob);

			ApplicationArguments args = Mockito.mock(ApplicationArguments.class);

			trigger.run(args);

			assertThat(ingestionJob.invocations()).isZero();
		}
	}

	private static final class RecordingIngestionJob implements GutenbergIngestionJob {
		private int invocations;

		@Override
		public void ingestAll() {
			invocations++;
		}

		private int invocations() {
			return invocations;
		}
	}

	private static final class StubRetrievalCatalog implements RetrievalCatalog {
		private final List<Integer> storedWorkIds;

		private StubRetrievalCatalog(List<Integer> storedWorkIds) {
			this.storedWorkIds = new ArrayList<>(storedWorkIds);
		}

		@Override
		public List<Integer> findStoredWorkIds() {
			return List.copyOf(storedWorkIds);
		}
	}
}
