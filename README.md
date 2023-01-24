# Video4j Face Recognition - Proof Of Concept

This project contains a proof of concept implementation for a Face Recognition implementation which uses the Apache Lucene Hierarchical Navigable Small World graph for ANN search of extracted face embeddings. It also features a jOOQ based storage implementation for extracted embeddings.

The source image / video will be parsed by [video4j-facedetect](https://github.com/metaloom/video4j-facedetect) which used [openpnp/opencv](https://github.com/openpnp/opencv) and [jdlib](https://github.com/metaloom/jdlib) to extract the face embeddings. These embeddings will be stored via jOOQ in Postgres for save keeping. The implemented `LuceneFaceIndexer` can be used to populate the lucene index by streaming the results from the postgres database via jOOQ. Flyway is used for database migration and setup.


## Building

At this point the project still requires some changes to upstream libraries which have not yet been published. This fact will make it hard to build the project locally.

## Example

```
try (FaceStorage storage = new FaceStorage()) {
    storage.connect(URL, USERNAME, PASSWORD);
    Facerecognition facerecognition = new StoringLuceneFacerecognitionImpl(INDEX_PATH, 0.60f, storage);

    // Learn a new face and store it in the database + lucene
    facerecognition.learn("Robert Beltran #1", "voy_cast/robert-beltran-1.jpg");
    …

    // Or recreate the lucene index from the database
    facerecognition.reindex();

    // Finally run a video and test the face recognition.
    facerecognition.testVideo(Paths.get("Live Fast And Prosper.mkv"));

}
```

## Tasks

* Use testcontainers for testing
* Add gender,age,mood columns for extracted faces
* Add support for different CNN models
* Fix building issues
* Rethink UUID usage
* Add label support?