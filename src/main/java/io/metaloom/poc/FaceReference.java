package io.metaloom.poc;

import java.util.UUID;

public class FaceReference {

	private final UUID uuid;
	private final String source;
	private final float[] vector;

	public FaceReference(UUID uuid, String source, float[] vector) {
		this.uuid = uuid;
		this.source = source;
		this.vector = vector;
	}

	public UUID uuid() {
		return uuid;
	}

	public String source() {
		return source;
	}

	public float[] vector() {
		return vector;
	}

	@Override
	public String toString() {
		return "[" + uuid() + "] from [" + source + "]";
	}
}
