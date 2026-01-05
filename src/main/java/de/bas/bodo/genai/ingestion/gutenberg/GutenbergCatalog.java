package de.bas.bodo.genai.ingestion.gutenberg;

import java.util.List;

public interface GutenbergCatalog {
	List<GutenbergWork> fetchWorksByAuthorId(int authorId);
}
