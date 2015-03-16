package com.ibm.database.compatibility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic implementation of the JDBC client interface. This class enables use
 * of more than one session, identified by a textual id. Each session represents
 * a single connection to the database server.
 * 
 */
public class BasicJdbcClient implements JdbcClient {

	private final Map<String, DatabaseCredential> credentials = new HashMap<String, DatabaseCredential>();
	private final Map<String, JdbcSession> sessions = new HashMap<String, JdbcSession>();
	private String lastCredentialId = "default";
	private String lastSessionId = "default";

	@Override
	public JdbcSession newSession(String id) throws SQLException {
		if (id != null) {
			this.lastSessionId = id;
		}
		JdbcSession session = new BasicJdbcSession(this, lastSessionId, lastCredentialId);
		this.sessions.put(lastSessionId, session);
		return session;
	}

	@Override
	public synchronized JdbcSession getJdbcSession(String id) {
		if (id == null) {
			return sessions.get(lastSessionId);
		} else {
			return sessions.get(id);
		}
	}

	@Override
	public synchronized JdbcSession putJdbcSession(String id, JdbcSession c) {
		return this.sessions.put(id, c);
	}

	@Override
	public synchronized JdbcSession removeJdbcSession(String id) {
		JdbcSession c = this.sessions.remove(id);
		if (c != null) {
			try {
				c.close();
			} catch (Exception e) {
				// do nothing
			}
		}
		return c;
	}

	@Override
	public Connection newConnection(String url) throws SQLException {
		return DriverManager.getConnection(url);
	}

	@Override
	public synchronized JdbcSession[] getJdbcSessions() {
		return this.sessions.values().toArray(new JdbcSession[0]);
	}

	@Override
	public DatabaseCredential newCredential(String credentialId, String host,
			Integer port, String databaseName, String user, String password,
			String additionalConnectionProperties) {
		if(credentialId != null) {
			this.lastCredentialId = credentialId;
		}
		DatabaseCredential credential = new DatabaseCredential.Builder().credentialId(this.lastCredentialId).host(host).port(port).databaseName(databaseName).user(user).password(password).additionalConnectionProperties(additionalConnectionProperties).build();
		this.credentials.put(lastCredentialId, credential);
		return credential;
	}

	@Override
	public synchronized DatabaseCredential removeCredential(String credentialId) {
		DatabaseCredential dc = this.credentials.remove(credentialId);
		return dc;
	}

	@Override
	public synchronized DatabaseCredential getDatabaseCredential(String credentialId) {
		if(credentialId == null) {
			return this.credentials.get(this.lastCredentialId);
		} else {
			return this.credentials.get(credentialId);
		}
		
	}

}
