package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class DecimalColumn extends AbstractColumn implements Column {

	private static int nunique = 100;

	public DecimalColumn(String colName, int seed) {
		// TODO: Informix decimal mapping to Java type?
		super(SqlDataType.DOUBLE_PRECISION, colName, seed);
	}
	
	public Object getValue(int i) {
		return (Double) getNumericValue(i);
	}
	
	public Number getNumericValue(int i) {
		return (double) (seed + Math.PI) * (i % nunique);
	}
	
	public Class<?> getJavaClassName() {
		return Double.class;
	}
	
}
