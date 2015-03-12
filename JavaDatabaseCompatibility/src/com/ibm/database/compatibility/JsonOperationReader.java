package com.ibm.database.compatibility;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class JsonOperationReader implements OperationReader, Closeable {

	private final Reader reader;
	private final BufferedReader bufferedReader;
	private String nextLine = null;

	public JsonOperationReader(String pathToFile) throws FileNotFoundException {
		this(new FileReader(pathToFile));
	}

	public JsonOperationReader(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	public JsonOperationReader(Reader reader) {
		this.reader = reader;
		this.bufferedReader = new BufferedReader(reader);
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
			advanceNextLine();
		}
		return op;
	}

	private void advanceNextLine() {
		try {
			while (true) {
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
