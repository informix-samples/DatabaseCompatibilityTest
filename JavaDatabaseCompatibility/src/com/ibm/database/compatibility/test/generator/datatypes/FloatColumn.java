package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class FloatColumn extends AbstractColumn implements Column {

	private static int nunique = 100;

	public FloatColumn(String colName, int seed) {
		super(SqlDataType.FLOAT, colName, seed);
	}
	
	public Object getValue(int i) {
		return (Float) getNumericValue(i);
	}
	
	public Number getNumericValue(int i) {
		return (float) (seed + Math.PI) * (i % nunique);
	}
	
	public boolean isNumeric() {
		return true;
	}

}
