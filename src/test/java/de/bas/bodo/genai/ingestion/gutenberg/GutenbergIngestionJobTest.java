package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GutenbergIngestionJob")
class GutenbergIngestionJobTest {
	private static final String AUTHOR_NAME = "Conan Doyle";
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
			properties.setAuthorName(AUTHOR_NAME);
			properties.setMaxDownloadCount(MAX_DOWNLOAD_COUNT);
			GutenbergIngestionJobRunner job = new GutenbergIngestionJobRunner(catalog, downloader, properties);

			job.ingestAll();

			assertThat(catalog.requestedAuthorName()).isEqualTo(AUTHOR_NAME);
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
		private String requestedAuthorName = "";

		private RecordingCatalog(List<GutenbergWork> works) {
			this.works = new ArrayList<>(works);
		}

		@Override
		public List<GutenbergWork> fetchWorksByAuthorName(String authorName) {
			this.requestedAuthorName = authorName;
			return List.copyOf(works);
		}

		private String requestedAuthorName() {
			return requestedAuthorName;
		}
	}
}
