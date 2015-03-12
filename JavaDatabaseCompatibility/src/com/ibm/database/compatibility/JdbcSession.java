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
	
	public String getLastStatementId();
	
	public String getLastPreparedStatementId();
	
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
	 * @return
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

	public PreparedStatement removePreparedStatement(String id);

	/**
	 * Returns the JDBC ResultSet associated with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public ResultSet getResultSet(String id);

	public ResultSet putResultSet(String id, ResultSet resultSet);

	public ResultSet removeResultSet(String id);

}
