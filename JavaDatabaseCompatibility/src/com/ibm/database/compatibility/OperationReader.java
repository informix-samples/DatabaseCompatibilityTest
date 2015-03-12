package com.ibm.database.compatibility;

public interface OperationReader {
	
	public boolean hasNext();
	
	public Operation next();

}
