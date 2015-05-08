package com.ibm.database.compatibility.test.generator.datatypes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ibm.database.compatibility.SqlDataType;

public class DateColumn extends AbstractColumn implements Column {

	protected List<String> dataValues = new ArrayList<String>();
	protected final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	protected final int nunique;
	
	public DateColumn(String colName, int seed) {
		super(SqlDataType.DATE, colName, seed);
		initializeData();
		nunique = dataValues.size();
	}
	
	public void initializeData() {
		dataValues.add("2014-10-15");
		dataValues.add("2014-10-24");
		dataValues.add("2014-11-30");
		dataValues.add("2014-12-01");
		dataValues.add("2014-12-22");
		dataValues.add("2015-01-01");
		dataValues.add("2015-01-14");
		dataValues.add("2015-01-31");
		dataValues.add("2015-03-07");
		dataValues.add("2015-03-09");
		dataValues.add("2015-03-20");
	}

	public Object getValue(int i) {
		return dataValues.get((i + seed) % nunique);
	}
	
	public Class<?> getJavaClassName() {
		return String.class;
	}
}
