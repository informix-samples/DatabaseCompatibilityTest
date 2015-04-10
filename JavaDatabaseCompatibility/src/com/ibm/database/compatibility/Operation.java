package com.ibm.database.compatibility;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class Operation {

	private static final Logger logger = LoggerFactory.getLogger(Operation.class);

	private String resource = null; // credential | session | statement | preparedStatement | resultSet
	private String action = null; // create | execute | close | startTransaction | commitTransaction | rollbackTransaction
	private String credentialId = null;
	private String sessionId = null;
	private String statementId = null;
	private String className = null;
	private String sql = null;
	private Binding[] bindings = null;
	private JsonArray expectedResults = null;
	
	private int errorCount = 0;
	private int line = 0;
	
	private Operation() {

	}

	
	
	public static class Builder {
		private String resource = null;
		private String action = null;
		private String credentialId = null;
		private String sessionId = null;
		private String statementId = null;
		private String className = null;
		private String sql = null;
		private Binding[] bindings = null;
		private JsonArray expectedResults = null;

		public Operation build() {
			Operation op = new Operation();
			op.resource = resource;
			op.action = action;
			op.credentialId = credentialId;
			op.sessionId = sessionId;
			op.statementId = statementId;
			op.className = className;
			op.sql = sql;
			op.bindings = bindings;
			op.expectedResults = expectedResults;
			return op;
		}

		public Builder resource(final String resource) {
			this.resource = resource;
			return this;
		}

		public Builder action(final String action) {
			this.action = action;
			return this;
		}
		
		public Builder credentialId(final String credentialId) {
			this.credentialId = credentialId;
			return this;
		}

		public Builder sessionId(final String sessionId) {
			this.sessionId = sessionId;
			return this;
		}

		public Builder statementId(final String statementId) {
			this.statementId = statementId;
			return this;
		}

		public Builder className(final String className) {
			this.className = className;
			return this;
		}

		public Builder sql(final String sql) {
			this.sql = sql;
			return this;
		}
		
		public Builder bindings(final Binding[] bindings) {
			this.bindings = new Binding[bindings.length];
			System.arraycopy(bindings, 0, this.bindings, 0, bindings.length);
			return this;
		}
		
		public Builder expectedResults(final JsonArray expectedResults) {
			this.expectedResults = expectedResults;
			return this;
		}
	}

	public void invoke(JdbcClient client) throws IOException, SQLException {
		this.errorCount = 0;
		if (resource.equalsIgnoreCase("credentials")) {
			if(action.equalsIgnoreCase("create"))	{
				/*-
				 * { "resource" : "credential" ,
				 *   "action" : "create" 
				 * }
				 * Actual server credential info should be picked up from the environment, e.g. VCAP services.
				 */
				DatabaseCredential credential = client.newCredential(credentialId);
				logger.debug("creating credential: id = {}, url = {}", credential.getCredentialId(), credential.getUrl());
			}
			if(action.equalsIgnoreCase("close")) {
				/*
				 * { "resource" : "credential" ,
				 *   "action" : "close" ,
				 *   "credentialId" : "mydb"
				 * }
				 */
				DatabaseCredential credential = client.removeCredential(credentialId);
				logger.debug("closing credential: id = {}, url = {}",credential.getCredentialId(), credential.getUrl() );
			}
		} else if (resource.equalsIgnoreCase("session")) {
			if (action.equalsIgnoreCase("create")) {
				// { resource: "createSession" , credentialsId , className = "com.informix.jdbc.IfxDriver" }
				if (className != null) {
					try {
						Class.forName(className);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(MessageFormat.format("Unable to load class {0}", className), e);
					}
				}
				JdbcSession session = client.newSession(sessionId);
				logger.debug("created new session id {}", session.getId());
			} else if (action.equalsIgnoreCase("close")) {
				JdbcSession session = client.removeJdbcSession(sessionId);
				logger.debug("closed session id {}", session.getId());
			} else if (action.equalsIgnoreCase("startTransaction")) {
				JdbcSession session = client.getJdbcSession(sessionId);
				session.startTransaction();
				logger.debug("starting transaction on session id {}", session.getId());
			} else if (action.equalsIgnoreCase("commitTransaction")) {
				JdbcSession session = client.getJdbcSession(sessionId);
				session.commitTransaction();
				logger.debug("committing transaction on session id {}", session.getId());
			} else if (action.equalsIgnoreCase("rollbackTransaction")) {
				JdbcSession session = client.getJdbcSession(sessionId);
				session.rollbackTransaction();
				logger.debug("rolling back transaction on session id {}", session.getId());
			} else {
				throw new RuntimeException(MessageFormat.format("Unsupported session action {0}", action));
			}
		} else if (resource.equalsIgnoreCase("statement")) {
			if (action.equalsIgnoreCase("create")) {
				/*-
				 * { "resource" : "statement" ,
				 *   "action" : "create" ,
				 *   "sessionId" : "mySession" , // optional
				 *   "statementId" : "abc" // optional
				 * } 
				 */
				JdbcSession jdbcSession = client.getJdbcSession(sessionId);
				jdbcSession.createStatement(statementId);
				logger.debug("created new statement id {}", jdbcSession.getLastStatementId());
			} else if (action.equalsIgnoreCase("execute")) {
				Statement stmt = null;
				try {
					JdbcSession jdbcSession = client.getJdbcSession(sessionId);
					stmt = jdbcSession.getStatement(statementId);
					if (stmt == null) {
						stmt = jdbcSession.createStatement(statementId);
					}
					logger.debug("executing statement (id: {} , sql: {})", jdbcSession.getLastStatementId(), sql);
					if (stmt.execute(sql)) {
						ResultSet rs = stmt.getResultSet();
						String actualResults = convertResultSetToJson(rs);
						rs.close();
						if (expectedResults != null) {
							compareResults(expectedResults, actualResults);
						}
					}
				} finally {
					//					if (stmt != null && statementId == null) {
					//						try {
					//							stmt.close();
					//						} catch (SQLException e) {
					//							// do nothing
					//						}
					//					}
				}
			} else if (action.equalsIgnoreCase("close")) {
				JdbcSession jdbcSession = client.getJdbcSession(sessionId);
				jdbcSession.removeStatement(statementId);
				logger.debug("closed statement id {}", jdbcSession.getLastStatementId());
			}
		} else if (resource.equalsIgnoreCase("preparedStatement")) {
			if (action.equalsIgnoreCase("create")) {
				/*-
				 * { "resource" : "preparedStatement" ,
				 *   "action" : "create" ,
				 *   "sql" : "select tabname from systables where tabid > 99" ,
				 *   "sessionId" : "mySession" , // optional
				 *   "statementId" : "abc" // optional
				 * } 
				 */
				JdbcSession jdbcSession = client.getJdbcSession(sessionId);
				Connection c = jdbcSession.getConnection();
				PreparedStatement pstmt = c.prepareStatement(sql);
				jdbcSession.putPreparedStatement(statementId, pstmt);
				logger.debug("created prepared statement id {}", jdbcSession.getLastPreparedStatementId());
			} else if (action.equalsIgnoreCase("execute")) {
				PreparedStatement pstmt = null;
				try {
					JdbcSession jdbcSession = client.getJdbcSession(sessionId);
					pstmt = jdbcSession.getPreparedStatement(statementId);
					if (pstmt == null) {
						Connection c = jdbcSession.getConnection();
						pstmt = c.prepareStatement(sql);
						jdbcSession.putPreparedStatement(statementId, pstmt);
					}
					logger.debug("executing prepared statement (id: {})", jdbcSession.getLastPreparedStatementId());
					if (this.bindings != null) {
						Binding.bindAll(this.bindings, pstmt);
					}
					if (pstmt.execute()) {
						ResultSet rs = pstmt.getResultSet();
						String actualResults = convertResultSetToJson(rs);
						rs.close();
						//System.out.println(actualResults);
						if (expectedResults != null) {
							compareResults(expectedResults, actualResults);
						}
					}
				} finally {
					//					if (pstmt != null && statementId == null) {
					//						try {
					//							pstmt.close();
					//						} catch (SQLException e) {
					//							// do nothing
					//						}
					//					}
				}				
			} else if (action.equalsIgnoreCase("close")) {
				JdbcSession jdbcSession = client.getJdbcSession(sessionId);
				jdbcSession.removeStatement(statementId);
				logger.debug("closed prepared statement id {}", jdbcSession.getLastPreparedStatementId());
			}
		} else {
			throw new RuntimeException(MessageFormat.format("Unsupported resource type {0}", resource));
		}
	}

	public Operation(String resource, String action, String sql) {
		this.resource = resource;
		this.action = action;
		this.sql = sql;
	}

	String convertResultSetToJson(ResultSet rs) throws IOException, SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		StringWriter sw = new StringWriter();
		JsonWriter jw = new JsonWriter(sw);
		jw.setHtmlSafe(false);
		jw.beginArray();
		while (rs.next()) {
			jw.beginObject();
			for (int i=1; i <= rsmd.getColumnCount(); ++i) {
				jw.name(rsmd.getColumnLabel(i));
				Object value = rs.getObject(i);
				GsonUtils.newGson().toJson(value, value.getClass(), jw);
			}
			jw.endObject();
		}
		jw.endArray();
		jw.flush();
		String json = sw.toString();
		return json;
	}

	private void compareResults(JsonArray expectedJson, String actualJson) throws IOException {
		JsonReader expected = null;
		JsonReader actual = null;
		try {
			expected = new JsonReader(new StringReader(expectedJson.toString()));
			actual = new JsonReader(new StringReader(actualJson));
			while (expected.hasNext()) {
				JsonToken next = expected.peek();
				switch (next) {
				case BEGIN_ARRAY:
					expected.beginArray();
					try {
						actual.beginArray();
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected begin array at at {0}", actual.getPath()));
					}
					break;
				case BEGIN_OBJECT:
					expected.beginObject();
					try {
						actual.beginObject();
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected begin object at at {0}", actual.getPath()));
					}
					break;
				case BOOLEAN:
					try {
						if (expected.nextBoolean() != actual.nextBoolean()) {
							logError(MessageFormat.format("boolean mismatch at {0}", expected.getPath()));
						}
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected boolean at at {0}", actual.getPath()));
					}
					break;
				case END_ARRAY:
					expected.endArray();
					try {
						actual.endArray();
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected end array at at {0}", actual.getPath()));
					}
					break;
				case END_DOCUMENT:
					try {
						actual.hasNext();
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected end document at at {0}", actual.getPath()));
					}
					break;
				case END_OBJECT:
					expected.endObject();
					try {
						actual.endObject();
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected end object at at {0}", actual.getPath()));
					}
					break;
				case NAME:
					try {
						String expectedName = expected.nextName();
						String actualName = actual.nextName();
						if (!expectedName.equals(actualName)) {
							logError(MessageFormat.format("name mismatch at {0} (expected: {1}, actual: {2})", expected.getPath(), expectedName, actualName));
						}
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected name at at {0}", actual.getPath()));
					}
					break;
				case NULL:
					expected.nextNull();
					try {
						actual.nextNull();
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected null at at {0}", actual.getPath()));
					}
					break;
				case NUMBER:
					try {
						double expectedDouble = expected.nextDouble();
						double actualDouble = actual.nextDouble();
						if (expectedDouble != actualDouble) {
							logError(MessageFormat.format("double mismatch at {0} (expected: {1}, actual: {2})", expected.getPath(), expectedDouble, actualDouble));
						}
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected double at at {0}", actual.getPath()));
					}
					break;
				case STRING:
					try {
						String expectedString = expected.nextString();
						String actualString = actual.nextString();
						if (!expectedString.equals(actualString)) {
							logError(MessageFormat.format("string mismatch at {0} (expected: {1}, actual: {2})", expected.getPath(), expectedString, actualString));
						}
					} catch (IllegalStateException e) {
						logError(MessageFormat.format("expected string at at {0}", actual.getPath()));
					}
					break;
				default:
					break;
				}
			}
			if (actual.hasNext()) {
				logError("compare results failed: actual has more results than expected");
			}
		} finally {
			try {
				expected.close();
			} catch (IOException e) {
				// do nothing
			}
			try {
				actual.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
	
	private void logError(final String message) {
		logger.error("Message: " + message);
		logger.error(toString());
		errorCount++;
	}
	
	public int getErrorCount() {
		return errorCount;
	}

	public static Operation fromJson(String line) {
		final Gson gson = GsonUtils.newGson();
		Operation operation = gson.fromJson(line, Operation.class);
		return operation;
	}

	@Override
	public String toString() {
		return GsonUtils.newGson().toJson(this);
	}

	public void setLine(int line) {
		this.line = line;
	}
	
	public int getLine() {
		return line;
	}
}
