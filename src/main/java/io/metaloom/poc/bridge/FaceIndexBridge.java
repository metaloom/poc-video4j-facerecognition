package io.metaloom.poc.bridge;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.poc.jooq.FaceStorage;
import io.metaloom.poc.lucene.LuceneFaceIndexer;

/**
 * Bridge between RDBS and lucene index. It loads the embedding from the database using jOOQ and stores it in the lucene index.
 */
public class FaceIndexBridge {

	public static final Logger log = LoggerFactory.getLogger(FaceIndexBridge.class);

	private final LuceneFaceIndexer indexer;
	private final FaceStorage storage;

	public FaceIndexBridge(LuceneFaceIndexer indexer, FaceStorage storage) {
		this.indexer = indexer;
		this.storage = storage;
	}

	/**
	 * Store all faces from the database in the lucene index.
	 * 
	 * @throws IOException
	 */
	public void index() throws IOException {
		indexer.writer(w -> {
			AtomicLong counter = new AtomicLong();
			long start = System.currentTimeMillis();
			try {
				// Stream faces from the storage and add them in the lucene index
				storage.loadFaces(face -> {
					try {
						indexer.index(w, face);
						counter.incrementAndGet();
					} catch (Exception e) {
						e.printStackTrace();
					}
					// Commit the index in specific intervals to disk
					try {
						if (counter.get() % 500 == 0) {
							w.flush();
						}
						if (counter.get() % 2000 == 0) {
							w.commit();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// A final flush and commit
				w.flush();
				w.commit();
			} catch (IOException e) {
				e.printStackTrace();
			}
			long end = System.currentTimeMillis() - start;
			log.info("[Bridge] Index operation completed in: " + end + "ms - Added " + counter.get() + " document to the index");
		});
	}

}
