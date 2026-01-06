package de.bas.bodo.genai.retrieval.internal;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("VectorStoreRetrievalMaintenance")
class VectorStoreRetrievalMaintenanceTest {
	private static final String CLEAR_STATEMENT = "truncate table vector_store";

	@Test
	void clearsVectorStoreTable() {
		JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
		VectorStoreRetrievalMaintenance maintenance = new VectorStoreRetrievalMaintenance(jdbcTemplate);

		maintenance.clearVectorStore();

		verify(jdbcTemplate).execute(CLEAR_STATEMENT);
	}
}
