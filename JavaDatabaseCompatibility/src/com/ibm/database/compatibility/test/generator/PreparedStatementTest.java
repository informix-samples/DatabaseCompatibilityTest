package com.ibm.database.compatibility.test.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.database.compatibility.Binding;
import com.ibm.database.compatibility.Binding.BindingsBuilder;
import com.ibm.database.compatibility.JsonOperationWriter;
import com.ibm.database.compatibility.test.generator.datatypes.CharColumn;
import com.ibm.database.compatibility.test.generator.datatypes.Column;
import com.ibm.database.compatibility.test.generator.datatypes.FloatColumn;
import com.ibm.database.compatibility.test.generator.datatypes.IntColumn;
import com.ibm.database.compatibility.test.generator.datatypes.VarcharColumn;

public class PreparedStatementTest {
	
	public static int nInserts = 10;
	
	public static void generateMultiplePreparedStatementTest(String filename) throws IOException {
		String testName = "test for interleaving multiple prepared statements";
		String sessionId = "testPstmt";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		TestGeneratorUtils.writeStartTestInfo(jow, testName);

		String tabName1 = "test1";
		List<Column> table1Columns = new ArrayList<Column>();
		table1Columns.add(new IntColumn("int_col", 93));
		table1Columns.add(new VarcharColumn("varchar_col", 20, 44, false));
		table1Columns.add(new CharColumn("char_col", 20, 21, false));
		table1Columns.add(new VarcharColumn("varchar_col2", 20, 15, false));
		String tabName2 = "test2";
		List<Column> table2Columns = new ArrayList<Column>();
		table2Columns.add(new IntColumn("int_col",88));
		table2Columns.add(new FloatColumn("float_col", 88));
		table2Columns.add(new VarcharColumn("varchar_col", 20, 91, false));

		jow.write(TestGeneratorUtils.getCreateSessionOperation(sessionId));
		jow.writeComment("creating tables");
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName1));
		jow.write(TestGeneratorUtils.getCreateTableOperation("ddl", tabName1, table1Columns));
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName2));
		jow.write(TestGeneratorUtils.getCreateTableOperation("ddl", tabName2, table2Columns));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("ddl"));
		
		jow.writeComment("insert initial data... switching off between two insert pstmts");
		jow.write(TestGeneratorUtils.getInsertPstmtOperation("pstmt1", tabName1, table1Columns.size()));
		jow.write(TestGeneratorUtils.getInsertPstmtOperation("pstmt2", tabName2, table2Columns.size()));
		JsonArray table1Data = new JsonArray();
		JsonArray table2Data = new JsonArray();
		JsonObject row;
		for (int i = 0; i < nInserts; i++) {
			row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "pstmt1", table1Columns, i);
			table1Data.add(row);
			row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "pstmt2", table2Columns, i);
			table2Data.add(row);
		}
		
		jow.writeComment("add third pstmt for query, interleaving that with some more inserts");
		jow.writeComment("pstmt1 = insert on table1, pstmt2 = insert on table2, pstmt3 = query on table2");
		jow.write(TestGeneratorUtils.getCreatePreparedStatementForQueryOperation("pstmt3", "select * from " + tabName2));
		row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "pstmt2", table2Columns, 40);
		table2Data.add(row);
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt3", table2Data));
		row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "pstmt1", table1Columns, 41);
		table1Data.add(row);
		row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "pstmt2", table2Columns, 41);
		table2Data.add(row);
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt3", table2Data));
		
		jow.writeComment("now switch two query pstmts and one delete pstmt");
		jow.writeComment("pstmt1 = delete on table1, pstmt2 = query on table1, pstmt3 = query on table2");
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("pstmt1"));
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("pstmt2"));
		Column table1DeleteColumn = table1Columns.get(3);
		jow.write(TestGeneratorUtils.getDeletePstmtOperation("pstmt1", tabName1, table1DeleteColumn));
		jow.write(TestGeneratorUtils.getCreatePreparedStatementForQueryOperation("pstmt2", "select * from " + tabName1));
		JsonObject deletedRow = (JsonObject) table1Data.remove(3);
		BindingsBuilder bb = new Binding.BindingsBuilder().add(1, deletedRow.get(table1DeleteColumn.getColumnName()).getAsString(), table1DeleteColumn.getColumnTypeName());
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt1", bb.build()));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt2", table1Data));
		deletedRow = (JsonObject) table1Data.remove(2);
		bb = new Binding.BindingsBuilder().add(1, deletedRow.get(table1DeleteColumn.getColumnName()).getAsString(), table1DeleteColumn.getColumnTypeName());
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt1", bb.build()));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt3", table2Data));
		deletedRow = (JsonObject) table1Data.remove(7);
		bb = new Binding.BindingsBuilder().add(1, deletedRow.get(table1DeleteColumn.getColumnName()).getAsString(), table1DeleteColumn.getColumnTypeName());
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt1", bb.build()));
		deletedRow = (JsonObject) table1Data.remove(5);
		bb = new Binding.BindingsBuilder().add(1, deletedRow.get(table1DeleteColumn.getColumnName()).getAsString(), table1DeleteColumn.getColumnTypeName());
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt1", bb.build()));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt3", table2Data));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("pstmt2", table1Data));
		
		jow.writeComment("close prepared statements");
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("pstmt1"));
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("pstmt2"));
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("pstmt3"));
		
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName1));
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName2));
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("ddl"));

		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void generatePreparedStatementCursorTest(String filename) throws IOException {
		String testName = "test for running multiple prepared statements with query cursors";
		String sessionId = "testPstmt";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		TestGeneratorUtils.writeStartTestInfo(jow, testName);
		
		String tabName1 = "test1";
		List<Column> table1Columns = new ArrayList<Column>();
		table1Columns.add(new IntColumn("id", 1));
		table1Columns.add(new CharColumn("location", 20, 18, false));
		table1Columns.add(new CharColumn("home", 20, 5, false));
		table1Columns.add(new VarcharColumn("notes", 20, 23, false));
		String tabName2 = "test2";
		List<Column> table2Columns = new ArrayList<Column>();
		table2Columns.add(new IntColumn("id", 11));
		table2Columns.add(new FloatColumn("price", 88));
		table2Columns.add(new VarcharColumn("notes", 20, 91, false));

		jow.write(TestGeneratorUtils.getCreateSessionOperation(sessionId));
		jow.writeComment("creating tables");
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName1));
		jow.write(TestGeneratorUtils.getCreateTableOperation("ddl", tabName1, table1Columns));
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName2));
		jow.write(TestGeneratorUtils.getCreateTableOperation("ddl", tabName2, table2Columns));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("ddl"));
		
		jow.writeComment("insert initial data");
		jow.write(TestGeneratorUtils.getInsertPstmtOperation("insert1", tabName1, table1Columns.size()));
		jow.write(TestGeneratorUtils.getInsertPstmtOperation("insert2", tabName2, table2Columns.size()));
		JsonArray table1Data = new JsonArray();
		JsonArray table2Data = new JsonArray();
		JsonObject row;
		for (int i = 0; i < nInserts; i++) {
			row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "insert1", table1Columns, i);
			table1Data.add(row);
			row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "insert2", table2Columns, i);
			table2Data.add(row);
		}
		jow.write(TestGeneratorUtils.getCloseStatementOperation("insert1"));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("insert2"));

		jow.writeComment("create two query prepared statements... switch off fetching some rows from each");
		jow.write(TestGeneratorUtils.getCreatePreparedStatementForQueryOperation("query1", "select * from " + tabName1 + " order by " + table1Columns.get(0).getColumnName()));
		jow.write(TestGeneratorUtils.getCreatePreparedStatementForQueryOperation("query2", "select * from " + tabName2 + " order by " + table2Columns.get(0).getColumnName()));
		JsonArray expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(0));
		expectedResults.add(table1Data.get(1));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query1", 2, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table2Data.get(0));
		expectedResults.add(table2Data.get(1));
		expectedResults.add(table2Data.get(2));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query2", 3, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(2));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query1", 1, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table2Data.get(3));
		expectedResults.add(table2Data.get(4));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query2", 2, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(3));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query1", 1, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table2Data.get(5));
		expectedResults.add(table2Data.get(6));
		expectedResults.add(table2Data.get(7));
		expectedResults.add(table2Data.get(8));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query2", 4, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(4));
		expectedResults.add(table1Data.get(5));
		expectedResults.add(table1Data.get(6));
		expectedResults.add(table1Data.get(7));
		expectedResults.add(table1Data.get(8));
		expectedResults.add(table1Data.get(9));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query1", null, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table2Data.get(9));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query2", null, expectedResults));
		
		jow.writeComment("re-execute the queries with more interwoven fetches");
		expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(0));
		expectedResults.add(table1Data.get(1));
		expectedResults.add(table1Data.get(2));
		expectedResults.add(table1Data.get(3));
		expectedResults.add(table1Data.get(4));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query1", 5, expectedResults));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query2", table2Data));
		expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(5));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query1", 1, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(6));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query1", 1, expectedResults));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query2", table2Data));
		expectedResults = new JsonArray();
		expectedResults.add(table2Data.get(0));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query2", 1, expectedResults));
		expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(7));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query1", 1, expectedResults));
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query2", table2Data));
		expectedResults = new JsonArray();
		expectedResults.add(table1Data.get(8));
		jow.write(TestGeneratorUtils.getFetchPstmtOperation("query1", 1, expectedResults));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("query1"));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("query2"));
		
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName1));
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName2));
		jow.write(TestGeneratorUtils.getClosePresparedStatementOperation("ddl"));
		
		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}

	
	public static void main(String[] args) throws IOException {
		generateMultiplePreparedStatementTest("resources/preparedStatementTest_1.json");
		generatePreparedStatementCursorTest("resources/preparedStatementTest_2.json");
	}

}
