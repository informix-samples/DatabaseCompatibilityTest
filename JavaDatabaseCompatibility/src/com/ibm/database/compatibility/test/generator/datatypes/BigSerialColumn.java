package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class BigSerialColumn extends AbstractColumn implements Column {

	protected long lastValue = 0;
	protected boolean autoIncrement = true;

	public BigSerialColumn(String colName, int seed, boolean autoIncrement) {
		super(SqlDataType.BIGSERIAL, colName, seed);
		this.autoIncrement = autoIncrement;
		this.lastValue = 2147483648L + seed - 1;
	}
	
	public Object getValue(int i) {
		if (autoIncrement) {
			// i is irrelevant, just return next value for serial column
			lastValue++;
			return lastValue;
		} else {
			return (2147483648L + seed + i);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Class getJavaClassName() {
		return Long.class;
	}

}
