package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class SerialColumn extends AbstractColumn implements Column {

	protected int lastValue = 0;
	protected boolean autoIncrement = true;

	public SerialColumn(String colName, int seed, boolean autoIncrement) {
		super(SqlDataType.SERIAL, colName, seed);
		this.autoIncrement = autoIncrement;
		this.lastValue = seed - 1;
	}
	
	public Object getValue(int i) {
		if (autoIncrement) {
			// i is irrelevant, just return next value for serial column
			lastValue++;
			return lastValue;
		} else {
			return (seed + i);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Class getJavaClassName() {
		return Integer.class;
	}

}
