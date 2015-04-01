package com.ibm.database.compatibility;

public enum SqlDataType {
	BIGINT, //
	BOOLEAN, //
	BYTE, //
	CHAR, //
	DATE, //
	DOUBLE, //
	FLOAT, //
	INT, //
	INT8, //
	LONG, //
	LVARCHAR, //
	NCHAR, //
	OBJECT, //
	SMALLINT, //
	TIME, //
	TIMESTAMP, //
	VARCHAR; //

	public static SqlDataType lookup(String name) {
		for (SqlDataType sdt : SqlDataType.values()) {
			if (sdt.name().equalsIgnoreCase(name)) {
				return sdt;
			}
		}
		return null;
	}
	
}
