package com.ibm.database.compatibility.test.generator.datatypes;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import com.ibm.database.compatibility.SqlDataType;

public class NCharColumn extends AbstractColumn implements Column {

	protected int colLength;
	
	protected static final List<String> dataValues = Arrays.asList(
		"狗", "猫", "鱼", "鸟", "鼠", "兔", "鸭", "牛", "猪", "马"
		);
	protected static final int nunique = dataValues.size();
	
	public NCharColumn(String colName, int colLength, int seed) {
		super(SqlDataType.NCHAR, colName, seed);
		this.colLength = colLength;
	}
	
	@Override
	public String getColumnTypeAsSQLString() {
		return super.getColumnTypeName() + "(" + colLength + ")";
	}
	
	public Object getValue(int i) {
		try {
			int index = i + seed;
			String v = dataValues.get(index % nunique);
			while (v.getBytes("UTF-8").length < colLength) {
				String next = dataValues.get(index % nunique);;
				if (next.getBytes("UTF-8").length + v.getBytes("UTF-8").length > colLength){
					break;
				}
				v = v + next;
			}
			return v;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not generate nchar value", e);
		}
	}
	
	public Class<?> getJavaClassName() {
		return String.class;
	}

}
