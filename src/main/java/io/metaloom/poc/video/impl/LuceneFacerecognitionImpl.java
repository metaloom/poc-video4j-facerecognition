package io.metaloom.poc.video.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.poc.FaceReference;
import io.metaloom.poc.bridge.FaceIndexBridge;
import io.metaloom.poc.jooq.FaceStorage;
import io.metaloom.poc.lucene.FaceIndexResult;
import io.metaloom.poc.lucene.impl.LuceneFaceIndexerImpl;
import io.metaloom.poc.video.AbstractFacerecognition;
import io.metaloom.video.facedetect.Face;

public class LuceneFacerecognitionImpl extends AbstractFacerecognition {

	public static final Logger log = LoggerFactory.getLogger(LuceneFacerecognitionImpl.class);

	public static final int RESULT_LIMIT = 10;

	private static LuceneFacerecognitionImpl INSTANCE;

	protected final LuceneFaceIndexerImpl indexer;

	private final float queryScoreThreshold;

	public LuceneFacerecognitionImpl(Path indexPath, float queryScoreThreshold) throws IOException {
		this.indexer = new LuceneFaceIndexerImpl(indexPath);
		this.queryScoreThreshold = queryScoreThreshold;
		INSTANCE = this;
		if (INSTANCE != null) {
			log.warn("Usage of multiple Facerecognition instances may cause unexpected errors.");
		}
	}

	@Override
	public void purge() throws IOException {
		indexer.purge();
	}

	@Override
	protected void storeFace(String source, Face face) throws IOException {
		indexer.writer(w -> {
			try {
				FaceReference faceRef = new FaceReference(UUID.randomUUID(), source, face.getEmbeddings());
				indexer.index(w, faceRef);
			} catch (IOException e) {
				log.error("Error while adding face of source {" + source + "} to the index", e);
			}
		});
	}

	@Override
	protected FaceIndexResult query(Face face) throws IOException {
		FaceReference ref = new FaceReference(null, null, face.getEmbeddings());
		return indexer.query(ref, RESULT_LIMIT, queryScoreThreshold);
	}

	@Override
	public void reindex() throws IOException {
		throw new RuntimeException("Not implemented");
	}

}
