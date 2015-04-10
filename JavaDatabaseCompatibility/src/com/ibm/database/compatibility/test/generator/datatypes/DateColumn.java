package com.ibm.database.compatibility.test.generator.datatypes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.ibm.database.compatibility.SqlDataType;

public class DateColumn extends AbstractColumn implements Column {

	protected List<Long> dataValues = new ArrayList<Long>();
	protected final int nunique;
	
	public DateColumn(String colName, int seed) {
		super(SqlDataType.DATE, colName, seed);
		initializeData();
		nunique = dataValues.size();
	}
	
	public void initializeData() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 10, 15);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2014, 10, 24);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2014, 11, 30);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2014, 12, 1);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2014, 12, 14);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2014, 12, 22);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2015, 1, 1);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2015, 1, 14);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2015, 1, 31);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2015, 3, 7);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2015, 3, 9);
		dataValues.add(calendar.getTimeInMillis());
		calendar.set(2015, 3, 20);
		dataValues.add(calendar.getTimeInMillis());
	}

	public Object getValue(int i) {
		return dataValues.get((i + seed) % nunique);
	}
	
	public Class<?> getJavaClassName() {
		return Long.class;
	}
}
