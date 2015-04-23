package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class DecimalColumn extends AbstractColumn implements Column {

	private static int nunique = 100;
	
	private Integer precision = null;

	public DecimalColumn(String colName, int seed) {
		super(SqlDataType.DECIMAL, colName, seed);
	}
	
	public DecimalColumn(String colName, int precision, int seed) {
		this(colName, seed);
		this.precision = precision;
	}
	
	public Object getValue(int i) {
		return (Double) getNumericValue(i);
	}
	
	@Override
	public String getColumnTypeAsSQLString() {
		if (precision == null) {
			return this.columnType.name();
		} else {
			return this.columnType.name() + "(" + precision + ")";
		}
	}
	
	public Number getNumericValue(int i) {
		return (double) (Math.pow(seed + Math.PI, i)) * (i % nunique);
	}
	
	public Class<?> getJavaClassName() {
		return Double.class;
	}
	
}
