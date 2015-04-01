package com.ibm.database.compatibility;

public enum SqlDataType {
	BIGINT, //
	BIGSERIAL, //
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
	SERIAL, //
	SERIAL8, //
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
