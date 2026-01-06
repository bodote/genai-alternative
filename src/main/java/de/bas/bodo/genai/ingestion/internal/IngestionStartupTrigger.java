package de.bas.bodo.genai.ingestion.internal;

import de.bas.bodo.genai.retrieval.RetrievalCatalog;
import de.bas.bodo.genai.retrieval.RetrievalMaintenance;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public final class IngestionStartupTrigger implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(IngestionStartupTrigger.class);
	private static final String CLEAN_DB_OPTION = "cleanDB";

	private final RetrievalCatalog retrievalCatalog;
	private final RetrievalMaintenance retrievalMaintenance;
	private final GutenbergIngestionJob ingestionJob;
	private final IngestionStartupProperties properties;

	public IngestionStartupTrigger(
			RetrievalCatalog retrievalCatalog,
			RetrievalMaintenance retrievalMaintenance,
			GutenbergIngestionJob ingestionJob,
			IngestionStartupProperties properties
	) {
		this.retrievalCatalog = retrievalCatalog;
		this.retrievalMaintenance = retrievalMaintenance;
		this.ingestionJob = ingestionJob;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (args.containsOption(CLEAN_DB_OPTION)) {
			logger.info("cleanDB flag detected. Clearing vector store and re-ingesting.");
			retrievalMaintenance.clearVectorStore();
			ingestionJob.ingestAll();
			return;
		}
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
