package com.ibm.database.compatibility;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationReaderRunner implements Runnable {

	private final JdbcClient client;
	private final OperationReader os;
	private final String testName;
	private static final Logger logger = LoggerFactory.getLogger(OperationReaderRunner.class);
	private TestResult testResult;

	public OperationReaderRunner(JdbcClient client, OperationReader os, String testName) {
		this.client = client;
		this.os = os;
		this.testName = testName;
	}

	@Override
	public void run() {
		logger.debug("starting test " + testName);
		int successfulOps = 0;
		int nonFatalErrors = 0;
		int fatalErrors = 0;
		List<String> errors = new ArrayList<String>();
		try {
			while (this.os.hasNext()) {
				Operation op = this.os.next();
				if (op != null) {
					//System.out.println(op.toString());
					op.invoke(this.client);
					if (op.getErrorCount() == 0) {
						successfulOps++;
					} else {
						nonFatalErrors += op.getErrorCount();
						errors.addAll(op.getErrorMessages());
					}
				}
			}
		} catch (SQLException e) {
			fatalErrors = 1;
			logger.error("SQLException:" + e.getErrorCode() + " " + e.getMessage(), e);
			errors.add("SQLException:" + e.getErrorCode() + " " + e.getMessage());
		} catch (Exception e) {
			fatalErrors = 1;
			logger.error("Exception:" + e.getMessage(), e);
			errors.add("Exception:" +  e.getMessage());
		}
		
		logger.debug("end of test");
		if (nonFatalErrors == 0 && fatalErrors == 0) {
			logger.info("TEST [" + testName + "] PASSED");
		} else {
			logger.info("TEST [" + testName + "]  FAILED");
		}
		logger.info(MessageFormat.format("TEST successful operations: {0}", successfulOps));
		if(nonFatalErrors > 0 || fatalErrors > 0) {
			logger.info(MessageFormat.format("TEST errors: {0}", nonFatalErrors + fatalErrors));
			logger.info(MessageFormat.format("TEST fatal errors: {0} ", fatalErrors));
		}
		
		testResult = new TestResult(DatabaseCredential.getJDBCDriverClassName(), testName, 
				nonFatalErrors == 0 && fatalErrors == 0, 
				(errors.size() > 0)? errors.toString() : null);
	}
	
	public TestResult getTestResult() {
		return this.testResult;
	}

	public static void main(String[] args) {
		JdbcClient client = new BasicJdbcClient();
		JsonOperationReader os = null;
		if (args.length != 1) {
			throw new RuntimeException(
					"Must supply the json test file as an argument");
		}
		
		ArrayList<File>testFiles = new ArrayList<File>();
		File f = new File (args[0]);
		if(f.isDirectory()) {
			String [] files = f.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".json");
					
				}
			});
			for(String file : files) {
				testFiles.add(new File(f, file));
			}
		}
		else {
			testFiles.add(f);
		}
		String driver = DatabaseCredential.getJDBCDriverClassName().equals(DatabaseCredential.DB2_JCC_CLASSNAME) ? "db2jcc" : "ifxjdbc";
		TestResultWriter resultWriter = null;
		try {
			resultWriter = new TestResultWriter("results/results_java_" + driver + ".json");
			for(File testFile : testFiles) {
				try {
					os = new JsonOperationReader(testFile.getAbsolutePath());
					OperationReaderRunner osr = new OperationReaderRunner(client, os, testFile.getName());
					osr.run();
					resultWriter.write(osr.getTestResult());
				}catch (Exception e) {
					if (os != null) {
						try {
							os.close();
						} catch (IOException e1) {
						}
					}
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (resultWriter != null) {
				try {
					resultWriter.close();
				} catch (IOException e1) {
				}
			}
		}
		
	}
}
