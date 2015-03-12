package com.ibm.database.compatibility;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JsonOperationStream implements OperationStream, Closeable {

	private final Reader reader;
	private final BufferedReader bufferedReader;
	private String nextLine = null;
	
	public JsonOperationStream(String pathToFile) throws FileNotFoundException {
		this(new FileReader(pathToFile));
	}
	
	public JsonOperationStream(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}
	
	public JsonOperationStream(Reader reader) {
		this.reader = reader;
		this.bufferedReader = new BufferedReader(reader);
		try {
			this.nextLine = this.bufferedReader.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Error reading next line from file", e);
		}
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
			try {
				this.nextLine = this.bufferedReader.readLine();
			} catch (IOException e) {
				throw new RuntimeException("Error reading next line from file", e);
			}
		}
		return op;
	}

	@Override
	public void close() throws IOException {
		if (this.bufferedReader != null) {
			this.bufferedReader.close();
		}
	}

}
