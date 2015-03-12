package com.ibm.database.compatibility;

public enum SqlDataType {
	BOOLEAN, //
	BYTE, //
	CHAR, //
	SHORT, //
	INT, //
	LONG, //
	FLOAT, //
	DOUBLE, //
	VARCHAR, //
	OBJECT, //
	DATE, //
	TIME, //
	TIMESTAMP; //

	public static SqlDataType lookup(String name) {
		for (SqlDataType sdt : SqlDataType.values()) {
			if (sdt.name().equalsIgnoreCase(name)) {
				return sdt;
			}
		}
		return null;
	}
	
}
