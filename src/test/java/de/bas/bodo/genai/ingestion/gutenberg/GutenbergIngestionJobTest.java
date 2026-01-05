package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GutenbergIngestionJob")
class GutenbergIngestionJobTest {
	private static final int AUTHOR_ID = 69;
	private static final int MAX_DOWNLOAD_COUNT = 2;

	@Nested
	@DisplayName("ingestAll")
	class IngestAll {
		@Test
		void downloadsOnlyConfiguredMaximum() {
			RecordingDownloader downloader = new RecordingDownloader();
			RecordingCatalog catalog = new RecordingCatalog(List.of(
					new GutenbergWork(1661, "A"),
					new GutenbergWork(2852, "B"),
					new GutenbergWork(408, "C")
			));
			GutenbergIngestionProperties properties = new GutenbergIngestionProperties();
			properties.setAuthorId(AUTHOR_ID);
			properties.setMaxDownloadCount(MAX_DOWNLOAD_COUNT);
			GutenbergIngestionJobRunner job = new GutenbergIngestionJobRunner(catalog, downloader, properties);

			job.ingestAll();

			assertThat(catalog.requestedAuthorId()).isEqualTo(AUTHOR_ID);
			assertThat(downloader.downloadedIds()).containsExactly(1661, 2852);
		}
	}

	private static final class RecordingDownloader implements GutenbergDownloader {
		private final List<Integer> downloadedIds = new ArrayList<>();

		@Override
		public void downloadWork(int workId) {
			downloadedIds.add(workId);
		}

		private List<Integer> downloadedIds() {
			return List.copyOf(downloadedIds);
		}
	}

	private static final class RecordingCatalog implements GutenbergCatalog {
		private final List<GutenbergWork> works;
		private int requestedAuthorId;

		private RecordingCatalog(List<GutenbergWork> works) {
			this.works = new ArrayList<>(works);
		}

		@Override
		public List<GutenbergWork> fetchWorksByAuthorId(int authorId) {
			this.requestedAuthorId = authorId;
			return List.copyOf(works);
		}

		private int requestedAuthorId() {
			return requestedAuthorId;
		}
	}
}
