package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public abstract class AbstractColumn implements Column {

	protected SqlDataType columnType;
	protected final String columnName;
	protected final int seed; 
	
	public AbstractColumn(SqlDataType colType, String colName, int seed) {
		this.columnType = colType;
		this.columnName = colName;
		this.seed = seed;
	}
	
	public String getColumnName() {
		return this.columnName;
	}
	
	public String getColumnTypeName() {
		return columnType.toString();
	}

	public String getColumnTypeAsSQLString() {
		return columnType.toString();
	}
}
