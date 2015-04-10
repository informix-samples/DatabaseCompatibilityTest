package com.ibm.database.compatibility;

public enum SqlDataType {
	BIGINT, //
	BIGSERIAL, //
	BOOLEAN, //
	BYTE, //
	CHAR, //
	DATE, //
	DATETIME, //
	DECIMAL, //
	DOUBLE_PRECISION, //
	INT, //
	INT8, //
	//INTERVAL, //Not supported in DB2
	LONG, //
	LVARCHAR, //
	NCHAR, //
	OBJECT, //
	SERIAL, //
	SERIAL8, //
	SMALLFLOAT, //
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
