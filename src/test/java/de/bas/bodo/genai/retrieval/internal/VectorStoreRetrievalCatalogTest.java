package de.bas.bodo.genai.retrieval.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("VectorStoreRetrievalCatalog")
class VectorStoreRetrievalCatalogTest {
	@Test
	void returnsStoredWorkIdsFromJdbcQuery() {
		JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
		when(jdbcTemplate.query(Mockito.anyString(), Mockito.<org.springframework.jdbc.core.RowMapper<Integer>>any()))
				.thenReturn(List.of(1661, 2852));
		VectorStoreRetrievalCatalog catalog = new VectorStoreRetrievalCatalog(jdbcTemplate);

		List<Integer> workIds = catalog.findStoredWorkIds();

		assertThat(workIds).containsExactly(1661, 2852);
		verify(jdbcTemplate).query(Mockito.anyString(), Mockito.<org.springframework.jdbc.core.RowMapper<Integer>>any());
	}
}
