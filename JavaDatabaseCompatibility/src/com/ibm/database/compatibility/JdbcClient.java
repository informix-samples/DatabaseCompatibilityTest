package com.ibm.database.compatibility;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcClient {
	
	public static final String DEFAULT_ID = "default";
	
	public JdbcSession newSession(String url, String id) throws SQLException;
	
	/**
	 * Returns the JDBC JdbcSession associated with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public JdbcSession getJdbcSession(String id);

	public JdbcSession putJdbcSession(String id, JdbcSession c);

	public JdbcSession removeJdbcSession(String id);

	public Connection newConnection(String url) throws SQLException;

}
