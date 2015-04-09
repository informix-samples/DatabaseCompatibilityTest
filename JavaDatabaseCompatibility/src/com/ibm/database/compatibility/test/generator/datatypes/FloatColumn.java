package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class FloatColumn extends AbstractColumn implements Column {

	private static int nunique = 100;

	public FloatColumn(String colName, int seed) {
		super(SqlDataType.FLOAT, colName, seed);
	}
	
	@Override 
	public String getColumnTypeAsSQLString() {
		return this.getColumnTypeName();
	}
	
	public Object getValue(int i) {
		return (Float) getNumericValue(i);
	}
	
	public Number getNumericValue(int i) {
		return (float) (seed + Math.PI) * (i % nunique);
	}
	
	@SuppressWarnings("rawtypes")
	public Class getJavaClassName() {
		return Float.class;
	}
}
