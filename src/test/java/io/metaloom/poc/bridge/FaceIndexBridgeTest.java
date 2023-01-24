package io.metaloom.poc.bridge;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import io.metaloom.poc.AbstractFacedetectionTest;
import io.metaloom.poc.FaceReference;
import io.metaloom.poc.jooq.FaceStorage;
import io.metaloom.poc.lucene.FaceIndexResult;
import io.metaloom.poc.lucene.FaceIndexResultEntry;
import io.metaloom.poc.lucene.LuceneFaceIndexer;
import io.metaloom.poc.lucene.impl.LuceneFaceIndexerImpl;

public class FaceIndexBridgeTest extends AbstractFacedetectionTest {

	@Before
	public void setup() throws IOException {
		FileUtils.deleteDirectory(INDEX_PATH.toFile());
	}

	@Test
	public void testBridge() throws Exception {
		try (FaceStorage storage = new FaceStorage()) {
			storage.connect(URL, USERNAME, PASSWORD);

			LuceneFaceIndexer indexer = new LuceneFaceIndexerImpl(INDEX_PATH);
			FaceIndexBridge bridge = new FaceIndexBridge(indexer, storage);
			bridge.index();

			// Now load a known face
			UUID uuid = UUID.fromString("e4a007d5-2191-49c9-aada-67861b6c1d14");
			FaceReference knownFace = storage.loadFace(uuid);

			// And run the query against the index
			FaceIndexResult result = indexer.query(knownFace, 5, 0.60f);
			for (FaceIndexResultEntry face : result.faces()) {
				System.out.println(face);
			}
		}
	}
}
