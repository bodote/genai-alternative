package de.bas.bodo.genai.generation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "genai.generation")
public class GenerationProperties {
	private int topK = 3;

	public int getTopK() {
		return topK;
	}

	public void setTopK(int topK) {
		this.topK = topK;
	}
}
