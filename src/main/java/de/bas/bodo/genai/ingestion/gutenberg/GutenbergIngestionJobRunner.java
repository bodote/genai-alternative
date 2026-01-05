package de.bas.bodo.genai.ingestion.gutenberg;

import de.bas.bodo.genai.ingestion.internal.GutenbergIngestionJob;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class GutenbergIngestionJobRunner implements GutenbergIngestionJob {
	private static final Logger logger = LoggerFactory.getLogger(GutenbergIngestionJobRunner.class);

	private final GutenbergCatalog catalog;
	private final GutenbergDownloader downloader;
	private final GutenbergIngestionProperties properties;

	public GutenbergIngestionJobRunner(
			GutenbergCatalog catalog,
			GutenbergDownloader downloader,
			GutenbergIngestionProperties properties
	) {
		this.catalog = catalog;
		this.downloader = downloader;
		this.properties = properties;
	}

	@Override
	public void ingestAll() {
		int authorId = properties.getAuthorId();
		String authorName = properties.getAuthorName();
		int maxCount = properties.getMaxDownloadCount();
		logger.info("Starting Gutenberg ingestion for author {} (id {}, max {} works).", authorName, authorId, maxCount);
		List<GutenbergWork> works = catalog.fetchWorksByAuthorName(authorName);
		int limit = Math.min(maxCount, works.size());
		for (int i = 0; i < limit; i++) {
			GutenbergWork work = works.get(i);
			logger.info("Downloading Gutenberg work {} - {}", work.id(), work.title());
			downloader.downloadWork(work.id());
		}
		logger.info("Completed Gutenberg ingestion. Downloaded {} works.", limit);
	}
}
