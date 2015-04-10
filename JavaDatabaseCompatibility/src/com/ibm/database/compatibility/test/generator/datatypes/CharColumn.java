package com.ibm.database.compatibility.test.generator.datatypes;

import java.util.Arrays;
import java.util.List;

import com.ibm.database.compatibility.SqlDataType;

public class CharColumn extends AbstractColumn implements Column {

	protected int colLength;
	protected boolean fillColumn = false;
	
	protected static final List<String> dataValues = Arrays.asList(
		"Alabama", "Alaska", "Arizona", "Arkansas", "California",
		"Colorado", "Connecticut", "Delaware", "Florida", "Georgia",
		"Hawaii", "Idaho", "Illinois", "Indiana", "Iowa",
		"Kansas", "Kentucky", "Louisiana", "Maine", "Maryland",
		"Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri"
		);
	protected static final int nunique = dataValues.size();
	
	public CharColumn(String colName, int colLength, int seed, boolean fillColumn) {
		super(SqlDataType.CHAR, colName, seed);
		this.colLength = colLength;
		this.fillColumn = fillColumn;
	}

	@Override
	public String getColumnTypeAsSQLString() {
		return super.getColumnTypeName() + "(" + colLength + ")";
	}
	
	public Object getValue(int i) {
		String v = dataValues.get((i + seed) % nunique);
		StringBuilder sb = new StringBuilder(v);
		if (fillColumn) {
			while (sb.length() < colLength) {
				sb.append(v);
			}
		} else {
			// pad with spaces, so comparison to value returned by Informix matches
			while (sb.length() < colLength) {
				sb.append(" ");
			}
		}
		v = sb.toString();
		if (v.length() > colLength) {
			 v = v.substring(0, colLength);
		}
		return v;
	}
	
	public Class<?> getJavaClassName() {
		return String.class;
	}
}
