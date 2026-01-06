package de.bas.bodo.genai.ingestion.internal;

import static org.assertj.core.api.Assertions.assertThat;

import de.bas.bodo.genai.retrieval.RetrievalCatalog;
import de.bas.bodo.genai.retrieval.RetrievalMaintenance;
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
			IngestionStartupProperties properties = new IngestionStartupProperties();
			RecordingRetrievalMaintenance maintenance = new RecordingRetrievalMaintenance();
			IngestionStartupTrigger trigger =
					new IngestionStartupTrigger(retrievalCatalog, maintenance, ingestionJob, properties);

			ApplicationArguments args = Mockito.mock(ApplicationArguments.class);

			trigger.run(args);

			assertThat(ingestionJob.invocations()).isEqualTo(1);
		}

		@Test
		void skipsWhenCatalogHasEntries() {
			RecordingIngestionJob ingestionJob = new RecordingIngestionJob();
			StubRetrievalCatalog retrievalCatalog = new StubRetrievalCatalog(List.of(1661));
			IngestionStartupProperties properties = new IngestionStartupProperties();
			RecordingRetrievalMaintenance maintenance = new RecordingRetrievalMaintenance();
			IngestionStartupTrigger trigger =
					new IngestionStartupTrigger(retrievalCatalog, maintenance, ingestionJob, properties);

			ApplicationArguments args = Mockito.mock(ApplicationArguments.class);

			trigger.run(args);

			assertThat(ingestionJob.invocations()).isZero();
		}

		@Test
		void skipsWhenStartupIsDisabled() {
			RecordingIngestionJob ingestionJob = new RecordingIngestionJob();
			StubRetrievalCatalog retrievalCatalog = new StubRetrievalCatalog(List.of());
			IngestionStartupProperties properties = new IngestionStartupProperties();
			properties.setEnabled(false);
			RecordingRetrievalMaintenance maintenance = new RecordingRetrievalMaintenance();
			IngestionStartupTrigger trigger =
					new IngestionStartupTrigger(retrievalCatalog, maintenance, ingestionJob, properties);

			ApplicationArguments args = Mockito.mock(ApplicationArguments.class);

			trigger.run(args);

			assertThat(ingestionJob.invocations()).isZero();
		}

		@Test
		void cleansVectorStoreAndIngestsWhenCleanDbOptionIsSet() {
			RecordingIngestionJob ingestionJob = new RecordingIngestionJob();
			StubRetrievalCatalog retrievalCatalog = new StubRetrievalCatalog(List.of(1661));
			RecordingRetrievalMaintenance maintenance = new RecordingRetrievalMaintenance();
			IngestionStartupProperties properties = new IngestionStartupProperties();
			IngestionStartupTrigger trigger =
					new IngestionStartupTrigger(retrievalCatalog, maintenance, ingestionJob, properties);
			ApplicationArguments args = new StubApplicationArguments(List.of("cleanDB"));

			trigger.run(args);

			assertThat(maintenance.invocations()).isEqualTo(1);
			assertThat(ingestionJob.invocations()).isEqualTo(1);
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

	private static final class RecordingRetrievalMaintenance implements RetrievalMaintenance {
		private int invocations;

		@Override
		public void clearVectorStore() {
			invocations++;
		}

		private int invocations() {
			return invocations;
		}
	}

	private static final class StubApplicationArguments implements ApplicationArguments {
		private final List<String> optionNames;

		private StubApplicationArguments(List<String> optionNames) {
			this.optionNames = List.copyOf(optionNames);
		}

		@Override
		public java.util.Set<String> getOptionNames() {
			return new java.util.HashSet<>(optionNames);
		}

		@Override
		public boolean containsOption(String name) {
			return optionNames.contains(name);
		}

		@Override
		public java.util.List<String> getOptionValues(String name) {
			return java.util.List.of();
		}

		@Override
		public java.util.List<String> getNonOptionArgs() {
			return java.util.List.of();
		}

		@Override
		public String[] getSourceArgs() {
			return optionNames.toArray(new String[0]);
		}
	}
}
