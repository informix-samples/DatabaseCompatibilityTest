package com.ibm.database.compatibility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BasicJdbcSession implements JdbcSession {

	private final JdbcClient client;
	private final String id;
	private final String url;
	private Connection connection;
	private final Map<String, Statement> statements = new ConcurrentHashMap<String, Statement>();
	private final Map<String, PreparedStatement> preparedStatements = new ConcurrentHashMap<String, PreparedStatement>();
	private final Map<String, ResultSet> resultSets = new ConcurrentHashMap<String, ResultSet>();

	BasicJdbcSession(JdbcClient client, String id, String url) {
		this.client = client;
		this.id = id;
		this.url = url;
		
	}
	
	@Override
	public synchronized Connection getConnection() throws SQLException {
		if (connection == null) {
			this.connection = client.newConnection(url);
		}
		return this.connection;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Statement createStatement(String id) throws SQLException {
		if (id == null) {
			id = DEFAULT_ID;
		}
		Connection c = getConnection();
		Statement stmt = c.createStatement();
		this.statements.put(id, stmt);
		return stmt;
	}
	
	@Override
	public Statement getStatement(String id) {
		if (id == null) {
			id = DEFAULT_ID;
		}
		return this.statements.get(id);
	}

	@Override
	public Statement putStatement(String id, Statement s) {
		if (id == null) {
			id = DEFAULT_ID;
		}
		return this.statements.put(id, s);
	}

	@Override
	public Statement removeStatement(String id) {
		if (id == null) {
			id = DEFAULT_ID;
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
	public PreparedStatement getPreparedStatement(String id) {
		if (id == null) {
			id = DEFAULT_ID;
		}
		return preparedStatements.get(id);
	}

	@Override
	public PreparedStatement putPreparedStatement(String id, PreparedStatement ps) {
		if (id == null) {
			id = DEFAULT_ID;
		}
		return this.preparedStatements.put(id, ps);
	}

	@Override
	public PreparedStatement removePreparedStatement(String id) {
		if (id == null) {
			id = DEFAULT_ID;
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
	public ResultSet getResultSet(String id) {
		if (id == null) {
			id = DEFAULT_ID;
		}
		return resultSets.get(id);
	}

	@Override
	public ResultSet putResultSet(String id, ResultSet resultSet) {
		if (id == null) {
			id = DEFAULT_ID;
		}
		return this.resultSets.put(id, resultSet);
	}

	@Override
	public ResultSet removeResultSet(String id) {
		if (id == null) {
			id = DEFAULT_ID;
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

}
