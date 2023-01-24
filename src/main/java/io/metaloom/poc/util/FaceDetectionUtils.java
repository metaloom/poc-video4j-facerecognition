package io.metaloom.poc.util;

import static io.metaloom.loom.db.jooq.tables.Face.FACE;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.jooq.Record;

import io.metaloom.poc.FaceReference;

public final class FaceDetectionUtils {

	private FaceDetectionUtils() {
	}

	public static float[] bytesToFloats(byte[] bytes) {
		if (bytes.length % Float.BYTES != 0) {
			throw new RuntimeException("Illegal length");
		}
		float floats[] = new float[bytes.length / Float.BYTES];
		ByteBuffer.wrap(bytes).asFloatBuffer().get(floats);
		return floats;
	}

	public static byte[] floatsToBytes(float[] floats) {
		byte bytes[] = new byte[Float.BYTES * floats.length];
		ByteBuffer.wrap(bytes).asFloatBuffer().put(floats);
		return bytes;
	}

	public static FaceReference toReference(Record face) {
		if (face == null) {
			return null;
		}
		UUID uuid = face.getValue(FACE.UUID);
		// String meta = face.getValue(FACE.META);
		String source = face.getValue(FACE.SOURCE);
		byte[] embedding = face.getValue(FACE.BLOB);

		float[] vector = FaceDetectionUtils.bytesToFloats(embedding);
		return new FaceReference(uuid, source, vector);
	}

	public static double[] convertFloatsToDoubles(float[] input) {
		if (input == null) {
			return null; // Or throw an exception - your choice
		}
		double[] output = new double[input.length];
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i];
		}
		return output;
	}

}
