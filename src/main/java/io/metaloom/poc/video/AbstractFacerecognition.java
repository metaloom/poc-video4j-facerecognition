package io.metaloom.poc.video;

import static io.metaloom.poc.util.FaceDetectionUtils.convertFloatsToDoubles;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.poc.FaceReference;
import io.metaloom.poc.lucene.FaceIndexResult;
import io.metaloom.poc.lucene.FaceIndexResultEntry;
import io.metaloom.utils.FilterHelper;
import io.metaloom.video.facedetect.Face;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.FacedetectorMetrics;
import io.metaloom.video.facedetect.dlib.impl.DLibFacedetector;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.ImageUtils;
import io.metaloom.video4j.utils.VideoUtils;

public abstract class AbstractFacerecognition implements Facerecognition {

	public static final Logger log = LoggerFactory.getLogger(AbstractFacerecognition.class);

	static {
		Video4j.init();
	}

	protected final DLibFacedetector detector;

	public AbstractFacerecognition() throws FileNotFoundException {
		this.detector = DLibFacedetector.create();
		this.detector.setMinFaceHeightFactor(0.05f);
	}

	@Override
	public void testVideoDirectory(Path folderPath) throws IOException {
		if (!Files.isDirectory(folderPath)) {
			throw new RuntimeException("The provided path " + folderPath + " does not exist or is not a folder");
		}

		List<Path> list = streamMediaFiles(folderPath).collect(Collectors.toList());
		Collections.shuffle(list);
		Collections.shuffle(list);
		Collections.shuffle(list);
		list.stream().forEach(videoFile -> {
			try {
				testVideo(videoFile);
			} catch (FileNotFoundException e) {
				System.err.println("Error while processing file " + videoFile);
				e.printStackTrace();
			}
		});
	}

	@Override
	public void testVideo(Path path) throws FileNotFoundException {
		if (!Files.isRegularFile(path)) {
			throw new FileNotFoundException("The provided path " + path + "  does not exist or is not a file");
		}
		String pathStr = path.normalize().toString();
		System.out.println(pathStr);

		FacedetectorMetrics metrics = FacedetectorMetrics.create();
		try (Video video = Videos.open(pathStr)) {
			video.seekToFrameRatio(0.20f);
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				.map(frame -> {
					// CVUtils.boxFrame2(frame, 512);
					return frame;
				})
				.map(detector::detect)
				// .filter(FaceVideoFrame::hasFace)
				.map(metrics::track)
				.map(detector::markFaces)
				.map(detector::markLandmarks)
				.map(this::markRecognitionLabel)
				.map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)));
			// .map(frame -> {
			// return Facedetection.cropToFace(frame, 0);
			// });
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}

	public static Stream<Path> streamMediaFiles(Path path) throws IOException {
		return Files.walk(path)
			.filter(Files::isRegularFile)
			.filter(FilterHelper::isVideo)
			.filter(FilterHelper::notEmpty);
	}

	@Override
	public FaceReference learn(String source, String imagePath) throws IOException {
		File imageFile = new File(imagePath);
		if (!imageFile.exists()) {
			throw new FileNotFoundException("Could not find image {" + imagePath + "} for source {" + source + "}");
		}
		List<? extends Face> faces = detector.detect(ImageUtils.load(imageFile));
		if (faces.isEmpty()) {
			System.out.println("No face detected in " + imagePath + " for label " + source);
		}
		if (faces.size() > 1) {
			System.out.println("More than one face was detected in image " + imagePath + " for label " + source);
		}
		Face face = faces.get(0);
		storeFace(source, face);

		// TODO handle UUID
		UUID uuid = UUID.randomUUID();
		float[] vector = face.getEmbeddings();
		return new FaceReference(uuid, source, vector);
	}

	@Override
	public void testImage(Path imagePath) throws IOException {
		List<? extends Face> faces = detector.detect(ImageUtils.load(imagePath.toFile()));
		for (Face face : faces) {
			double[] embeddings = convertFloatsToDoubles(face.getEmbeddings());
			if (embeddings == null) {
				System.out.println("Skipping face - no valid embeddings");
				continue;
			}

			FaceIndexResult result = query(face);
			if (!result.faces().isEmpty()) {
				FaceIndexResultEntry best = result.faces().get(0);
				String bestSource = best.source();
				System.out.println(bestSource + " d: " + String.format("%.4f", best.score()));
			}
			// double fontScale = 1.0f;
			// Scalar color = new Scalar(255, 255, 255);
			// CVUtils.drawText(frame, bestSource + " d: " + String.format("%.2f", bestDistance),
			// new org.opencv.core.Point(face.start().x, face.start().y), fontScale, color, 1);
		}
	}

	private FaceVideoFrame markRecognitionLabel(FaceVideoFrame frame) {
		for (Face face : frame.faces()) {
			float[] embeddings = face.getEmbeddings();
			// Skip faces which were detected but no embeddings could be extraced
			if (embeddings == null) {
				continue;
			}
			String bestSource = "unknown";
			double bestScore = Double.MAX_VALUE;
			try {
				FaceIndexResult result = query(face);
				if(!result.faces().isEmpty()) {
					FaceIndexResultEntry best = result.faces().get(0);
					bestSource = best.source();
					bestScore = best.score();
				}
			} catch (Exception e) {
				log.error("Error while checking face", e);
			}
			double fontScale = 1.2f;
			if (bestSource.equalsIgnoreCase("unknown")) {
				Scalar color = new Scalar(255, 255, 255);
				String info = bestSource + " d: " + String.format("%.2f", bestScore);
				if (bestScore > 1) {
					info = bestSource;
				}
				CVUtils.drawText(frame, info,
					new org.opencv.core.Point(face.start().x, face.start().y - 4), fontScale, color, 1);
			} else {
				Scalar color = new Scalar(0, 255, 0);
				String info = bestSource + " d: " + String.format("%.2f", bestScore);
				if (bestScore > 1) {
					info = bestSource;
				}
				CVUtils.drawText(frame, info,
					new org.opencv.core.Point(face.start().x, face.start().y - 4), fontScale, color, 1);
				System.out.println(bestSource + info);
			}
		}
		return frame;
	}

	protected abstract void storeFace(String source, Face face) throws IOException;

	protected abstract FaceIndexResult query(Face face) throws IOException;
	

}
