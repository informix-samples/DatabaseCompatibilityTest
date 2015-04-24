package com.ibm.database.compatibility;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseCredential {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseCredential.class);
	
	public static String DB2_JCC_CLASSNAME = "com.ibm.db2.jcc.DB2Driver";
	public static String INFORMIX_JDBC_CLASSNAME = "com.informix.jdbc.IfxDriver";

	private String jdbcClassName = INFORMIX_JDBC_CLASSNAME;
	private String credentialId = null;
	private String host = null;
	private Integer port = null;
	private String databaseName = null;
	private String user = null;
	private String password = null;
	private String additionalConnectionProperties = "";
	
	public DatabaseCredential(String credentialId) {
		this.credentialId = credentialId;
		getDatabaseCredentialsFromEnv();
		loadJDBCDriver();
	}
	
	private void getDatabaseCredentialsFromEnv() {
		// In Bluemix, we'll get this info from VCAP services
		if (System.getenv().containsKey("HOST")) {
			host = System.getenv("HOST");
		} else {
			throw new RuntimeException("Missing HOST env variable");
		}
		if (System.getenv().containsKey("PORT")) {
			port = Integer.parseInt(System.getenv("PORT"));
		} else {
			throw new RuntimeException("Missing PORT env variable");
		}
		if (System.getenv().containsKey("DATABASE")) {
			databaseName = System.getenv("DATABASE");
		} else {
			throw new RuntimeException("Missing DATABASE env variable");
		}
		if (System.getenv().containsKey("USER")) {
			user = System.getenv("USER");
		} else {
			throw new RuntimeException("Missing USER env variable");
		}
		if (System.getenv().containsKey("PASSWORD")) {
			password = System.getenv("PASSWORD");
		} else {
			throw new RuntimeException("Missing PASSWORD env variable");
		}
		if (System.getenv().containsKey("CONN_PROPERTIES")) {
			additionalConnectionProperties = System.getenv().get("CONN_PROPERTIES");
		}
	}
	
	private void loadJDBCDriver() {
		String className = INFORMIX_JDBC_CLASSNAME;
		if (System.getenv().containsKey("PROTOCOL") && System.getenv().get("PROTOCOL").equals("DRDA")) {
			className = DB2_JCC_CLASSNAME;
		}
		logger.info(MessageFormat.format("Loading JDBC class: {0}", className));
		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(MessageFormat.format("Unable to load class {0}", className), e);
		}
	}
	
	public synchronized String getCredentialId() {
		return this.credentialId;
	}
	
	public synchronized String getJdbcClassName() {
		return jdbcClassName;
	}
	
	public synchronized String getUrl() {
		String jdbcUrl;
		if (jdbcClassName.equals(DB2_JCC_CLASSNAME)) {
			jdbcUrl = "jdbc:ids://" + this.host + ":" + this.port + "/" + this.databaseName;
			if (this.additionalConnectionProperties != null && this.additionalConnectionProperties.length() > 0) {
				jdbcUrl += ":" + this.additionalConnectionProperties;
			}
		} else {
			jdbcUrl = "jdbc:informix-sqli://" + this.host + ":" + this.port + "/" + this.databaseName + ":USER=" + this.user + ";PASSWORD=" + this.password + ";" + this.additionalConnectionProperties;;
		}
		return jdbcUrl;
	}
	
	public synchronized String getUser() {
		return user;
	}

	public synchronized String getPassword() {
		return password;
	}
	
	@Override
	public String toString() {
		return GsonUtils.newGson().toJson(this);
	}
	
}
