package io.metaloom.poc.video;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import io.metaloom.poc.FaceReference;

public interface Facerecognition {

	FaceReference learn(String source, String imagePath) throws IOException;

	void testVideoDirectory(Path videoPath) throws IOException;

	void testImage(Path imagePath) throws IOException;

	void purge() throws IOException;

	void testVideo(Path path) throws FileNotFoundException;

	void reindex() throws IOException;


}
