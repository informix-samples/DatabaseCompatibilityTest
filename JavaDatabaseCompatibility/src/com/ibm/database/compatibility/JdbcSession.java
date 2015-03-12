package com.ibm.database.compatibility;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface JdbcSession extends Closeable {

	public static final String DEFAULT_ID = "default";
	
	/**
	 * Returns this session's id.
	 * 
	 * @return
	 */
	public String getId();

	public Connection getConnection() throws SQLException;

	public Statement createStatement(String id) throws SQLException;
	
	public Statement getStatement(String id);

	public Statement putStatement(String id, Statement s);

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
