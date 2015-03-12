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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class Operation {
	
	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
//	{ "stmt_type" : "stmt" | "pstmt" , // should we use a statement or a prepared statement
//		  "sql" : "...." , // the sql to be run/prepared
//		  "binds" : [ "Lance" , 34 , "3412 W ..." ] // for prepared statements, the binds can be ordered or by column name
//		          : { "name" : "Lance" , "age" : 34 , "address" : "3412 W..." }
//		  "result" : [ { } , { } ]  // An array of rows
//		}
	
	private String resource = null; // session | statement | preparedStatement
	private String action = null; // create | execute | close
	private String sessionId = null;
	private String statementId = null;
	private String url = null;
	private String className = null;
	private String sql = null;
	private Object[] binds = null;
	private JsonArray expectedResults = null;
	
	private Operation() {
		
	}
	
	public void invoke(JdbcClient client) throws IOException, SQLException {
		if (resource.equalsIgnoreCase("session")) {
			if (action.equalsIgnoreCase("create")) {
				// { resource: "createSession" , url: "jdbc:informix-sqli://..." , className = "com.informix.jdbc.IfxDriver" }
				if (className != null) {
					try {
						Class.forName(className);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(MessageFormat.format("Unable to load class {0}", className), e);
					}
				}
				client.newSession(url, sessionId);
			} else if (action.equalsIgnoreCase("close")) {
				client.removeJdbcSession(sessionId);
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
			} else if (action.equalsIgnoreCase("execute")) {
				Statement stmt = null;
				try {
					JdbcSession jdbcSession = client.getJdbcSession(sessionId);
					stmt = jdbcSession.getStatement(statementId);
					if (stmt == null) {
						stmt = jdbcSession.createStatement(statementId);
					}
					if (stmt.execute(sql)) {
						ResultSet rs = stmt.getResultSet();
						ResultSetMetaData rsmd = rs.getMetaData();
						while (rs.next()) {
							StringBuilder sb = new StringBuilder();
							for (int i=1; i <= rsmd.getColumnCount(); ++i) {
								if (i > 1) {
									sb.append(" , ");
								}
								sb.append(rs.getObject(i));
							}
							System.out.println(sb.toString());
						}
					}
				} finally {
					if (stmt != null && statementId == null) {
						try {
							stmt.close();
						} catch (SQLException e) {
							// do nothing
						}
					}
				}				
			} else if (action.equalsIgnoreCase("close")) {
				JdbcSession jdbcSession = client.getJdbcSession(sessionId);
				jdbcSession.removeStatement(statementId);
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
					if (binds != null && binds.length > 0) {
						for (int i=0; i < binds.length; ++i) {
							pstmt.setObject(i+1, binds[i]);
						}						
					}
					if (pstmt.execute()) {
						ResultSet rs = pstmt.getResultSet();
						String actualResults = convertResultSetToJson(rs);
						System.out.println(actualResults);
						if (expectedResults != null) {
							compareResults(expectedResults, actualResults);
						}
					}
				} finally {
					if (pstmt != null && statementId == null) {
						try {
							pstmt.close();
						} catch (SQLException e) {
							// do nothing
						}
					}
				}				
			} else if (action.equalsIgnoreCase("close")) {
				JdbcSession jdbcSession = client.getJdbcSession(sessionId);
				jdbcSession.removeStatement(statementId);
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

	static String convertResultSetToJson(ResultSet rs) throws IOException, SQLException {
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
				gson.toJson(value, value.getClass(), jw);
			}
			jw.endObject();
		}
		jw.endArray();
		jw.flush();
		String json = sw.toString();
		return json;
	}
	
	static void compareResults(JsonArray expectedJson, String actualJson) throws IOException {
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
						System.out.println(MessageFormat.format("expected begin array at at {0}", actual.getPath()));
					}
					break;
				case BEGIN_OBJECT:
					expected.beginObject();
					try {
						actual.beginObject();
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected begin object at at {0}", actual.getPath()));
					}
					break;
				case BOOLEAN:
					try {
						if (expected.nextBoolean() != actual.nextBoolean()) {
							System.out.println(MessageFormat.format("boolean mismatch at {0}", expected.getPath()));
						}
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected boolean at at {0}", actual.getPath()));
					}
				case END_ARRAY:
					expected.endArray();
					try {
						actual.endArray();
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected end array at at {0}", actual.getPath()));
					}
					break;
				case END_DOCUMENT:
					try {
						actual.hasNext();
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected end document at at {0}", actual.getPath()));
					}
					break;
				case END_OBJECT:
					expected.endObject();
					try {
						actual.endObject();
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected end object at at {0}", actual.getPath()));
					}
					break;
				case NAME:
					try {
						String expectedName = expected.nextName();
						String actualName = actual.nextName();
						if (!expectedName.equals(actualName)) {
							System.out.println(MessageFormat.format("name mismatch at {0} (expected: {1}, actual: {2})", expected.getPath(), expectedName, actualName));
						}
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected name at at {0}", actual.getPath()));
					}
					break;
				case NULL:
					expected.nextNull();
					try {
						actual.nextNull();
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected null at at {0}", actual.getPath()));
					}
					break;
				case NUMBER:
					try {
						if (expected.nextDouble() != actual.nextDouble()) {
							System.out.println(MessageFormat.format("double mismatch at {0}", expected.getPath()));
						}
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected double at at {0}", actual.getPath()));
					}
					break;
				case STRING:
					try {
						String expectedString = expected.nextString();
						String actualString = actual.nextString();
						if (!expectedString.equals(actualString)) {
							System.out.println(MessageFormat.format("string mismatch at {0} (expected: {1}, actual: {2})", expected.getPath(), expectedString, actualString));
						}
					} catch (IllegalStateException e) {
						System.out.println(MessageFormat.format("expected string at at {0}", actual.getPath()));
					}
					break;
				default:
					break;
				}
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
		
	public static Operation fromJson(String line) {
		Operation operation = gson.fromJson(line, Operation.class);
		return operation;
	}
	
	public static void main(String[] args) {
		Operation o = new Operation("statement", "execute", "select tabname from systables where tabid > 99");
		String s = gson.toJson(o);
		System.out.println(s);
		Operation o2 = gson.fromJson(s, Operation.class);
		System.out.println(o2);
	}

}
