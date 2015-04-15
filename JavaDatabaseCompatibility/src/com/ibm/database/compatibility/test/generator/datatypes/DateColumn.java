package com.ibm.database.compatibility.test.generator.datatypes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ibm.database.compatibility.SqlDataType;

public class DateColumn extends AbstractColumn implements Column {

	protected List<Long> dataValues = new ArrayList<Long>();
	protected final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	protected final int nunique;
	
	public DateColumn(String colName, int seed) {
		super(SqlDataType.DATE, colName, seed);
		initializeData();
		nunique = dataValues.size();
	}
	
	public void initializeData() {
		try {
			dataValues.add(format.parse("2014-10-15").getTime());
			dataValues.add(format.parse("2014-10-24").getTime());
			dataValues.add(format.parse("2014-11-30").getTime());
			dataValues.add(format.parse("2014-12-01").getTime());
			dataValues.add(format.parse("2014-12-22").getTime());
			dataValues.add(format.parse("2015-01-01").getTime());
			dataValues.add(format.parse("2015-01-14").getTime());
			dataValues.add(format.parse("2015-01-31").getTime());
			dataValues.add(format.parse("2015-03-07").getTime());
			dataValues.add(format.parse("2015-03-09").getTime());
			dataValues.add(format.parse("2015-03-20").getTime());
		}
		catch(ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public Object getValue(int i) {
		return dataValues.get((i + seed) % nunique);
	}
	
	public Class<?> getJavaClassName() {
		return Long.class;
	}
}
