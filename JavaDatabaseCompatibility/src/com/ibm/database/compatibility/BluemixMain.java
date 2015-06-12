package com.ibm.database.compatibility;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/dbtest")
public class BluemixMain {

	@GET
	public String doDatabaseTest() {
		OperationReaderRunner.main(new String[]{
				"/WEB-INF/resources/dataTypeTest_BIGINT.json",
				"/WEB-INF/resources/dataTypeTest_BIGSERIAL.json",
				"/WEB-INF/resources/dataTypeTest_BOOLEAN.json",
				"/WEB-INF/resources/dataTypeTest_CHAR.json",
				"/WEB-INF/resources/dataTypeTest_DATE.json",
				"/WEB-INF/resources/dataTypeTest_DATETIME0.json",
				"/WEB-INF/resources/dataTypeTest_DATETIME1.json",
				"/WEB-INF/resources/dataTypeTest_DATETIME2.json",
				"/WEB-INF/resources/dataTypeTest_DATETIME3.json",
				"/WEB-INF/resources/dataTypeTest_DATETIME4.json",
				"/WEB-INF/resources/dataTypeTest_DATETIME5.json",
				"/WEB-INF/resources/dataTypeTest_DECIMAL.json",
				"/WEB-INF/resources/dataTypeTest_INT.json",
				"/WEB-INF/resources/dataTypeTest_INT8.json",
				"/WEB-INF/resources/dataTypeTest_LVARCHAR.json",
				"/WEB-INF/resources/dataTypeTest_NVARCHAR.json",
				"/WEB-INF/resources/dataTypeTest_SERIAL.json",
				"/WEB-INF/resources/dataTypeTest_SERIAL8.json",
				"/WEB-INF/resources/dataTypeTest_SMALLFLOAT.json",
				"/WEB-INF/resources/dataTypeTest_SMALLINT.json",
				"/WEB-INF/resources/dataTypeTest_VARCHAR.json",
				"/WEB-INF/resources/ops.json",
				"/WEB-INF/resources/preparedStatementTest_1.json",
				"/WEB-INF/resources/preparedStatementTest_2.json",
				"/WEB-INF/resources/transactionTest.json"
				});
		return "Test run complete. See log for results";
	}
}
