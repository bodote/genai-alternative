package de.bas.bodo.genai.retrieval;

public record StoredChunk(int workId, int index, String text, double score) {}
