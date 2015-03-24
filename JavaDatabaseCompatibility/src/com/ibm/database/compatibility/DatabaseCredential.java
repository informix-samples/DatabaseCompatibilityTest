package com.ibm.database.compatibility;

public class DatabaseCredential {
	//private static final Logger logger = LoggerFactory.getLogger(DatabaseCredential.class);
	
	private String credentialId = null;
	private String host = null;
	private Integer port = null;
	private String databaseName = null;
	private String user = null;
	private String password = null;
	private String additionalConnectionProperties = null;
	
	private DatabaseCredential() {
		
	}
	
	public static class Builder {
		private String credentialId = null;
		private String host = null;
		private Integer port = null;
		private String databaseName = null;
		private String user = null;
		private String password = null;
		private String additionalConnectionProperties = null;
		
		public synchronized DatabaseCredential build() {
			DatabaseCredential dc = new DatabaseCredential();
			dc.credentialId = credentialId;
			dc.host = host;
			dc.port = port;
			dc.databaseName = databaseName;
			dc.user = user;
			dc.password = password;
			dc.additionalConnectionProperties = additionalConnectionProperties;
			return dc;
		}
		
		public Builder credentialId(final String credentialId) {
			this.credentialId = credentialId;
			return this;
		}
		
		public Builder host(final String host) {
			this.host = host;
			return this;
		}
		
		public Builder port(final Integer port) {
			this.port = port;
			return this;
		}
		
		public Builder databaseName(final String databaseName) {
			this.databaseName = databaseName;
			return this;
		}
		
		public Builder user(final String user) {
			this.user = user;
			return this;
		}
		
		public Builder password(final String password) {
			this.password = password;
			return this;
		}
		
		public Builder additionalConnectionProperties(final String additionalConnectionProperties) {
			this.additionalConnectionProperties = additionalConnectionProperties;
			return this;
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
