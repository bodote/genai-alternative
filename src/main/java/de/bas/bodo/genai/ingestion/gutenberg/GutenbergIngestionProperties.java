package de.bas.bodo.genai.ingestion.gutenberg;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "genai.ingestion")
public class GutenbergIngestionProperties {
	private int chunkSize = 1000;
	private int chunkOverlap = 100;
	private int maxDownloadCount = 2;
	private int authorId = 69;
	private String authorName = "Conan Doyle";

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public int getChunkOverlap() {
		return chunkOverlap;
	}

	public void setChunkOverlap(int chunkOverlap) {
		this.chunkOverlap = chunkOverlap;
	}

	public int getMaxDownloadCount() {
		return maxDownloadCount;
	}

	public void setMaxDownloadCount(int maxDownloadCount) {
		this.maxDownloadCount = maxDownloadCount;
	}

	public int getAuthorId() {
		return authorId;
	}

	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
}
