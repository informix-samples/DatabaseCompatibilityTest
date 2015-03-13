package com.ibm.database.compatibility;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public class JsonOperationWriter implements OperationWriter, Flushable, Closeable {
	
	private final Writer writer;
	private final BufferedWriter bufferedWriter;
	
	public JsonOperationWriter(String pathToFile) throws IOException {
		this(new FileWriter(pathToFile));
	}

	public JsonOperationWriter(File file) throws IOException {
		this(new FileWriter(file));
	}

	public JsonOperationWriter(Writer writer) {
		this.writer = writer;
		this.bufferedWriter = new BufferedWriter(writer);
	}
	
	@Override
	public void write(Operation op) throws IOException {
		bufferedWriter.write(GsonUtils.newGson().toJson(op));
		bufferedWriter.newLine();
	}

	@Override
	public void writeComment(String comment) throws IOException {
		String[] lines = comment.split("\n");
		for (String line : lines) {
			bufferedWriter.write("# ");
			bufferedWriter.write(line);
			bufferedWriter.newLine();
		}
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
