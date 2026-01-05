package de.bas.bodo.genai.ingestion.gutenberg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GutenbergDownloadClient implements GutenbergDownloader {
	private static final String WORK_PAGE_PREFIX = "https://www.gutenberg.org/ebooks/";
	private static final String GUTENBERG_HOST = "https://www.gutenberg.org";
	private static final Pattern TEXT_LINK_PATTERN = Pattern.compile("href=\\\"([^\\\"]+\\.txt(?:\\.utf-8)?)\\\"");

	private final GutenbergHttpClient httpClient;
	private final GutenbergTextStore textStore;

	GutenbergDownloadClient(GutenbergHttpClient httpClient, GutenbergTextStore textStore) {
		this.httpClient = httpClient;
		this.textStore = textStore;
	}

	@Override
	public void downloadWork(int workId) {
		String workPageUrl = WORK_PAGE_PREFIX + workId;
		String html = httpClient.get(workPageUrl);
		String textUrl = resolveTextUrl(html);
		String fullTextUrl = toAbsoluteUrl(textUrl);
		String textBody = httpClient.get(fullTextUrl);
		textStore.save(workId, textBody, fullTextUrl);
	}

	private static String resolveTextUrl(String html) {
		Matcher matcher = TEXT_LINK_PATTERN.matcher(html);
		if (!matcher.find()) {
			throw new IllegalStateException("No text link found");
		}
		return matcher.group(1);
	}

	private static String toAbsoluteUrl(String href) {
		if (href.startsWith("http")) {
			return href;
		}
		return GUTENBERG_HOST + (href.startsWith("/") ? href : "/" + href);
	}
}
