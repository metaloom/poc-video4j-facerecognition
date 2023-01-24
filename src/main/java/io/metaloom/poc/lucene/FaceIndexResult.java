package io.metaloom.poc.lucene;

import java.util.ArrayList;
import java.util.List;

public class FaceIndexResult {

	private final List<FaceIndexResultEntry> results = new ArrayList<>();

	public void add(FaceIndexResultEntry entry) {
		results.add(entry);
	}

	public List<FaceIndexResultEntry> getResults() {
		return results;
	}

	public List<FaceIndexResultEntry> faces() {
		return results;
	}

}
