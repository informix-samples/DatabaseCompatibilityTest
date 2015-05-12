package com.ibm.database.compatibility;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public class TestResultWriter implements Flushable, Closeable {
	
	private final BufferedWriter bufferedWriter;
	
	public TestResultWriter(String pathToFile) throws IOException {
		this(new FileWriter(pathToFile));
	}

	public TestResultWriter(File file) throws IOException {
		this(new FileWriter(file));
	}

	public TestResultWriter(Writer writer) {
		this.bufferedWriter = new BufferedWriter(writer);
	}
	
	public void write(TestResult result) throws IOException {
		bufferedWriter.write(GsonUtils.newGson().toJson(result));
		bufferedWriter.newLine();
	}

	@Override
	public void close() throws IOException {
		bufferedWriter.close();
	}

	@Override
	public void flush() throws IOException {
		bufferedWriter.flush();		
	}

}
