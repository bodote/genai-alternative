package de.bas.bodo.genai.ingestion.gutenberg;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GutenbergCatalogClient implements GutenbergCatalog {
	private static final String AUTHOR_URL_PREFIX = "https://www.gutenberg.org/ebooks/author/";
	private static final Pattern WORK_PATTERN = Pattern.compile(
			"<a\\s+href=\\\"/ebooks/(\\d+)\\\"[^>]*>\\s*<span\\s+class=\\\"title\\\">(.*?)</span>",
			Pattern.DOTALL
	);

	private final GutenbergHttpClient httpClient;

	GutenbergCatalogClient(GutenbergHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public List<GutenbergWork> fetchWorksByAuthorId(int authorId) {
		String html = httpClient.get(AUTHOR_URL_PREFIX + authorId);
		return parseWorks(html);
	}

	private static List<GutenbergWork> parseWorks(String html) {
		Matcher matcher = WORK_PATTERN.matcher(html);
		List<GutenbergWork> works = new ArrayList<>();
		while (matcher.find()) {
			int id = Integer.parseInt(matcher.group(1));
			String title = decodeEntities(matcher.group(2).trim());
			works.add(new GutenbergWork(id, title));
		}
		return List.copyOf(works);
	}

	private static String decodeEntities(String title) {
		return title
				.replace("&amp;", "&")
				.replace("&quot;", "\"")
				.replace("&#39;", "'");
	}
}
