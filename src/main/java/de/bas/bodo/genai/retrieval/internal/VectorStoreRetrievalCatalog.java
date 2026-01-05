package de.bas.bodo.genai.retrieval.internal;

import de.bas.bodo.genai.retrieval.RetrievalCatalog;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public final class VectorStoreRetrievalCatalog implements RetrievalCatalog {
	private static final String WORK_ID_QUERY = "select distinct (metadata->>'workId')::int from vector_store";

	private final JdbcTemplate jdbcTemplate;

	public VectorStoreRetrievalCatalog(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Integer> findStoredWorkIds() {
		return jdbcTemplate.query(WORK_ID_QUERY, (rs, rowNum) -> rs.getInt(1));
	}
}
