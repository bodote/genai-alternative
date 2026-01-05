package de.bas.bodo.genai.ingestion.gutenberg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class RecordingGutenbergHttpClient implements GutenbergHttpClient {
	private final Map<String, String> responses;
	private final String defaultResponse;
	private final boolean requireMatch;
	private final List<String> requestedUrls = new ArrayList<>();

	private RecordingGutenbergHttpClient(Map<String, String> responses, String defaultResponse, boolean requireMatch) {
		this.responses = new HashMap<>(responses);
		this.defaultResponse = defaultResponse;
		this.requireMatch = requireMatch;
	}

	static RecordingGutenbergHttpClient fixedResponse(String response) {
		return new RecordingGutenbergHttpClient(Map.of(), response, false);
	}

	static RecordingGutenbergHttpClient withResponses(Map<String, String> responses) {
		return new RecordingGutenbergHttpClient(responses, "", true);
	}

	static RecordingGutenbergHttpClient sequence(List<String> responses) {
		return new RecordingGutenbergHttpClientSequence(responses);
	}

	@Override
	public String get(String url) {
		requestedUrls.add(url);
		if (requireMatch) {
			return Objects.requireNonNull(responses.get(url));
		}
		return responses.getOrDefault(url, defaultResponse);
	}

	List<String> requestedUrls() {
		return List.copyOf(requestedUrls);
	}

	String lastUrl() {
		if (requestedUrls.isEmpty()) {
			return "";
		}
		return requestedUrls.get(requestedUrls.size() - 1);
	}

	private static final class RecordingGutenbergHttpClientSequence extends RecordingGutenbergHttpClient {
		private final List<String> responses;
		private int index;

		private RecordingGutenbergHttpClientSequence(List<String> responses) {
			super(Map.of(), "", false);
			this.responses = List.copyOf(responses);
		}

		@Override
		public String get(String url) {
			super.get(url);
			if (index >= responses.size()) {
				throw new IllegalStateException("No response configured for " + url);
			}
			return responses.get(index++);
		}
	}
}
