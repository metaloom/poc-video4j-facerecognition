package io.metaloom.poc.video.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.metaloom.poc.video.Facerecognition;
import io.metaloom.poc.video.impl.LuceneFacerecognitionImpl;

public class FacedetectionWorkerRunner {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: [INDEX_PATH] [TEST_PATH]");
			System.exit(10);
		}
		Path indexPath = Paths.get(args[0]);
		Path testPath = Paths.get(args[1]);

		if (!Files.exists(indexPath)) {
			System.err.println("The index could not be found at " + indexPath);
			System.exit(11);
		}
		if (!Files.exists(testPath)) {
			System.err.println("The test path could not be found at " + testPath);
			System.exit(12);
		}

		Facerecognition facerecognition = new LuceneFacerecognitionImpl(indexPath, 0.60f);
		facerecognition.testImage(testPath);
	}
}
