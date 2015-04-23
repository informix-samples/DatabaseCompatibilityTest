package com.ibm.database.compatibility.test.generator.datatypes;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.ibm.database.compatibility.SqlDataType;

public class DateTimeColumn extends AbstractColumn implements Column {
	private final int fractionSize;
	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final List<Object> dataValues = new ArrayList<Object>();
	private final int nunique;
	
	public DateTimeColumn(String colName, int fractionSize, int seed) {
		super(SqlDataType.DATETIME, colName, seed);
		this.fractionSize = fractionSize;
		initializeData();
		nunique = dataValues.size();
	}
	
	public void initializeData() {
		try {
			Random r = new Random(seed);
			for(int i = 0; i < 12; i++) {
				StringBuilder sb = new StringBuilder();
				sb.append(r.nextInt(50) + 2000);//year
				sb.append('-');
				sb.append(r.nextInt(11));	//month
				sb.append('-');
				sb.append(r.nextInt(28));	//day
				sb.append(' ');
				sb.append(r.nextInt(23));	//hour
				sb.append(':');
				sb.append(r.nextInt(59)); 	//minute
				sb.append(':');
				sb.append(r.nextInt(59)); 	//second
				sb.append('.');
				if( fractionSize >= 1) {
					sb.append(r.nextInt(9));
				}
				else {
					sb.append(0);
				}
				if( fractionSize >= 2) {
					sb.append(r.nextInt(9));
				}
				else {
					sb.append(0);
				}
				if( fractionSize >= 3) {
					sb.append(r.nextInt(9));
				}
				else {
					sb.append(0);
				}
				Date d = format.parse(sb.toString());
				Timestamp ts = new Timestamp(d.getTime());
				if(fractionSize > 3) {
					if (fractionSize == 4) {
						ts.setNanos(ts.getNanos() + (r.nextInt(9) * 100000));
					} else {
						ts.setNanos(ts.getNanos() + (r.nextInt(99) * 10000));
					}
				}
				//System.out.println(sb);
				//System.out.println(ts);
				if (fractionSize <= 3) {
					dataValues.add(ts.getTime());
				} else {
					String tsString = ts.toString();
					while (tsString.substring(tsString.indexOf(".") + 1).length() != fractionSize) {
						tsString = tsString + "0";
					}
					dataValues.add(tsString);
				}
			}
		}
		catch(ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getColumnTypeAsSQLString() {
		if (fractionSize == 0) {
			return "DATETIME YEAR TO SECOND";
		} else {
			return "DATETIME YEAR TO FRACTION(" + fractionSize + ")";
		}
	}

	@Override
	public Object getValue(int i) {
		return dataValues.get((i + seed) % nunique);
	}

	@Override
	public Class<?> getJavaClassName() {
		if (fractionSize <=3) {
			return Long.class;
		} else {
			return String.class;
		}
	}
}
