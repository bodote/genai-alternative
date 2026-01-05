package de.bas.bodo.genai.retrieval.internal;

public record StoredChunk(int workId, int index, String text, double score) {}
