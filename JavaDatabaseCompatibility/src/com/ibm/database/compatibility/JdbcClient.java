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

	/**
	 * Creates a new JDBC session to the database specified by the url.
	 * 
	 * @param url
	 * @return
	 * @throws SQLException
	 */
	public JdbcSession newSession(String url) throws SQLException;

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

	/**
	 * Removes a session from this client. This automatically closes the JDBC
	 * session.
	 * 
	 * @param id
	 * @return
	 */
	public JdbcSession removeJdbcSession(String id);

	/**
	 * Creates a new JDBC connection to the specified url.
	 * 
	 * @param url
	 *            the JDBC url to connect to
	 * @return
	 * @throws SQLException
	 */
	public Connection newConnection(String url) throws SQLException;

	/**
	 * Creates a new database credential object.
	 * 
	 * @param credentialId
	 * 			the name of the credentials 
	 * @return
	 */
	public DatabaseCredential newCredential(String credentialId);

	/**
	 * Closes a new Database Credential to the specified credential object
	 * 
	 * @param credentialId
	 * 			the name of the credentials
	 * @return
	 */
	public DatabaseCredential removeCredential(String credentialId);

	/**
	 * Returns the Database Credential associated with the specified id
	 * 
	 * @param credentialId
	 * 			the name of the credentials
	 * @return
	 */
	public DatabaseCredential getDatabaseCredential(String credentialId);
	
}
