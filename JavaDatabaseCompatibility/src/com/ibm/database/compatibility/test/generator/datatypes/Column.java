package com.ibm.database.compatibility.test.generator.datatypes;

public interface Column {

	public String getColumnName();
	public String getColumnTypeName();
	public String getColumnTypeAsSQLString();
	public Object getValue(int i);
	public Class getJavaClassName();
}
