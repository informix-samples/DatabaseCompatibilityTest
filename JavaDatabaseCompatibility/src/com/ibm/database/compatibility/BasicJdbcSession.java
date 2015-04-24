package com.ibm.database.compatibility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class BasicJdbcSession implements JdbcSession {
	
	private static final String DEFAULT_ID = "default";
	
	private final JdbcClient client;
	private final String sessionId;
	private final String credentialsId;
	private Connection connection;
	private String lastStatementId = DEFAULT_ID;
	private final Map<String, Statement> statements = new HashMap<String, Statement>();
	private String lastPreparedStatementId = DEFAULT_ID;
	private final Map<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	private String lastResultSetId = DEFAULT_ID;
	private final Map<String, ResultSet> resultSets = new HashMap<String, ResultSet>();

	BasicJdbcSession(JdbcClient client, String sessionId, String credentialsId) {
		this.client = client;
		this.sessionId = sessionId;
		this.credentialsId = credentialsId;
	}
	
	@Override
	public synchronized Connection getConnection() throws SQLException {
		if (connection == null) {
			final DatabaseCredential credentials = client.getDatabaseCredential(this.credentialsId);
			this.connection = client.newConnection(credentials.getUrl(), credentials.getUser(), credentials.getPassword());
		}
		return this.connection;
	}

	@Override
	public String getId() {
		return this.sessionId;
	}
	
	@Override
	public synchronized void startTransaction() throws SQLException {
		getConnection().setAutoCommit(false);
	}

	@Override
	public synchronized void commitTransaction() throws SQLException {
		Connection c = getConnection();
		c.commit();
		c.setAutoCommit(true);
	}
	
	@Override
	public synchronized void rollbackTransaction() throws SQLException {
		Connection c = getConnection();
		c.rollback();
		c.setAutoCommit(true);
	}

	@Override
	public synchronized Statement createStatement(String id) throws SQLException {
		if (id == null) {
			id = lastStatementId;
		} else {
			lastStatementId = id;
		}
		Connection c = getConnection();
		Statement stmt = c.createStatement();
		this.statements.put(id, stmt);
		return stmt;
	}
	
	@Override
	public synchronized Statement getStatement(String id) {
		if (id == null) {
			id = lastStatementId;
		} else {
			lastStatementId = id;
		}
		return this.statements.get(id);
	}

	@Override
	public synchronized Statement putStatement(String id, Statement s) {
		if (id == null) {
			id = lastStatementId;
		} else {
			lastStatementId = id;
		}
		return this.statements.put(id, s);
	}

	@Override
	public synchronized Statement removeStatement(String id) {
		if (id == null) {
			id = lastStatementId;
		} else {
			lastStatementId = id;
		}
		Statement s = this.statements.remove(id);
		if (s != null) {
			try {
				s.close();
			} catch (Exception e) {
				// do nothing
			}
		}
		return s;
	}

	@Override
	public synchronized PreparedStatement getPreparedStatement(String id) {
		if (id == null) {
			id = lastPreparedStatementId;
		} else {
			lastPreparedStatementId = id;
		}
		return preparedStatements.get(id);
	}

	@Override
	public synchronized PreparedStatement putPreparedStatement(String id, PreparedStatement ps) {
		if (id == null) {
			id = lastPreparedStatementId;
		} else {
			lastPreparedStatementId = id;
		}
		return this.preparedStatements.put(id, ps);
	}

	@Override
	public synchronized PreparedStatement removePreparedStatement(String id) {
		if (id == null) {
			id = lastPreparedStatementId;
		} else {
			lastPreparedStatementId = id;
		}
		PreparedStatement ps = this.preparedStatements.remove(id);
		if (ps != null) {
			try {
				ps.close();
			} catch (Exception e) {
				// do nothing
			}
		}
		return ps;
	}

	@Override
	public synchronized ResultSet getResultSet(String id) {
		if (id == null) {
			id = lastResultSetId;
		} else {
			lastResultSetId = id;
		}
		return resultSets.get(id);
	}

	@Override
	public synchronized ResultSet putResultSet(String id, ResultSet resultSet) {
		if (id == null) {
			id = lastResultSetId;
		} else {
			lastResultSetId = id;
		}
		return this.resultSets.put(id, resultSet);
	}

	@Override
	public synchronized ResultSet removeResultSet(String id) {
		if (id == null) {
			id = lastResultSetId;
		} else {
			lastResultSetId = id;
		}
		ResultSet rs = this.resultSets.remove(id);
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
				// do nothing
			}
		}
		return rs;
	}

	@Override
	public void close() throws IOException {
		try {
			this.connection.close();
		} catch (Exception e) {
			throw new IOException("Unable to close JDBC connection", e);
		}
	}

	@Override
	public synchronized String getLastStatementId() {
		return this.lastStatementId;
	}

	@Override
	public synchronized String getLastPreparedStatementId() {
		return this.lastPreparedStatementId;
	}

	@Override
	public synchronized String getLastResultSetId() {
		return this.lastResultSetId;
	}

}
