package io.metaloom.poc.jooq;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.metaloom.poc.AbstractFacedetectionTest;
import io.metaloom.poc.FaceReference;

public class FaceStorageTest extends AbstractFacedetectionTest {

	@Test
	public void testStorage() throws Exception {
		try (FaceStorage storage = new FaceStorage()) {
			storage.connect(URL, USERNAME, PASSWORD);
			storage.purge();

			for (int i = 0; i < 100; i++) {
				float[] vector = generateFakeVector();
				FaceReference face = new FaceReference(UUID.randomUUID(), "test_face_" + i, vector);
				storage.storeFace(face);
			}

			AtomicInteger counter = new AtomicInteger();
			storage.loadFaces(face -> {
				System.out.println("Got: " + face);
				counter.incrementAndGet();
			});
			assertEquals("We expect to load all elements we created before.",100, counter.get());
		}
	}

}
