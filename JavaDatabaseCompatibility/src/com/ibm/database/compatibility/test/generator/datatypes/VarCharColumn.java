package com.ibm.database.compatibility.test.generator.datatypes;

import java.util.Arrays;
import java.util.List;

import com.ibm.database.compatibility.SqlDataType;

public class VarCharColumn extends AbstractColumn implements Column {

	protected int colLength;
	protected boolean fillColumn = false;
	
	protected static final List<String> dataValues = Arrays.asList(
		"Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", 
		"New Meixco", "New York", "North Carolina", "North Dakota", "Ohio", 
		"Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", 
		"South Dakota", "Tennessee", "Texas", "Utah", "Vermont", 
		"Virgina", "Washington", "West Virginia", "Wisconsin", "Wyoming"
		);
	protected static final int nunique = dataValues.size();
	
	public VarCharColumn(String colName, int colLength, int seed, boolean fillColumn) {
		super(SqlDataType.VARCHAR, colName, seed);
		this.colLength = colLength;
		this.fillColumn = fillColumn;
	}
	
	@Override
	public String getColumnTypeAsSQLString() {
		return super.getColumnTypeName() + "(" + colLength + ")";
	}
	
	public Object getValue(int i) {
		String v = dataValues.get((i + seed) % nunique);
		if (fillColumn) {
			while (v.length() < colLength) {
				v = v + v;
			}
		}
		if (v.length() > colLength) {
			v = v.substring(0, colLength);
		}
		return v;
	}
	
	public boolean isNumeric() {
		return false;
	}

}
