package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class BigIntColumn extends AbstractColumn implements Column {

	protected static int nunique = 30;
	
	public BigIntColumn(String colName, int seed) {
		super(SqlDataType.BIGINT, colName, seed);
	}
	
	public long getNumericValue(int i) {
		return ((Double) Math.pow(2, 32 + ((seed + i) % nunique))).longValue();
	}
	
	public Object getValue(int i) {
		return (Long) getNumericValue(i);
	}
	
	public Class<?> getJavaClassName() {
		return Long.class;
	}

}
