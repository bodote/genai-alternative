package de.bas.bodo.genai.ingestion.gutenberg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("GutenbergRestClient")
class GutenbergRestClientTest {
	private static final String URL = "https://www.gutenberg.org/ebooks/1661";
	private static final String RESPONSE = "<html>ok</html>";

	@Test
	void fetchesHtmlFromUrl() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.build();
		server.expect(requestTo(URL))
				.andRespond(withSuccess(RESPONSE, MediaType.TEXT_HTML));

		GutenbergRestClient client = new GutenbergRestClient(restClient);

		String body = client.get(URL);

		server.verify();
		assertThat(body).isEqualTo(RESPONSE);
	}
}
