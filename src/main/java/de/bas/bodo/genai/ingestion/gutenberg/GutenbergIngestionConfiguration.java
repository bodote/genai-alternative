package de.bas.bodo.genai.ingestion.gutenberg;

import de.bas.bodo.genai.ingestion.internal.IngestionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GutenbergIngestionProperties.class)
public class GutenbergIngestionConfiguration {
	@Bean
	RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}

	@Bean
	GutenbergHttpClient gutenbergHttpClient(RestClient restClient) {
		return new GutenbergRestClient(restClient);
	}

	@Bean
	GutenbergCatalog gutenbergCatalog(GutenbergHttpClient httpClient) {
		return new GutenbergCatalogClient(httpClient);
	}

	@Bean
	GutenbergTextStore gutenbergTextStore(IngestionHandler ingestionHandler, GutenbergIngestionProperties properties) {
		return new GutenbergIngestionTextStore(ingestionHandler, properties);
	}

	@Bean
	GutenbergDownloader gutenbergDownloader(GutenbergHttpClient httpClient, GutenbergTextStore textStore) {
		return new GutenbergDownloadClient(httpClient, textStore);
	}
}
