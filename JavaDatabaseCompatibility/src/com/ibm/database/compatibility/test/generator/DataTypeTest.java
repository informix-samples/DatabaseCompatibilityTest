package com.ibm.database.compatibility.test.generator;

import java.io.IOException;
import java.util.ArrayList;
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
import com.ibm.database.compatibility.test.generator.datatypes.BooleanColumn;
import com.ibm.database.compatibility.test.generator.datatypes.CharColumn;
import com.ibm.database.compatibility.test.generator.datatypes.Column;
import com.ibm.database.compatibility.test.generator.datatypes.FloatColumn;
import com.ibm.database.compatibility.test.generator.datatypes.IntColumn;
import com.ibm.database.compatibility.test.generator.datatypes.LVarcharColumn;
import com.ibm.database.compatibility.test.generator.datatypes.NCharColumn;
import com.ibm.database.compatibility.test.generator.datatypes.VarcharColumn;

public class DataTypeTest {
	
	public static int N_INSERTS = 10;
	public static int N_QUERIES = 3;
	
	public static String host = "localhost";
	public static int port = 8088;
	public static String dbname = "test";
	public static String user = "informix";
	public static String password = "password";
	public static String additionalConnectionProperties = "CLIENT_LOCALE=en_us.utf8;DB_LOCALE=en_us.utf8";
	
	public static Operation getCreateCredentialsOperation() {
		return new Operation.Builder().resource("credentials").action("create").credentialId("test").host(host).port(port).db(dbname).user(user).password(password).additionalConnectionProperties(additionalConnectionProperties).build();
	}
	
	public static Operation getCreateSessionOperation(String sessionId) {
		return new Operation.Builder().resource("session").action("create").sessionId(sessionId).className("com.informix.jdbc.IfxDriver").build();
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
		return getExecutePstmtOperation(stmtId, bindings, null);
	} 
	
	public static Operation getExecutePstmtOperation(String stmtId, Binding[] bindings, JsonArray expectedResults) {
		Builder op = new Operation.Builder().resource("preparedStatement").action("execute").statementId(stmtId).bindings(bindings);
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
		jow.write(getCreateCredentialsOperation());
	}
	
	public static void writeEndTestInfo(JsonOperationWriter jow, String testName) throws IOException {
		jow.writeComment("end of test: " + testName);
	}
	
	public static void createDataTypeTest(JsonOperationWriter jow, String tabName, List<Column> tableColumns, int nInserts, int nQueries) throws IOException {
		jow.write(getCreateSessionOperation("test"));
		jow.writeComment("creating table");
		jow.write(getDropTableOperation("ddl", tabName));
		jow.write(getCreateTableOperation("ddl", tabName, tableColumns));
		jow.write(getCloseStatementOperation("ddl"));
		
		jow.writeComment("inserting data");
		jow.write(getInsertPstmtOperation("insert", tabName, tableColumns.size()));
		JsonArray tableData = new JsonArray();
		for (int i = 0; i < nInserts; i++) {
			JsonObject row = new JsonObject();
			BindingsBuilder bb = new Binding.BindingsBuilder();
			for (int j = 0; j < tableColumns.size(); j++) {
				Column c = tableColumns.get(j);
				Object v = c.getValue(i);
				if (v instanceof Number) {
					row.add(c.getColumnName(), new JsonPrimitive((Number) v));
				} else if (v instanceof String) {
					row.add(c.getColumnName(), new JsonPrimitive((String) v));
				} else if (v instanceof Boolean) {
					row.add(c.getColumnName(), new JsonPrimitive((Boolean) v));
				} else {
					throw new RuntimeException("Unhandled type for column value. Type: " + v.getClass().getCanonicalName());
				}
				bb.add(j + 1, v, c.getColumnTypeName());
			}
			tableData.add(row);
			jow.write(getExecutePstmtOperation("insert", bb.build()));
		}
		jow.write(getClosePresparedStatementOperation("insert"));
		
		jow.writeComment("running equality query and validating data");
		jow.write(getCreatePreparedStatementForQueryOperation("query", tabName, tableColumns.get(0)));
		for (int i = 0; i < nQueries; i++) {
			int index = (i * 3) % nInserts;
			BindingsBuilder bb = new Binding.BindingsBuilder().add(0, tableColumns.get(0).getValue(index), tableColumns.get(0).getColumnTypeName());
			jow.write(getExecutePstmtOperation("query", bb.build(), getMatchingRows(tableData, tableColumns.get(0), tableColumns.get(0).getValue(index))));
		}
		jow.write(getClosePresparedStatementOperation("query"));

		jow.writeComment("dropping table");
		jow.write(getDropTableOperation("ddl", tabName));
		jow.write(getCloseStatementOperation("ddl"));
		//jow.write(getCloseSessionOperation("test"));	// TODO why does it fail with non-exclusive access if we don't close the session?
	}
	
