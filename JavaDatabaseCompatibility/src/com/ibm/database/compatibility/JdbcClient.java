package com.ibm.database.compatibility;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A single client accessing a database using JDBC. A client can have multiple
 * {@link JdbcSession}s. Each session is associated with a single JDBC
 * {@link Connection}.
 * 
 */
public interface JdbcClient {

	public JdbcSession newSession(String url, String id) throws SQLException;

	/**
	 * Returns the JDBC JdbcSession associated with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public JdbcSession getJdbcSession(String id);

	/**
	 * Returns an array (as a copy) containing all of JDBC sessions associated
	 * with this client.
	 * 
	 * @return
	 */
	public JdbcSession[] getJdbcSessions();

	/**
	 * Manually adds a new JDBC session to to this client.
	 * 
	 * @param id
	 * @param jdbcSession
	 * @return
	 */
	public JdbcSession putJdbcSession(String id, JdbcSession jdbcSession);

	public JdbcSession removeJdbcSession(String id);

	public Connection newConnection(String url) throws SQLException;

}
