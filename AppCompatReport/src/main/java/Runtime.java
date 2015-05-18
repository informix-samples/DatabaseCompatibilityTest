
public class Runtime implements Comparable<Runtime> {
	
	private final String language;
	private final String client;
	private final String server;
	
	public Runtime(String language, String client, String server) {
		this.language = language;
		this.client = client;
		this.server = server;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getClient() {
		return this.client;
	}
	
	public String getServer() {
		return this.server;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Runtime other = (Runtime) obj;
		if (client == null) {
			if (other.client != null) {
				return false;
			}
		} else if (!client.equals(other.client)) {
			return false;
		}
		if (language == null) {
			if (other.language != null) {
				return false;
			}
		} else if (!language.equals(other.language)) {
			return false;
		}
		if (server == null) {
			if (other.server != null) {
				return false;
			}
		} else if (!server.equals(other.server)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Runtime [language=");
		builder.append(language);
		builder.append(", client=");
		builder.append(client);
		builder.append(", server=");
		builder.append(server);
		builder.append("]");
		return builder.toString();
	}
	
	private String toComparisonString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getLanguage());
		sb.append("-");
		sb.append(getClient());
		sb.append("-");
		sb.append(getServer());
		return sb.toString();
	}

	@Override
	public int compareTo(Runtime o) {
		String s1 = this.toComparisonString();
		String s2 = o.toComparisonString();
		return s1.compareTo(s2);
	}

}