	public static JsonArray getMatchingRows(JsonArray tableData, Column column, Object queryValue) {
		JsonArray matches = new JsonArray();
		for (JsonElement e : tableData) {
			JsonObject row = (JsonObject) e;
			JsonElement rowElement = row.get(column.getColumnName());
			Object colValue = convertJsonElementToObject(rowElement, column);
			if (colValue.equals(queryValue)) {
				matches.add(row);
			}
		}
		return matches;
	}
	
	public static Object convertJsonElementToObject(JsonElement element, Column column) {
		if (element.isJsonNull()) {
			return null;
		} else if (element.isJsonPrimitive()) {
			try {
				if (column.getJavaClassName().equals(String.class)) {
					return element.getAsString();
				} else if (column.getJavaClassName().equals(Boolean.class)) {
					return element.getAsBoolean();
				} else {
					return element.getAsNumber();
				}
			} catch (ClassCastException e) {
				throw new RuntimeException("ClassCastException on primitive type in convertJsonElementToObject. json primitive: " + element.toString(), e);
			}
		} else {
			throw new RuntimeException("Unhandled type in convertJsonElementToObject. json element: " + element.toString());
		}
	}
	
	public static void generateIntTest(String filename) throws IOException {
		String testName = "int datatype test";
		String tabName = "int_test";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		writeStartTestInfo(jow, testName);
		List<Column> columns = new ArrayList<Column>();
		int[] nColumns = {1, 50, 100};
		for (int n : nColumns) {
			columns.clear();
			for (int j = 0; j < n; j++) {
				columns.add(new IntColumn("i" + j, j));
			}
			createDataTypeTest(jow,tabName, columns, N_INSERTS, N_QUERIES);
		}
		writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateFloatTest(String filename) throws IOException {
		// TODO: Test precision in float column?
		String testName = "float datatype test";
		String tabName = "float_test";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		writeStartTestInfo(jow, testName);
		List<Column> columns = new ArrayList<Column>();
		int[] nColumns = {1, 50, 100};
		for (int n : nColumns) {
			columns.clear();
			for (int j = 0; j < n; j++) {
				columns.add(new FloatColumn("i" + j, j));
			}
			createDataTypeTest(jow,tabName, columns, N_INSERTS, N_QUERIES);
		}
		writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateCharTest(String filename) throws IOException {
		String testName = "char datatype test";
		String tabName = "char_test";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		writeStartTestInfo(jow, testName);
		List<Column> columns = new ArrayList<Column>();
		int[] charLegths = {2, 100, 255};
		int[] nColumns = {1, 50};
		for (int charLength : charLegths) {
			for (int n : nColumns) {
				columns.clear();
				for (int j = 0; j < n; j++) {
					columns.add(new CharColumn("i" + j, charLength, j, (j % 2) == 0));
				}
				createDataTypeTest(jow,tabName, columns, N_INSERTS, N_QUERIES);
			}
		}
		// Also test a single char column of max bytes
		columns.clear();
		columns.add(new CharColumn("i0", 32767, 32767, true));
		createDataTypeTest(jow,tabName, columns, N_INSERTS, N_QUERIES);

		writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateVarcharTest(String filename) throws IOException {
		String testName = "varchar datatype test";
		String tabName = "varchar_test";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		writeStartTestInfo(jow, testName);
		List<Column> columns = new ArrayList<Column>();
		int[] charLegths = {2, 100, 255};
		int[] nColumns = {1, 50, 100};
		for (int charLength : charLegths) {
			for (int n : nColumns) {
				columns.clear();
				for (int j = 0; j < n; j++) {
					columns.add(new VarcharColumn("i" + j, charLength, j, (j % 2) == 0));
				}
				createDataTypeTest(jow,tabName, columns, N_INSERTS, N_QUERIES);
			}
		}
		writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}

	public static void generateLVarcharTest(String filename) throws IOException {
		String testName = "lvarchar datatype test";
		String tabName = "lvarchar_test";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		writeStartTestInfo(jow, testName);
		List<Column> columns = new ArrayList<Column>();
		int[] charLegths = {500, 1024, 2048, 5000};
		int[] nColumns = {1, 5};
		for (int charLength : charLegths) {
			for (int n : nColumns) {
				columns.clear();
				for (int j = 0; j < n; j++) {
					columns.add(new LVarcharColumn("i" + j, charLength, j));
				}
				createDataTypeTest(jow,tabName, columns, 3, 1);
			}
		}
		writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateNCharTest(String filename) throws IOException {
		String testName = "nchar datatype test";
		String tabName = "nchar_test";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		writeStartTestInfo(jow, testName);
		List<Column> columns = new ArrayList<Column>();
		int[] charLegths = {2, 50, 100};
		int[] nColumns = {1, 50, 100};
		for (int charLength : charLegths) {
			for (int n : nColumns) {
				columns.clear();
				for (int j = 0; j < n; j++) {
					columns.add(new NCharColumn("i" + j, charLength, j));
				}
				createDataTypeTest(jow,tabName, columns, N_INSERTS, N_QUERIES);
			}
		}
		writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}

	public static void generateBooleanTest(String filename) throws IOException {
		String testName = "boolean datatype test";
		String tabName = "boolean_test";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		writeStartTestInfo(jow, testName);
		List<Column> columns = new ArrayList<Column>();
		int[] nColumns = {1, 50, 100};
		for (int n : nColumns) {
			columns.clear();
			for (int j = 0; j < n; j++) {
				columns.add(new BooleanColumn("i" + j, j));
			}
			createDataTypeTest(jow,tabName, columns, N_INSERTS, 2);
		}
		writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
//	public static void addIntegerTest(JsonOperationWriter jow) throws IOException {
//		jow.writeComment("start of integer test");
//		Operation createCredentials = new Operation.Builder().resource("credentials").action("create").credentialId("mydb").host("10.168.8.130").port(40000).db("textdb").user("informix").password("informix").additionalConnectionProperties("CLIENT_LOCALE=en_us.utf8;DB_LOCALE=en_us.utf8").build();
//		jow.write(createCredentials);
//		Operation createSession = new Operation.Builder().resource("session").action("create").credentialId("mydb").className("com.informix.jdbc.IfxDriver").build();
//		jow.write(createSession);
//		Builder ddlOpBuilder = new Operation.Builder().resource("statement").action("execute").statementId("ddl");
//		jow.write(ddlOpBuilder.action("execute").sql("DROP TABLE IF EXISTS INT_TEST").build());
//		for (int i=1; i <= 100; ++i) {
//			jow.writeComment(MessageFormat.format("create table with {0} integer columns", i));
//			StringBuilder sqlCreateTable = new StringBuilder("CREATE TABLE INT_TEST (");
//			StringBuilder sqlInsert = new StringBuilder("INSERT INTO INT_TEST VALUES(");
//			for (int j=0; j < i; ++j) {
//				if (j > 0) {
//					sqlCreateTable.append(", ");
//					sqlInsert.append(",");
//				}
//				sqlCreateTable.append("i");
//				sqlCreateTable.append(j);
//				sqlCreateTable.append(" integer");
//				sqlInsert.append("?");
//			}
//			sqlCreateTable.append(")");
//			sqlInsert.append(")");
//			jow.write(ddlOpBuilder.sql(sqlCreateTable.toString()).build());
//			jow.writeComment("creating prepared statement for insert");
//			Builder dmlOpBuilder = new Operation.Builder().resource("preparedStatement").action("create").statementId("insert").sql(sqlInsert.toString());
//			jow.write(dmlOpBuilder.build());
//			dmlOpBuilder.action("execute").sql(null);
//			BindingsBuilder bb = new Binding.BindingsBuilder();
//			for (int j=1; j <= i; ++j) {
//				bb.add(j, j, SqlDataType.INT.toString());
//			}
//			dmlOpBuilder.bindings(bb.build());
//			jow.write(dmlOpBuilder.build());
//			jow.write(ddlOpBuilder.action("execute").sql("DROP TABLE IF EXISTS INT_TEST").build());
//		}
//		jow.writeComment("closing ddl statement");
//		jow.write(ddlOpBuilder.action("close").build());
//		jow.writeComment("end of integer test");
//	}
	
	public static void getSystemEnv() {
		if (System.getenv().containsKey("HOST")) {
			host = System.getenv("HOST");
		}
		if (System.getenv().containsKey("PORT")) {
			port = Integer.parseInt(System.getenv("PORT"));
		}
		if (System.getenv().containsKey("DATABASE")) {
			dbname = System.getenv("DATABASE");
		}
		if (System.getenv().containsKey("USER")) {
			user = System.getenv("USER");
		}
		if (System.getenv().containsKey("PASSWORD")) {
			password = System.getenv("PASSWORD");
		}
		if (System.getenv().containsKey("CONN_PROPERTIES")) {
			additionalConnectionProperties = System.getenv().get("CONN_PROPERTIES");
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		getSystemEnv();
		generateIntTest("dataTypeTest_INT.json");
		generateFloatTest("dataTypeTest_FLOAT.json");
		generateCharTest("dataTypeTest_CHAR.json");
		generateVarcharTest("dataTypeTest_VARCHAR.json");
		generateLVarcharTest("dataTypeTest_LVARCHAR.json");
		generateNCharTest("dataTypeTest_NCHAR.json");
		generateBooleanTest("dataTypeTest_BOOLEAN.json");
	}

}
