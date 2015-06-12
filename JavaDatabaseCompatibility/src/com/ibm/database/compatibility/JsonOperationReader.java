package com.ibm.database.compatibility;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JsonOperationReader implements OperationReader, Closeable {

	private final BufferedReader bufferedReader;
	private String nextLine = null;
	
	private int lineNumber = 0;

	public JsonOperationReader(String pathToFile) throws FileNotFoundException {
		if (System.getenv().containsKey("VCAP_SERVICES")) {
			this.bufferedReader = new BufferedReader(new InputStreamReader(JsonOperationReader.class.getResourceAsStream(pathToFile)));
		} else {
			this.bufferedReader = new BufferedReader(new FileReader(pathToFile));
		}
		advanceNextLine();
	}

	@Override
	public boolean hasNext() {
		return this.nextLine != null;
	}

	@Override
	public Operation next() {
		Operation op = null;
		if (this.nextLine != null) {
			op = Operation.fromJson(this.nextLine);
			op.setLine(lineNumber);
			advanceNextLine();
		}
		return op;
	}

	private void advanceNextLine() {
		try {
			while (true) {
				lineNumber++;
				this.nextLine = this.bufferedReader.readLine();
				if (this.nextLine == null || !this.nextLine.startsWith("#")) {
					return;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error reading next line from file", e);
		}
	}

	@Override
	public void close() throws IOException {
		if (this.bufferedReader != null) {
			this.bufferedReader.close();
		}
	}
}
