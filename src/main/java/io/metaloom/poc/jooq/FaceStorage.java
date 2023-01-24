package io.metaloom.poc.jooq;

import static io.metaloom.loom.db.jooq.tables.Face.FACE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import io.metaloom.loom.db.jooq.tables.Face;
import io.metaloom.loom.db.jooq.tables.records.FaceRecord;
import io.metaloom.poc.FaceReference;
import io.metaloom.poc.util.FaceDetectionUtils;

public class FaceStorage implements AutoCloseable {

	private Connection conn;

	private DSLContext ctx;

	public FaceStorage() {
	}

	public void connect(String url, String username, String password) throws SQLException {
		conn = DriverManager.getConnection(url, username, password);
		ctx = DSL.using(conn, SQLDialect.POSTGRES);
	}

	@Override
	public void close() throws Exception {
		if (conn != null) {
			conn.close();
			ctx = null;
		}
	}

	public void storeFace(FaceReference face) {
		assertOpen();

		ctx.transaction(t -> {
			byte[] data = FaceDetectionUtils.floatsToBytes(face.vector());
			ctx.insertInto(FACE)
				.columns(FACE.UUID, FACE.BLOB, FACE.SOURCE, FACE.FACE_NR)
				.values(face.uuid(), data, face.source(), 1)
				.execute();
		});

	}

	public void loadFaces(Consumer<FaceReference> consumer) {
		assertOpen();
		Result<Record> faces = ctx.select()
			.from(Face.FACE)
			.fetch();

		faces.forEach(face -> {
			// Convert the record into a POJO for our business logic
			FaceReference faceRef = FaceDetectionUtils.toReference(face);
			consumer.accept(faceRef);
		});

	}

	public FaceReference loadFace(UUID uuid) {
		FaceRecord record = ctx.fetchOne(FACE, FACE.UUID.eq(uuid));
		return FaceDetectionUtils.toReference(record);
	}

	private void assertOpen() {
		Objects.requireNonNull(conn, "The needed connection could not be found. Please connect before usage.");
		Objects.requireNonNull(ctx, "The needed context could not be found. Please connect before usage.");
	}

	public void purge() {
		assertOpen();
		ctx.delete(FACE).execute();
	}

}
