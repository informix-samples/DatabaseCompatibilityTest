package com.ibm.database.compatibility;

public class TestResult {

	private String language = "java";
	private String client = null;
	private String server = "Informix";
	private String test = null;
	private boolean result;
	private String detail = null;
	
	public TestResult(String driverName, String testName, boolean result, String detail) {
		this.client = driverName;
		this.test = testName;
		this.result = result;
		this.detail = detail;
	}
	
	public String getLanguage() {
		return language;
	}

	public String getClient() {
		return client;
	}

	public String getServer() {
		return server;
	}

	public String getTest() {
		return test;
	}

	public boolean getResult() {
		return result;
	}

	public String getDetail() {
		return detail;
	}

}
