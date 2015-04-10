package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class BooleanColumn extends AbstractColumn implements Column {

	protected static final int nunique = 2;
	
	public BooleanColumn(String colName, int seed) {
		super(SqlDataType.BOOLEAN, colName, seed);
	}

	public Object getValue(int i) {
		return ((i + seed) % 2) == 0;
	}
	
	public boolean isNumeric() {
		return false;
	}
	
	public Class<?> getJavaClassName() {
		return Boolean.class;
	}
	
}
