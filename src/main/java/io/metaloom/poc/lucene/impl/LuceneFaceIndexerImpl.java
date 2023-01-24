package io.metaloom.poc.lucene.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.KnnVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnVectorQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.poc.FaceReference;
import io.metaloom.poc.lucene.FaceIndexResult;
import io.metaloom.poc.lucene.FaceIndexResultEntry;
import io.metaloom.poc.lucene.LuceneFaceIndexer;

public class LuceneFaceIndexerImpl implements LuceneFaceIndexer {

	public static final Logger log = LoggerFactory.getLogger(LuceneFaceIndexerImpl.class);

	private final Path indexPath;

	/**
	 * Create a new indexer
	 * 
	 * @param indexPath
	 *            Path to the lucene index location
	 */
	public LuceneFaceIndexerImpl(Path indexPath) {
		this.indexPath = indexPath;
	}

	@Override
	public void index(IndexWriter w, FaceReference face) throws IOException {
		Document doc = new Document();
		doc.add(new StoredField(UUID_FIELD_KEY, face.uuid().toString()));
		doc.add(new StoredField(SOURCE_FIELD_KEY, face.source()));
		doc.add(new KnnVectorField(VECTOR_FIELD_KEY, face.vector()));
		w.addDocument(doc);
	}

	@Override
	public void writer(Consumer<IndexWriter> consumer) throws IOException {
		try (MMapDirectory dir = new MMapDirectory(indexPath)) {
			try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig())) {
				consumer.accept(writer);
			}
		}
	}

	@Override
	public FaceIndexResult query(FaceReference embedding, int limit, float scoreThreshold) throws IOException {
		try (MMapDirectory dir = new MMapDirectory(indexPath)) {
			try (IndexReader reader = DirectoryReader.open(dir)) {
				return queryIndex(reader, embedding, limit, scoreThreshold);
			}
		}
	}

	private FaceIndexResult queryIndex(IndexReader reader, FaceReference embedding, int limit, float scoreThreshold) throws IOException {
		IndexSearcher searcher = new IndexSearcher(reader);

		// Check whether search should utilize the compacted vector form of the fingerprint.
		float[] vector = embedding.vector();
		TopDocs results = searcher.search(new KnnVectorQuery(VECTOR_FIELD_KEY, vector, limit), 10);
		return from(reader, results, scoreThreshold);
	}

	public FaceIndexResult from(IndexReader reader, TopDocs results, float scoreThreshold) {
		if (log.isDebugEnabled()) {
			log.debug("Hits: " + results.totalHits);
		}
		FaceIndexResult result = new FaceIndexResult();
		for (ScoreDoc sdoc : results.scoreDocs) {
			if (sdoc.score < scoreThreshold) {
				if (log.isTraceEnabled()) {
					log.trace("Omitting doc since its score is below our threshold of " + scoreThreshold);
				}
				continue;
			}
			try {
				Document doc = reader.document(sdoc.doc);
				IndexableField uuidField = doc.getField(UUID_FIELD_KEY);
				if (uuidField != null && uuidField instanceof StoredField) {
					StoredField storedUuidField = (StoredField) uuidField;

					String source = null;
					IndexableField sourceField = doc.getField(SOURCE_FIELD_KEY);
					if (sourceField != null && sourceField instanceof StoredField) {
						StoredField storedSourceField = (StoredField) sourceField;
						source = storedSourceField.stringValue();
					}
					if (log.isDebugEnabled()) {
						log.debug("Found: " + storedUuidField.stringValue() + " = " + String.format("%.1f", sdoc.score));
					}

					UUID uuid = UUID.fromString(storedUuidField.stringValue());
					result.add(new FaceIndexResultEntry(sdoc.score, source, uuid));
				} else {
					if (log.isWarnEnabled()) {
						log.warn("Document does not provide an id field. Will thus be omitted from query result.", doc);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;

	}

	@Override
	public void purge() throws IOException {
		try (MMapDirectory dir = new MMapDirectory(indexPath)) {
			try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig())) {
				writer.deleteAll();
			}
		}
	}

}
