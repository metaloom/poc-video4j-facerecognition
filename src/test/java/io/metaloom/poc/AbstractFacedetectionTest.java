package io.metaloom.poc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public abstract class AbstractFacedetectionTest {

	public static final Path INDEX_PATH = Paths.get("target/lucene_index");

	public static String USERNAME = "postgres";
	public static String PASSWORD = "finger";
	public static String URL = "jdbc:postgresql://localhost:5432/postgres";

	protected float[] generateFakeVector() {
		Random rnd = new Random();
		float[] array = new float[128];
		for (int i = 0; i < 128; i++) {
			array[i] = rnd.nextFloat();
		}
		return array;
	}
}
