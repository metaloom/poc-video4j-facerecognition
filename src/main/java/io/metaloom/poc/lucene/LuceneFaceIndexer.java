package io.metaloom.poc.lucene;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.lucene.index.IndexWriter;

import io.metaloom.poc.FaceReference;

public interface LuceneFaceIndexer {

	public static final String VECTOR_FIELD_KEY = "embedding";

	public static final String SOURCE_FIELD_KEY = "source";

	public static final String UUID_FIELD_KEY = "uuid";

	/**
	 * Query the index using the provided face.
	 * 
	 * @param face
	 * @param limit
	 *            Limit the result size
	 * @param scoreThreshold
	 *            Limit the results to a specific score - Only results above the score will be returned
	 * @return
	 * @throws IOException
	 */
	FaceIndexResult query(FaceReference face, int limit, float scoreThreshold) throws IOException;

	void writer(Consumer<IndexWriter> consumer) throws IOException;

	/**
	 * Add the face to the index using the provided writer. Use {@link #writer(Consumer)} to get the writer.
	 * 
	 * @param writer
	 * @param face
	 * @throws IOException
	 */
	void index(IndexWriter writer, FaceReference face) throws IOException;

	/**
	 * Purge all document from the index.
	 * 
	 * @throws IOException
	 */
	void purge() throws IOException;

}
