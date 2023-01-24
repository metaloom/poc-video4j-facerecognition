package io.metaloom.poc.video.impl;

import java.io.IOException;
import java.nio.file.Path;

import io.metaloom.poc.FaceReference;
import io.metaloom.poc.bridge.FaceIndexBridge;
import io.metaloom.poc.jooq.FaceStorage;

/**
 * This implementation of the {@link LuceneFacerecognitionImpl} will automatically store learned faces in the {@link FaceStorage}.
 */
public class StoringLuceneFacerecognitionImpl extends LuceneFacerecognitionImpl {

	private FaceStorage storage;

	public StoringLuceneFacerecognitionImpl(Path indexPath, float queryScoreThreshold, FaceStorage storage) throws IOException {
		super(indexPath, queryScoreThreshold);
		this.storage = storage;
	}

	@Override
	public void reindex() throws IOException {
		FaceIndexBridge bridge = new FaceIndexBridge(indexer, storage);
		bridge.index();
	}

	@Override
	public FaceReference learn(String source, String imagePath) throws IOException {
		FaceReference ref = super.learn(source, imagePath);
		storage.storeFace(ref);
		return ref;
	}
}
