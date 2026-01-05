package de.bas.bodo.genai.ingestion.internal;

import de.bas.bodo.genai.retrieval.RetrievalCatalog;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public final class IngestionStartupTrigger implements ApplicationRunner {
	private final RetrievalCatalog retrievalCatalog;
	private final GutenbergIngestionJob ingestionJob;

	public IngestionStartupTrigger(RetrievalCatalog retrievalCatalog, GutenbergIngestionJob ingestionJob) {
		this.retrievalCatalog = retrievalCatalog;
		this.ingestionJob = ingestionJob;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (retrievalCatalog.findStoredWorkIds().isEmpty()) {
			ingestionJob.ingestAll();
		}
	}
}
