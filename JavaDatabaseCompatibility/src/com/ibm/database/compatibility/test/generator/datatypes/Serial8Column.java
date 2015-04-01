package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class Serial8Column extends BigSerialColumn implements Column {

	public Serial8Column(String colName, int seed, boolean autoIncrement) {
		super(colName, seed, autoIncrement);
		this.columnType = SqlDataType.SERIAL8;
	}
	
}
