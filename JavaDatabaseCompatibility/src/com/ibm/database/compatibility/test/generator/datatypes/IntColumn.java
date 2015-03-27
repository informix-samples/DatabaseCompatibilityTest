package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class IntColumn extends AbstractColumn implements Column {

	protected static int nunique = 100;
	
	public IntColumn(String colName, int seed) {
		super(SqlDataType.INT, colName, seed);
	}
	
	public Number getNumericValue(int i) {
		return (seed + i) % nunique;
	}
	
	public Object getValue(int i) {
		return (Integer) getNumericValue(i);
	}
	
	@SuppressWarnings("rawtypes")
	public Class getJavaClassName() {
		return Integer.class;
	}

}
