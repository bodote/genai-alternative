package de.bas.bodo.genai.retrieval;

public record RetrievedChunk(int workId, int index, String text, double score) {
}
