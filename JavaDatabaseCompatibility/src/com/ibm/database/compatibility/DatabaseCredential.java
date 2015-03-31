package com.ibm.database.compatibility;

public class DatabaseCredential {
	//private static final Logger logger = LoggerFactory.getLogger(DatabaseCredential.class);
	
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
	
	public synchronized String getCredentialId() {
		return this.credentialId;
	}
	
	public synchronized String getUrl() {
		final String jdbcUrl = "jdbc:informix-sqli://" + this.host + ":" + this.port + "/" + this.databaseName + ":USER=" + this.user + ";PASSWORD=" + this.password + ";" + this.additionalConnectionProperties;
		return jdbcUrl;
	}
	
	@Override
	public String toString() {
		return GsonUtils.newGson().toJson(this);
	}
	
}
