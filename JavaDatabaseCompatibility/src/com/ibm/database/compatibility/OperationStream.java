package com.ibm.database.compatibility;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface OperationStream extends Closeable {
	
	public boolean hasNext();
	
	public Operation next();

}
