package com.ibm.database.compatibility.test.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.database.compatibility.Binding;
import com.ibm.database.compatibility.Binding.BindingsBuilder;
import com.ibm.database.compatibility.JsonOperationWriter;
import com.ibm.database.compatibility.SqlDataType;
import com.ibm.database.compatibility.test.generator.datatypes.BigIntColumn;
import com.ibm.database.compatibility.test.generator.datatypes.BigSerialColumn;
import com.ibm.database.compatibility.test.generator.datatypes.BooleanColumn;
import com.ibm.database.compatibility.test.generator.datatypes.CharColumn;
import com.ibm.database.compatibility.test.generator.datatypes.Column;
import com.ibm.database.compatibility.test.generator.datatypes.DateColumn;
import com.ibm.database.compatibility.test.generator.datatypes.FloatColumn;
import com.ibm.database.compatibility.test.generator.datatypes.Int8Column;
import com.ibm.database.compatibility.test.generator.datatypes.IntColumn;
import com.ibm.database.compatibility.test.generator.datatypes.LVarcharColumn;
import com.ibm.database.compatibility.test.generator.datatypes.NCharColumn;
import com.ibm.database.compatibility.test.generator.datatypes.Serial8Column;
import com.ibm.database.compatibility.test.generator.datatypes.SerialColumn;
import com.ibm.database.compatibility.test.generator.datatypes.SmallIntColumn;
import com.ibm.database.compatibility.test.generator.datatypes.VarcharColumn;

public class DataTypeTest {
	
	public static String FILEPATH = "resources/";
	public static String FILEPREFIX = "dataTypeTest_";
	public static String FILESUFFIX = ".json";
	
	public static int N_INSERTS = 10;
	public static int N_QUERIES = 3;
	
	public static String getTestOutputFileName(String datatype) {
		return FILEPATH + FILEPREFIX + datatype + FILESUFFIX;
	}
	
	/**
	 * Create a data type test for the specified column types 
	 * that tests all of the CRUD operations on that type.
	 * 
	 * @param jow
	 * @param tabName
	 * @param tableColumns
	 * @throws IOException
	 */
	public static void createDataTypeTest_CRUD(JsonOperationWriter jow, String tabName, Column column) throws IOException {
		createDataTypeTest_CRUD(jow, tabName, column, true);
	}
	
