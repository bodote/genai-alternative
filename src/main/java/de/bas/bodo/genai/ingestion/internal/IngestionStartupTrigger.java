package de.bas.bodo.genai.ingestion.internal;

import de.bas.bodo.genai.retrieval.RetrievalCatalog;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public final class IngestionStartupTrigger implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(IngestionStartupTrigger.class);

	private final RetrievalCatalog retrievalCatalog;
	private final GutenbergIngestionJob ingestionJob;
	private final IngestionStartupProperties properties;

	public IngestionStartupTrigger(
			RetrievalCatalog retrievalCatalog,
			GutenbergIngestionJob ingestionJob,
			IngestionStartupProperties properties
	) {
		this.retrievalCatalog = retrievalCatalog;
		this.ingestionJob = ingestionJob;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!properties.isEnabled()) {
			logger.info("Skipping ingestion because startup ingestion is disabled.");
			return;
		}
		List<Integer> storedWorkIds = retrievalCatalog.findStoredWorkIds();
		if (storedWorkIds.isEmpty()) {
			logger.info("No stored Gutenberg works found. Starting ingestion.");
			ingestionJob.ingestAll();
			return;
		}
		logger.info("Skipping ingestion. Found {} stored Gutenberg works.", storedWorkIds.size());
	}
}
