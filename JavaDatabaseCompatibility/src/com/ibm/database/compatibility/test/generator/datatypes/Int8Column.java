package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class Int8Column extends BigIntColumn implements Column {

	public Int8Column(String colName, int seed) {
		super(colName, seed);
		this.columnType = SqlDataType.INT8;
	}

}
