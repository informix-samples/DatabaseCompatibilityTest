package com.ibm.database.compatibility.test.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.database.compatibility.JsonOperationWriter;
import com.ibm.database.compatibility.test.generator.datatypes.Column;
import com.ibm.database.compatibility.test.generator.datatypes.IntColumn;
import com.ibm.database.compatibility.test.generator.datatypes.VarcharColumn;

public class TransactionTest {
	
	public static void generateTransactionTest(String filename) throws IOException {
		String testName = "transaction test";
		String tabName = "transaction_test";
		String sessionId = "testTxn";
		JsonOperationWriter jow = new JsonOperationWriter(filename);
		TestGeneratorUtils.writeStartTestInfo(jow, testName);

		List<Column> tableColumns = new ArrayList<Column>();
		tableColumns.add(new IntColumn("int_col", 93));
		tableColumns.add(new VarcharColumn("varchar_col", 20, 44, false));
		jow.write(TestGeneratorUtils.getCreateSessionOperation(sessionId));
		jow.writeComment("creating table");
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName));
		jow.write(TestGeneratorUtils.getCreateTableOperation("ddl", tabName, tableColumns));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("ddl"));
		
		jow.writeComment("start transaction");
		jow.write(TestGeneratorUtils.getStartTxnSessionOperation(sessionId));
		jow.writeComment("insert row");
		jow.write(TestGeneratorUtils.getInsertPstmtOperation("insert", tabName, tableColumns.size()));
		JsonObject row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "insert", tableColumns, 0);
		jow.writeComment("query table");
		jow.write(TestGeneratorUtils.getCreatePreparedStatementForQueryOperation("query", "SELECT * FROM " + tabName));
		JsonArray expectedResult = new JsonArray();
		expectedResult.add(row);
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", expectedResult));

		jow.writeComment("rollback transaction");
		jow.write(TestGeneratorUtils.getRollbackTxnSessionOperation(sessionId));
		jow.writeComment("query table");
		expectedResult = new JsonArray();
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", expectedResult));

		jow.writeComment("start transaction");
		jow.write(TestGeneratorUtils.getStartTxnSessionOperation(sessionId));
		jow.writeComment("insert rows");
		row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "insert", tableColumns, 5);
		expectedResult.add(row);
		row = TestGeneratorUtils.writeInsertExecuteStatement(jow, "insert", tableColumns, 22);
		expectedResult.add(row);
		jow.writeComment("query table");
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", expectedResult));

		jow.writeComment("commit transaction");
		jow.write(TestGeneratorUtils.getCommitTxnSessionOperation(sessionId));
		jow.writeComment("query table");
		jow.write(TestGeneratorUtils.getExecutePstmtOperation("query", expectedResult));

		jow.writeComment("drop table");
		jow.write(TestGeneratorUtils.getDropTableOperation("ddl", tabName));
		jow.write(TestGeneratorUtils.getCloseStatementOperation("ddl"));

		TestGeneratorUtils.writeEndTestInfo(jow, testName);
		jow.flush();
		jow.close();
	}
	
	public static void main(String[] args) throws IOException {
		generateTransactionTest("resources/transactionTest.json");
	}

}
