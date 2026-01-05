package de.bas.bodo.genai.retrieval.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Testcontainers
@DisplayName("VectorStoreRetrievalCatalog IT")
class VectorStoreRetrievalCatalogIT {
	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

	@Test
	void readsDistinctWorkIdsFromVectorStoreMetadata() throws Exception {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setDriverClass(org.postgresql.Driver.class);
		dataSource.setUrl(postgres.getJdbcUrl());
		dataSource.setUsername(postgres.getUsername());
		dataSource.setPassword(postgres.getPassword());
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.execute("create table vector_store (id bigserial primary key, metadata jsonb)");
		jdbcTemplate.update("insert into vector_store (metadata) values ('{\"workId\": 1661}')");
		jdbcTemplate.update("insert into vector_store (metadata) values ('{\"workId\": 2852}')");
		jdbcTemplate.update("insert into vector_store (metadata) values ('{\"workId\": 1661}')");

		VectorStoreRetrievalCatalog catalog = new VectorStoreRetrievalCatalog(jdbcTemplate);

		List<Integer> workIds = catalog.findStoredWorkIds();

		assertThat(workIds).containsExactlyInAnyOrder(1661, 2852);
	}
}
