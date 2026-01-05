package de.bas.bodo.genai.ingestion.gutenberg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class GutenbergCatalogClient implements GutenbergCatalog {
	private static final String GUTENDEX_BASE_URL = "https://gutendex.com/books";
	private static final String LANGUAGE_FILTER = "en";

	private final GutenbergHttpClient httpClient;
	private final ObjectMapper objectMapper;

	GutenbergCatalogClient(GutenbergHttpClient httpClient, ObjectMapper objectMapper) {
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	@Override
	public List<GutenbergWork> fetchWorksByAuthorName(String authorName) {
		String url = GUTENDEX_BASE_URL
				+ "?search=" + encode(authorName)
				+ "&languages=" + encode(LANGUAGE_FILTER);
		String json = httpClient.get(url);
		List<GutenbergWork> works = parseWorks(json);
		if (works.isEmpty()) {
			String fallbackUrl = GUTENDEX_BASE_URL
					+ "?search=" + encode(fallbackAuthorName(authorName))
					+ "&languages=" + encode(LANGUAGE_FILTER);
			String fallbackJson = httpClient.get(fallbackUrl);
			return parseWorks(fallbackJson);
		}
		return works;
	}

	private List<GutenbergWork> parseWorks(String json) {
		try {
			GutendexResponse response = objectMapper.readValue(json, GutendexResponse.class);
			return response.results().stream()
					.map(book -> new GutenbergWork(book.id(), book.title()))
					.toList();
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to parse Gutendex response", ex);
		}
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static String fallbackAuthorName(String authorName) {
		if (authorName == null || authorName.isBlank()) {
			return "Conan Doyle";
		}
		if (authorName.contains(",")) {
			String[] parts = authorName.split(",", 2);
			String lastName = parts[0].trim();
			String firstName = parts.length > 1 ? parts[1].trim() : "";
			return (firstName + " " + lastName).trim();
		}
		if (authorName.contains("Arthur Conan Doyle")) {
			return "Conan Doyle";
		}
		return authorName;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GutendexResponse(List<GutendexBook> results) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GutendexBook(int id, String title) {
	}
}
