package com.ibm.database.compatibility;

import java.io.IOException;

public interface OperationWriter {
	
	public void write(Operation op) throws IOException;
	
	public void writeComment(String comment) throws IOException;

}