	/**
	 * Create a data type test for the specified column types 
	 * that tests all of the CRUD operations on that type.
	 * 
	 * @param jow
	 * @param tabName
	 * @param tableColumns
	 * @param supportsUpdates
	 * @throws IOException
	 */
	public static void createDataTypeTest_CRUD(JsonOperationWriter jow, String tabName, Column column, boolean supportsUpdates) throws IOException {
		List<Column> tableColumns = Arrays.asList(column);
		int insertSeed = 27;
		int updateSeed = 88;
		
		jow.writeComment("---- Testing CRUD operations ----");
		jow.write(TestGeneratorUtils.getCreateSessionOperation("testCRUD"));
		jow.writeComment("creating table");
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName));
		jow.write(TestGeneratorUtils.getCreateTableOperation("ddl", tabName, tableColumns));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("ddl"));
		
		jow.writeComment("insert row");
		jow.write(TestGeneratorUtils.getInsertPstmtOperation("insert", tabName, 1));
		JsonObject row = TestGeneratorUtils.writeInsertExecuteStatement(jow, tableColumns, insertSeed);
		
		jow.writeComment("query for row");
		jow.write(TestGeneratorUtils.getCreatePreparedStatementForQueryOperation("query", tabName, column));
		JsonArray expectedResult = new JsonArray();
		expectedResult.add(row);
		BindingsBuilder bb = new Binding.BindingsBuilder().add(1, column.getValue(insertSeed), column.getColumnTypeName());
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", bb.build(), expectedResult));
		
		if (supportsUpdates) {
			jow.writeComment("update row");
			jow.write(TestGeneratorUtils.getUpdatePstmtOperation("update", tabName, column, column));
			bb = new Binding.BindingsBuilder().add(1, column.getValue(updateSeed), column.getColumnTypeName())
					.add(2, column.getValue(insertSeed), column.getColumnTypeName());
			jow.write(TestGeneratorUtils.getExecutePstmtOperation("update", bb.build(), expectedResult));
			row = new JsonObject();
			row.add(column.getColumnName(), TestGeneratorUtils.createJsonElement(column.getValue(updateSeed)));
	
			jow.writeComment("query for original row");
			expectedResult = new JsonArray();
			bb = new Binding.BindingsBuilder().add(1, column.getValue(insertSeed), column.getColumnTypeName());
			jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", bb.build(), expectedResult));
			
			jow.writeComment("query for updated row");
			expectedResult.add(row);
			bb = new Binding.BindingsBuilder().add(1, column.getValue(updateSeed), column.getColumnTypeName());
			jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", bb.build(), expectedResult));
		} else {
			jow.writeComment("updates not supported on this datatype... skipping update");
		}
		
		jow.writeComment("delete row");
		jow.write(TestGeneratorUtils.getDeletePstmtOperation("delete", tabName, column));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("delete", bb.build(), expectedResult));
		
		jow.writeComment("query for deleted row");
		expectedResult = new JsonArray();
		bb = new Binding.BindingsBuilder().add(1, column.getValue(updateSeed), column.getColumnTypeName());
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", bb.build(), expectedResult));
		
		jow.writeComment("dropping table");
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("ddl"));
	}
	
	
	/**
	 * Create a data type test for the specified columns types
	 * that tests query and insert prepared statement reuse.
	 * @param jow
	 * @param tabName
	 * @param tableColumns
	 * @param nInserts
	 * @param nQueries
	 * @throws IOException
	 */
	public static void createDataTypeTest_QueryInsertPstmt(JsonOperationWriter jow, String tabName, List<Column> tableColumns, int nInserts, int nQueries) throws IOException {
		jow.writeComment("---- Testing query and insert prepared statement reuse ----");
		jow.write(TestGeneratorUtils.getCreateSessionOperation("testPstmt"));
		jow.writeComment("creating table");
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName));
		jow.write(TestGeneratorUtils.getCreateTableOperation("ddl", tabName, tableColumns));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("ddl"));
		
		jow.writeComment("inserting data");
		jow.write(TestGeneratorUtils.getInsertPstmtOperation("insert", tabName, tableColumns.size()));
		JsonArray tableData = new JsonArray();
		for (int i = 0; i < nInserts; i++) {
			JsonObject row = TestGeneratorUtils.writeInsertExecuteStatement(jow, tableColumns, i);
			tableData.add(row);
		}
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("insert"));
		
		jow.writeComment("running equality query and validating data");
		jow.write(TestGeneratorUtils.getCreatePreparedStatementForQueryOperation("query", tabName, tableColumns.get(0)));
		for (int i = 0; i < nQueries; i++) {
			int index = (i * 3) % nInserts;
			BindingsBuilder bb = new Binding.BindingsBuilder().add(0, tableColumns.get(0).getValue(index), tableColumns.get(0).getColumnTypeName());
			jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", bb.build(), getMatchingRows(tableData, tableColumns.get(0), tableColumns.get(0).getValue(index))));
		}
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("query"));

		jow.writeComment("dropping table");
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("ddl"));
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
	
	public static void generateIntTest() throws IOException {
		String datatype = SqlDataType.INT.toString();
		String testName = "int datatype test";
		String tabName = "int_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new IntColumn("i0", 0));

		List<Column> columns = new ArrayList<Column>();
		int[] nColumns = {3, 50, 100};
		for (int n : nColumns) {
			columns.clear();
			for (int j = 0; j < n; j++) {
				columns.add(new IntColumn("i" + j, j));
			}
			createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
		}
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}

	public static void generateBigIntTest() throws IOException {
		String datatype = SqlDataType.BIGINT.toString();
		String testName = "bigint datatype test";
		String tabName = "bigint_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new BigIntColumn("i0", 10));

		List<Column> columns = new ArrayList<Column>();
		int nColumns = 3;
		for (int j = 0; j < nColumns; j++) {
			columns.add(new BigIntColumn("i" + j, 60 - j));
		}
		createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}

	public static void generateInt8Test() throws IOException {
		String datatype = SqlDataType.INT8.toString();
		String testName = "int8 datatype test";
		String tabName = "int8_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new Int8Column("i0", 12));

		List<Column> columns = new ArrayList<Column>();
		int nColumns = 3;
		for (int j = 0; j < nColumns; j++) {
			columns.add(new Int8Column("i" + j, 59 - j));
		}
		createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}

	public static void generateSmallIntTest() throws IOException {
		String datatype = SqlDataType.SMALLINT.toString();
		String testName = "smallint datatype test";
		String tabName = "smallint_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new SmallIntColumn("i0", 11));

		List<Column> columns = new ArrayList<Column>();
		int nColumns = 3;
		for (int j = 0; j < nColumns; j++) {
			columns.add(new SmallIntColumn("i" + j, 59 - j));
		}
		createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateSerialTest() throws IOException {
		String datatype = SqlDataType.SERIAL.toString();
		String testName = "serial datatype test";
		String tabName = "serial_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new SerialColumn("i0", 55, false), false);

		List<Column> columns = new ArrayList<Column>();
		columns.add(new SerialColumn("id", 0, true));
		columns.add(new VarcharColumn("value", 20, 0, false));
		createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateBigSerialTest() throws IOException {
		String datatype = SqlDataType.BIGSERIAL.toString();
		String testName = "bigserial datatype test";
		String tabName = "bigserial_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new BigSerialColumn("i0", 55, false), false);

		List<Column> columns = new ArrayList<Column>();
		columns.add(new BigSerialColumn("id", 0, true));
		columns.add(new VarcharColumn("value", 20, 0, false));
		createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateSerial8Test() throws IOException {
		String datatype = SqlDataType.SERIAL8.toString();
		String testName = "serial8 datatype test";
		String tabName = "serial8_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new Serial8Column("i0", 55, false), false);

		List<Column> columns = new ArrayList<Column>();
		columns.add(new Serial8Column("id", 0, true));
		columns.add(new VarcharColumn("value", 20, 0, false));
		createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateFloatTest() throws IOException {
		// TODO: Test precision in float column?
		String datatype = SqlDataType.FLOAT.toString();
		String testName = "float datatype test";
		String tabName = "float_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new FloatColumn("i0", 0));

		List<Column> columns = new ArrayList<Column>();
		int[] nColumns = {1, 50, 100};
		for (int n : nColumns) {
			columns.clear();
			for (int j = 0; j < n; j++) {
				columns.add(new FloatColumn("i" + j, j));
			}
			createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
		}
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateCharTest() throws IOException {
		String datatype = SqlDataType.CHAR.toString();
		String testName = "char datatype test";
		String tabName = "char_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new CharColumn("i0", 1, 0, true));
		
		List<Column> columns = new ArrayList<Column>();
		int[] charLegths = {2, 100, 255};
		int[] nColumns = {1, 50};
		for (int charLength : charLegths) {
			for (int n : nColumns) {
				columns.clear();
				for (int j = 0; j < n; j++) {
					columns.add(new CharColumn("i" + j, charLength, j, (j % 2) == 0));
				}
				createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
			}
		}
		
		// Also test a single char column of max bytes
		columns.clear();
		columns.add(new CharColumn("i0", 32767, 32767, true));
		createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);

		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateVarcharTest() throws IOException {
		String datatype = SqlDataType.VARCHAR.toString();
		String testName = "varchar datatype test";
		String tabName = "varchar_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new VarcharColumn("i0", 5, 0, true));
		
		List<Column> columns = new ArrayList<Column>();
		int[] charLegths = {2, 100, 255};
		int[] nColumns = {1, 50, 100};
		for (int charLength : charLegths) {
			for (int n : nColumns) {
				columns.clear();
				for (int j = 0; j < n; j++) {
					columns.add(new VarcharColumn("i" + j, charLength, j, (j % 2) == 0));
				}
				createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
			}
		}
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}

	public static void generateLVarcharTest() throws IOException {
		String datatype = SqlDataType.LVARCHAR.toString();
		String testName = "lvarchar datatype test";
		String tabName = "lvarchar_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new LVarcharColumn("i0", 500, 0));
		
		List<Column> columns = new ArrayList<Column>();
		int[] charLegths = {1024, 2048, 5000};
		int[] nColumns = {1, 5};
		for (int charLength : charLegths) {
			for (int n : nColumns) {
				columns.clear();
				for (int j = 0; j < n; j++) {
					columns.add(new LVarcharColumn("i" + j, charLength, j));
				}
				createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, 3, 1);
			}
		}
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateNCharTest() throws IOException {
		String datatype = SqlDataType.NCHAR.toString();
		String testName = "nchar datatype test";
		String tabName = "nchar_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new NCharColumn("i0", 2, 0));
		
		List<Column> columns = new ArrayList<Column>();
		int[] charLegths = {50, 100};
		int[] nColumns = {1, 50, 100};
		for (int charLength : charLegths) {
			for (int n : nColumns) {
				columns.clear();
				for (int j = 0; j < n; j++) {
					columns.add(new NCharColumn("i" + j, charLength, j));
				}
				createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, N_QUERIES);
			}
		}
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}

	public static void generateBooleanTest() throws IOException {
		String datatype = SqlDataType.BOOLEAN.toString();
		String testName = "boolean datatype test";
		String tabName = "boolean_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new BooleanColumn("i0", 0));
		
		List<Column> columns = new ArrayList<Column>();
		int[] nColumns = {1, 50, 100};
		for (int n : nColumns) {
			columns.clear();
			for (int j = 0; j < n; j++) {
				columns.add(new BooleanColumn("i" + j, j));
			}
			createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, 2);
		}
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generateDateTest() throws IOException {
		String datatype = SqlDataType.DATE.toString();
		String testName = "date datatype test";
		String tabName = "date_test";
		JsonOperationWriter jow = new JsonOperationWriter(getTestOutputFileName(datatype));
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		createDataTypeTest_CRUD(jow, tabName, new DateColumn("i0", 0));
		
		List<Column> columns = new ArrayList<Column>();
		int[] nColumns = {1, 50, 100};
		for (int n : nColumns) {
			columns.clear();
			for (int j = 0; j < n; j++) {
				columns.add(new DateColumn("i" + j, j));
			}
			createDataTypeTest_QueryInsertPstmt(jow,tabName, columns, N_INSERTS, 2);
		}
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		generateIntTest();
		generateBigIntTest();
		generateInt8Test();
		generateSmallIntTest();
		generateSerialTest();
		generateBigSerialTest();
		generateSerial8Test();
		generateFloatTest();
		generateCharTest();
		generateVarcharTest();
		generateLVarcharTest();
		generateNCharTest();
		generateBooleanTest();
		generateDateTest();
	}

}
