package de.bas.bodo.genai.retrieval;

public record EmbeddedChunk(int workId, int index, String text, float[] embedding) {}
