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

	private final Map<String, JdbcSession> sessions = new HashMap<String, JdbcSession>();
	private String lastUrl = null;
	private String lastSessionId = "default";

	@Override
	public JdbcSession newSession(String url, String id) throws SQLException {
		if (url != null) {
			this.lastUrl = url;
		}
		if (id != null) {
			this.lastSessionId = id;
		}
		JdbcSession session = new BasicJdbcSession(this, lastSessionId, this.lastUrl);
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

}
