package com.ibm.database.compatibility.test.generator.datatypes;

import com.ibm.database.compatibility.SqlDataType;

public class SmallIntColumn extends IntColumn implements Column {

	public SmallIntColumn(String colName, int seed) {
		super(colName, seed);
		this.columnType = SqlDataType.SMALLINT;
	}

}
