package com.ibm.database.compatibility.test.generator;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.database.compatibility.Binding;
import com.ibm.database.compatibility.Binding.BindingsBuilder;
import com.ibm.database.compatibility.JsonOperationWriter;
import com.ibm.database.compatibility.Operation;
import com.ibm.database.compatibility.Operation.Builder;
import com.ibm.database.compatibility.test.generator.datatypes.Column;

public class TestGeneratorUtils {
	
	public static Operation getCreateCredentialsOperation(String credentialId) {
		return new Operation.Builder().resource("credentials").action("create").credentialId(credentialId).build();
	}
	
	public static Operation getCreateSessionOperation(String sessionId) {
		return new Operation.Builder().resource("session").action("create").sessionId(sessionId).build();
	}

	public static Operation getStartTxnSessionOperation(String sessionId) {
		return new Operation.Builder().resource("session").action("startTransaction").sessionId(sessionId).build();
	}

	public static Operation getCommitTxnSessionOperation(String sessionId) {
		return new Operation.Builder().resource("session").action("commitTransaction").sessionId(sessionId).build();
	}

	public static Operation getRollbackTxnSessionOperation(String sessionId) {
		return new Operation.Builder().resource("session").action("rollbackTransaction").sessionId(sessionId).build();
	}
	
	public static Operation getCloseSessionOperation(String sessionId) {
		return new Operation.Builder().resource("session").action("close").sessionId(sessionId).build();
	}
	
	public static Operation getCreateStatementOperation(String stmtId) {
		return new Operation.Builder().resource("statement").action("create").statementId(stmtId).build();
	}

	public static Operation getExecuteStatementOperation(String stmtId, String sql) {
		return new Operation.Builder().resource("statement").action("execute").statementId(stmtId).sql(sql).build();
	}
	
	public static Operation getCloseStatementOperation(String stmtId) {
		return new Operation.Builder().resource("statement").action("close").statementId(stmtId).build();
	}
	
	public static Operation getCreatePreparedStatementOperation(String stmtId, String sql) {
		return new Operation.Builder().resource("preparedStatement").action("create").statementId(stmtId).sql(sql).build();
	}

	public static Operation getExecutePstmtOperation(String stmtId, Binding[] bindings) {
		return getExecutePstmtOperation(stmtId, bindings, null, null);
	} 
	
	public static Operation getExecutePstmtOperation(String stmtId, JsonArray expectedResults) {
		return getExecutePstmtOperation(stmtId, null, null, expectedResults);
	}

	public static Operation getExecutePstmtOperation(String stmtId, int nfetch, JsonArray expectedResults) {
		return getExecutePstmtOperation(stmtId, null, nfetch, expectedResults);
	}

	public static Operation getExecutePstmtOperation(String stmtId, Binding[] bindings, JsonArray expectedResults) {
		return getExecutePstmtOperation(stmtId, bindings, null, expectedResults);
	}
		
	public static Operation getExecutePstmtOperation(String stmtId, Binding[] bindings, Integer nfetch, JsonArray expectedResults) {
		Builder op = new Operation.Builder().resource("preparedStatement").action("execute").statementId(stmtId);
		if (bindings != null) {
			op.bindings(bindings);
		}
		if (nfetch != null) {
			op.nfetch(nfetch);
		}
		if (expectedResults != null) {
			op.expectedResults(expectedResults);
		}
		return op.build();
	}
	
	public static Operation getFetchPstmtOperation(String stmtId, Integer nfetch, JsonArray expectedResults) {
		Builder op = new Operation.Builder().resource("preparedStatement").action("fetch").statementId(stmtId);
		if (nfetch != null) {
			op.nfetch(nfetch);
		}
		if (expectedResults != null) {
			op.expectedResults(expectedResults);
		}
		return op.build();
	}
	
	public static Operation getClosePresparedStatementOperation(String stmtId) {
		return new Operation.Builder().resource("preparedStatement").action("close").statementId(stmtId).build();
	}
		
