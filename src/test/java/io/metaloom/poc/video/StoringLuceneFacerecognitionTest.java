package io.metaloom.poc.video;

import java.nio.file.Paths;

import org.junit.Test;

import io.metaloom.poc.AbstractFacedetectionTest;
import io.metaloom.poc.jooq.FaceStorage;
import io.metaloom.poc.video.impl.StoringLuceneFacerecognitionImpl;

public class StoringLuceneFacerecognitionTest extends AbstractFacedetectionTest {

	private static final boolean RELEARN = false;

	@Test
	public void testFacerecognition() throws Exception {

		try (FaceStorage storage = new FaceStorage()) {
			storage.connect(URL, USERNAME, PASSWORD);
			Facerecognition facerecognition = new StoringLuceneFacerecognitionImpl(INDEX_PATH, 0.60f, storage);

			if (RELEARN) {
				// Purge the storage and the index and relearn
				storage.purge();
				facerecognition.purge();

				facerecognition.learn("Robert Beltran #1", "voy_cast/robert-beltran-1.jpg");
				facerecognition.learn("Robert Beltran #2", "voy_cast/robert-beltran-2.jpg");

				facerecognition.learn("Ethan Phillips #1", "voy_cast/ethan-phillips-1.jpg");
				facerecognition.learn("Ethan Phillips #2", "voy_cast/ethan-phillips-2.jpg");

				facerecognition.learn("Garrett Wang #1", "voy_cast/garrett-wang-1.jpg");
				facerecognition.learn("Garrett Wang #2", "voy_cast/garrett-wang-2.jpg");

				facerecognition.learn("Jennifer Anne Lien #1", "voy_cast/jennifer-anne-lien-1.jpg");

				facerecognition.learn("Jeri Ryan #1", "voy_cast/jeri-ryan-1.jpg");
				facerecognition.learn("Jeri Ryan #2", "voy_cast/jeri-ryan-2.jpg");

				facerecognition.learn("Kate Mulgrew #1", "voy_cast/kate-mulgrew-1.jpg");
				facerecognition.learn("Kate Mulgrew #2", "voy_cast/kate-mulgrew-2.jpg");

				facerecognition.learn("Robert Duncan #1", "voy_cast/robert-duncan-1.jpg");
				facerecognition.learn("Robert Picardo #1", "voy_cast/robert-picardo-1.jpg");

				facerecognition.learn("Roxann Dawson #1", "voy_cast/roxann-dawson-1.jpg");
				facerecognition.learn("Roxann Dawson #2", "voy_cast/roxann-dawson-2.jpg");
				facerecognition.learn("Roxann Dawson #3", "voy_cast/roxann-dawson-3.jpg");

				facerecognition.learn("Tim Russ #1", "voy_cast/tim-russ-1.jpg");
				facerecognition.learn("Tim Russ #2", "voy_cast/tim-russ-2.jpg");
			} else {
				// Load the index from the storage
				facerecognition.reindex();
			}

			// Now use the index
			//facerecognition.testVideo(Paths.get("Live Fast And Prosper.mkv"));
			//facerecognition.testVideoDirectory(Paths.get("Voyager"));
		}

	}

}
