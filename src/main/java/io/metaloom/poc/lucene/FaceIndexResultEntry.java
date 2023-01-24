package io.metaloom.poc.lucene;

import java.util.UUID;

public class FaceIndexResultEntry {

	private UUID uuid;
	private String source;
	private float score;

	public FaceIndexResultEntry(float score, String source, UUID uuid) {
		this.score = score;
		this.source = source;
		this.uuid = uuid;
	}

	public String source() {
		return source;
	}

	public float score() {
		return score;
	}

	public UUID uuid() {
		return uuid;
	}

	@Override
	public String toString() {
		return "[" + uuid() + "] => " + source + ", score:" + String.format("%.2f", score());
	}
}
