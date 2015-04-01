package com.ibm.database.compatibility;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationReaderRunner implements Runnable {

	private final JdbcClient client;
	private final OperationReader os;
	
	private static final Logger logger = LoggerFactory.getLogger(OperationReaderRunner.class);

	public OperationReaderRunner(JdbcClient client, OperationReader os) {
		this.client = client;
		this.os = os;
	}

	@Override
	public void run() {
		logger.debug("starting test");
		int successfulOps = 0;
		int nonFatalErrors = 0;
		int fatalErrors = 0;
		try {
			while (os.hasNext()) {
				Operation op = os.next();
				if (op != null) {
					//System.out.println(op.toString());
					op.invoke(client);
					if (op.getErrorCount() == 0) {
						successfulOps++;
					} else {
						nonFatalErrors += op.getErrorCount();
					}
				}
			}
		} catch (SQLException e) {
			fatalErrors = 1;
			logger.error("SQLException:" + e.getErrorCode() + " " + e.getMessage(), e);
		} catch (Exception e) {
			fatalErrors = 1;
			logger.error("Exception:" + e.getMessage(), e);
		}
		
		logger.debug("end of test");
		if (nonFatalErrors == 0 && fatalErrors == 0) {
			logger.info("TEST PASSED");
		} else {
			logger.info("TEST FAILED");
		}
		logger.info(MessageFormat.format("successful operations: {0}", successfulOps));
		logger.info(MessageFormat.format("errors: {0}", nonFatalErrors + fatalErrors));
		logger.info(MessageFormat.format("fatal errors: {0} ", fatalErrors));

		if (nonFatalErrors == 0 && fatalErrors == 0) {
			System.out.println("\n\nTEST PASSED");
		} else {
			System.err.println("\n\nTEST FAILED");	
		}
		System.out.println(MessageFormat.format("successful operations: {0}", successfulOps));
		System.out.println(MessageFormat.format("errors: {0}", nonFatalErrors + fatalErrors));
		System.out.println(MessageFormat.format("fatal errors: {0} ", fatalErrors));

	}

	public static void main(String[] args) {
		JdbcClient client = new BasicJdbcClient();
		JsonOperationReader os = null;
		try {
			os = new JsonOperationReader("resources/dataTypeTest_CHAR.json");
			OperationReaderRunner osr = new OperationReaderRunner(client, os);
			osr.run();
		} catch (Exception e) {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e1) {
				}
			}
			e.printStackTrace();
		}
	}

}
