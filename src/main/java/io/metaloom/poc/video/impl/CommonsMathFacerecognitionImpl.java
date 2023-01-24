package io.metaloom.poc.video.impl;

import static io.metaloom.poc.util.FaceDetectionUtils.convertFloatsToDoubles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.poc.lucene.FaceIndexResult;
import io.metaloom.poc.lucene.FaceIndexResultEntry;
import io.metaloom.poc.video.AbstractFacerecognition;
import io.metaloom.video.facedetect.Face;

/**
 * In-Memory implementation for face recognition which uses {@link EuclideanDistance} from commons-math to compute the score between known faces. This solution
 * is slower compared to {@link LuceneFacerecognitionImpl} since it requires re-computation on every query of the distance to all known faces.
 */
public class CommonsMathFacerecognitionImpl extends AbstractFacerecognition {

	public static final Logger log = LoggerFactory.getLogger(CommonsMathFacerecognitionImpl.class);

	final static double LIMIT = 0.50;

	private Map<String, Face> knownFaces = new HashMap<>();

	public CommonsMathFacerecognitionImpl() throws FileNotFoundException {
	}

	@Override
	protected void storeFace(String source, Face face) {
		knownFaces.put(source, face);
	}

	@Override
	protected FaceIndexResult query(Face face) {
		double[] embeddings = convertFloatsToDoubles(face.getEmbeddings());
		EuclideanDistance d = new EuclideanDistance();
		FaceIndexResult result = new FaceIndexResult();
		String bestLabel = "unknown";
		double bestDistance = Double.MAX_VALUE;
		for (Entry<String, Face> known : knownFaces.entrySet()) {

			float[] knownEmbeddingsFloat = known.getValue().getEmbeddings();
			if (knownEmbeddingsFloat == null) {
				log.warn("Known face for label " + known.getKey() + " has no embeddings.");
				continue;
			}
			double[] knownEmbeddings = convertFloatsToDoubles(knownEmbeddingsFloat);
			double distance = d.compute(embeddings, knownEmbeddings);
			if (distance <= LIMIT && distance < bestDistance) {
				bestDistance = distance;
				bestLabel = known.getKey();
			}
		}
		if (!bestLabel.equals("unknown")) {
			// TODO handle UUID
			result.add(new FaceIndexResultEntry((float) bestDistance, bestLabel, null));
		}
		return result;
	}

	@Override
	public void reindex() throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void purge() throws IOException {
		knownFaces.clear();
	}

}
