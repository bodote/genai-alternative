package de.bas.bodo.genai.ingestion.gutenberg;

import java.util.List;

public interface GutenbergCatalog {
	List<GutenbergWork> fetchWorksByAuthorName(String authorName);
}
