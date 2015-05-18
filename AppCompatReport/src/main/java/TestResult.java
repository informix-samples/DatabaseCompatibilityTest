import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestResult {

	private String language;
	private String client;
	private String server;
	private String test;
	private String result;
	private String detail;

	public Runtime getRuntime() {
		return new Runtime(language, client, server);
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
		return this.test;
	}
	
	public String getResult() {
		return this.result;
	}

	public String getDetail() {
		return this.detail;
	}

	public static TestResult fromJson(String jsonTestResult) {
		Gson gson = new GsonBuilder().create();
		TestResult tr = gson.fromJson(jsonTestResult, TestResult.class);
		return tr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result + ((detail == null) ? 0 : detail.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		result = prime * result + ((test == null) ? 0 : test.hashCode());
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
		TestResult other = (TestResult) obj;
		if (client == null) {
			if (other.client != null) {
				return false;
			}
		} else if (!client.equals(other.client)) {
			return false;
		}
		if (detail == null) {
			if (other.detail != null) {
				return false;
			}
		} else if (!detail.equals(other.detail)) {
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
		if (test == null) {
			if (other.test != null) {
				return false;
			}
		} else if (!test.equals(other.test)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestResult [language=");
		builder.append(language);
		builder.append(", client=");
		builder.append(client);
		builder.append(", server=");
		builder.append(server);
		builder.append(", test=");
		builder.append(test);
		builder.append(", detail=");
		builder.append(detail);
		builder.append("]");
		return builder.toString();
	}

}
