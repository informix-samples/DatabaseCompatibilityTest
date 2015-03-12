package com.ibm.database.compatibility;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface JdbcSession extends Closeable {

	/**
	 * Returns this session's id.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Returns the id of the last statement used by this session.
	 * 
	 * @return the id of the last statement used by this session
	 */
	public String getLastStatementId();

	/**
	 * Returns the id of the last prepared statement used by this session.
	 * 
	 * @return the id of the last prepared statement used by this session
	 */
	public String getLastPreparedStatementId();

	/**
	 * Returns the id of the last result set used by this session.
	 * 
	 * @return the id of the last result set used by this session
	 */
	public String getLastResultSetId();

	/**
	 * Returns the JDBC connection associated with this session.
	 * 
	 * @return the JDBC connection associated with this session
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException;

	/**
	 * Creates a new JDBC statement with the specified id. If the
	 * <code>id</code> is null, it is assumed to be the last statement id used
	 * with this session.
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public Statement createStatement(String id) throws SQLException;

	/**
	 * Retrieves the JDBC statement associated with the specified id. If the
	 * <code>id</code> is null, it is assumed to be the last statement id used
	 * with this session.
	 * 
	 * @param id
	 * @return
	 */
	public Statement getStatement(String id);

	/**
	 * Manually adds a JDBC statement to the map of those associated with this
	 * session. If the <code>id</code> is <code>null</code>, it is assumed to be
	 * the last statement id used with this session.
	 * 
	 * @param id
	 * @param s
	 * @return
	 */
	public Statement putStatement(String id, Statement s);

	/**
	 * Removes a JDBC statement from the map of those associated with this
	 * session. If the <code>id</code> is <code>null</code>, it is assumed to be
	 * the last statement id used with this session.
	 * 
	 * @param id
	 *            the statement to remove from this session
	 * @return the statement that was removed or <code>null</code> if there was
	 *         no statement with the specified id
	 */
	public Statement removeStatement(String id);

	/**
	 * Returns the JDBC PreparedStatement associated with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public PreparedStatement getPreparedStatement(String id);

	public PreparedStatement putPreparedStatement(String id, PreparedStatement ps);

	/**
	 * Dissociates the prepared statement referenced by the specified id from
	 * this session. This automatically closes the prepared statement.
	 * 
	 * @param id
	 *            the prepared statement to remove from this session
	 * @return the prepared statement that was removed or <code>null</code> if
	 *         there was no prepared statement with the specified id
	 */
	public PreparedStatement removePreparedStatement(String id);

	/**
	 * Returns the JDBC ResultSet associated with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public ResultSet getResultSet(String id);

	public ResultSet putResultSet(String id, ResultSet resultSet);

	/**
	 * Dissociates the result set referenced by the specified id from this
	 * session. This automatically closes the result set.
	 * 
	 * @param id
	 *            the result set to remove from this session
	 * @return the result set that was removed or <code>null</code> if there was
	 *         no result set with the specified id
	 */
	public ResultSet removeResultSet(String id);

}
