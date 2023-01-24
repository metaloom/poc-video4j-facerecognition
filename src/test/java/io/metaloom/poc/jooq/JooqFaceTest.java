package io.metaloom.poc.jooq;

import static io.metaloom.loom.db.jooq.tables.Face.FACE;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.Test;

import io.metaloom.loom.db.jooq.tables.Face;
import io.metaloom.loom.db.jooq.tables.records.FaceRecord;
import io.metaloom.poc.AbstractFacedetectionTest;

public class JooqFaceTest extends AbstractFacedetectionTest {

	@Test
	public void testJooqFace() {
		try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
			DSLContext ctx = DSL.using(conn, SQLDialect.POSTGRES);

			// Clear DB
			ctx.delete(FACE).execute();

			ctx.transaction(t -> {

				for (int i = 0; i < 100; i++) {
					byte[] data = ("Test " + i).getBytes();
					assertEquals(1, ctx.insertInto(FACE)
						.columns(FACE.BLOB, FACE.SOURCE, FACE.FACE_NR)
						.values(data, "nix w" + i, 1)
						.execute());
				}

			});

			UUID newUuid = UUID.randomUUID();
			FaceRecord record = ctx.fetchOne(FACE, FACE.UUID.eq(newUuid));
			System.out.println(record);

			// record.store()
			Result<Record> faces = ctx.select()
				.from(Face.FACE)
				.fetch();

			faces.forEach(face -> {
				UUID uuid = face.getValue(FACE.UUID);
				String meta = face.getValue(FACE.META);
				byte[] embedding = face.getValue(FACE.BLOB);

				System.out.println("Got " + uuid + " " + meta + " " + new String(embedding));
			});
			// For the sake of this tutorial, let's keep exception handling simple
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
