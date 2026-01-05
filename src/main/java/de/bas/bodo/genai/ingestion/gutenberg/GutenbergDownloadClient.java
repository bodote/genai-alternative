package de.bas.bodo.genai.ingestion.gutenberg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

final class GutenbergDownloadClient implements GutenbergDownloader {
	private static final String BOOK_DETAILS_PREFIX = "https://gutendex.com/books/";

	private final GutenbergHttpClient httpClient;
	private final GutenbergTextStore textStore;
	private final ObjectMapper objectMapper;

	GutenbergDownloadClient(GutenbergHttpClient httpClient, GutenbergTextStore textStore, ObjectMapper objectMapper) {
		this.httpClient = httpClient;
		this.textStore = textStore;
		this.objectMapper = objectMapper;
	}

	@Override
	public void downloadWork(int workId) {
		String detailsUrl = BOOK_DETAILS_PREFIX + workId + "/";
		String json = httpClient.get(detailsUrl);
		GutendexBookDetails details = parseDetails(json);
		String textUrl = resolveTextUrl(details.formats(), detailsUrl);
		String textBody = httpClient.get(textUrl);
		textStore.save(workId, textBody, textUrl);
	}

	private GutendexBookDetails parseDetails(String json) {
		try {
			return objectMapper.readValue(json, GutendexBookDetails.class);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to parse Gutendex response", ex);
		}
	}

	private static String resolveTextUrl(Map<String, String> formats, String detailsUrl) {
		if (formats == null || formats.isEmpty()) {
			throw new IllegalStateException("Gutendex response has no formats for " + detailsUrl);
		}
		return formats.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("text/plain"))
				.map(Map.Entry::getValue)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No text/plain format found for " + detailsUrl));
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GutendexBookDetails(Map<String, String> formats) {
	}
}
