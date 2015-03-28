package com.ibm.database.compatibility;

public enum SqlDataType {
	BOOLEAN, //
	BYTE, //
	CHAR, //
	DATE, //
	DOUBLE, //
	FLOAT, //
	INT, //
	LONG, //
	LVARCHAR, //
	NCHAR, //
	OBJECT, //
	SHORT, //
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