	public static Operation getDropTableOperation(String stmtId, String tabName) {
		return getExecuteStatementOperation(stmtId, "DROP TABLE IF EXISTS " + tabName);
	}
	
	public static Operation getCreateTableOperation(String stmtId, String tabName, List<Column> columns) {
		StringBuilder sql = new StringBuilder("CREATE TABLE ");
		sql.append(tabName);
		sql.append(" (");
		int i = 0;
		for (Column c : columns) {
			if (i > 0) {
				sql.append(",");
			}
			sql.append(c.getColumnName() + " " + c.getColumnTypeAsSQLString());
			i++;
		}
		sql.append(")");
		return getExecuteStatementOperation(stmtId, sql.toString());
	}
	
	public static Operation getInsertPstmtOperation(String stmtId, String tabName, int colCount) {
		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(tabName);
		sql.append(" VALUES (");
		for (int i = 0; i < colCount; i++) {
			if (i > 0) {
				sql.append(",");
			}
			sql.append("?");
		}
		sql.append(")");
		return getCreatePreparedStatementOperation(stmtId, sql.toString());
	}
	
	public static Operation getUpdatePstmtOperation(String stmtId, String tabName, Column updateColumn, Column queryColumn) {
		StringBuilder sql = new StringBuilder("UPDATE ");
		sql.append(tabName);
		sql.append(" SET ");
		sql.append(updateColumn.getColumnName());
		sql.append(" = ? WHERE ");
		sql.append(queryColumn.getColumnName());
		sql.append(" = ?");
		return getCreatePreparedStatementOperation(stmtId, sql.toString());
	}

	public static Operation getDeletePstmtOperation(String stmtId, String tabName,Column queryColumn) {
		StringBuilder sql = new StringBuilder("DELETE FROM ");
		sql.append(tabName);
		sql.append(" WHERE ");
		sql.append(queryColumn.getColumnName());
		sql.append(" = ?");
		return getCreatePreparedStatementOperation(stmtId, sql.toString());
	}
	
	public static JsonObject writeInsertExecuteStatement(JsonOperationWriter jow, String stmtId, List<Column> tableColumns, int i) throws IOException {
		JsonObject row = new JsonObject();
		BindingsBuilder bb = new Binding.BindingsBuilder();
		for (int j = 0; j < tableColumns.size(); j++) {
			Column c = tableColumns.get(j);
			Object v = c.getValue(i);
			row.add(c.getColumnName(), createJsonElement(v));
			bb.add(j + 1, v, c.getColumnTypeName());
		}
		jow.write(getExecutePstmtOperation(stmtId, bb.build()));
		return row;
	}
	
	public static JsonElement createJsonElement(Object v) {
		if (v instanceof Number) {
			return new JsonPrimitive((Number) v);
		} else if (v instanceof String) {
			return new JsonPrimitive((String) v);
		} else if (v instanceof Boolean) {
			return new JsonPrimitive((Boolean) v);
		} else {
			throw new RuntimeException("Unhandled type for column value. Type: " + v.getClass().getCanonicalName());
		}
	}
	
	public static Operation getCreatePreparedStatementForQueryOperation(String stmtId, String query) {
		return getCreatePreparedStatementOperation(stmtId, query);
	}
	
	public static Operation getCreatePreparedStatementForQueryOperation(String stmtId, String tabName, Column columnToQuery) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ");
		sql.append(tabName);
		sql.append(" WHERE ");
		sql.append(columnToQuery.getColumnName());
		sql.append(" = ? ");
		return getCreatePreparedStatementOperation(stmtId, sql.toString());
	}
	
	public static void writeStartTestInfo(JsonOperationWriter jow, String testName) throws IOException {
		jow.writeComment("start of test: " + testName);
		jow.write(getCreateCredentialsOperation("test"));
	}
	
	public static void writeEndTestInfo(JsonOperationWriter jow, String testName) throws IOException {
		jow.writeComment("end of test: " + testName);
	}
	
}
