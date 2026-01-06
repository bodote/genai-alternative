package de.bas.bodo.genai.retrieval.internal;

import de.bas.bodo.genai.retrieval.RetrievalMaintenance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public final class VectorStoreRetrievalMaintenance implements RetrievalMaintenance {
	private static final String CLEAR_VECTOR_STORE = "truncate table vector_store";

	private final JdbcTemplate jdbcTemplate;

	public VectorStoreRetrievalMaintenance(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void clearVectorStore() {
		jdbcTemplate.execute(CLEAR_VECTOR_STORE);
	}
}
