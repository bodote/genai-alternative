package de.bas.bodo.genai.generation.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "genai.generation")
public class GenerationProperties {
	private int topK = 3;
	private int historyMaxTurns = 20;

	public int getTopK() {
		return topK;
	}

	public void setTopK(int topK) {
		this.topK = topK;
	}

	public int getHistoryMaxTurns() {
		return historyMaxTurns;
	}

	public void setHistoryMaxTurns(int historyMaxTurns) {
		this.historyMaxTurns = historyMaxTurns;
	}
}
