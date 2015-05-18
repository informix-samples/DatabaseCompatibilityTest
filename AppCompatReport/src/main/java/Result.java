import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Result implements Comparable<Result>{

	private final File file;
	private final Set<TestResult> testResults;

	public Result(File file) throws IOException {
		this.file = file;
		this.testResults = extractTestResults(file);
	}

	static Set<TestResult> extractTestResults(File file) throws IOException {
		final Set<TestResult> testResults = new HashSet<TestResult>();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				TestResult tr = TestResult.fromJson(line);
				testResults.add(tr);
			}
		} catch (FileNotFoundException e) {
			System.out.println(file.getAbsolutePath());
			throw e;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					// do nothing
				}
			}
		}
		return testResults;
	}

	public File getFile() {
		return this.file;
	}

	public Set<Runtime> getRuntimes() {
		HashSet<Runtime> runtimes = new HashSet<Runtime>();
		for (TestResult tr : testResults) {
			runtimes.add(tr.getRuntime());
		}
		return runtimes;
	}

	public Set<TestResult> getTestResults() {
		return Collections.unmodifiableSet(testResults);
	}
	
	public TestResult getTestResult(String test) {
		if (test == null) {
			throw new NullPointerException("test name must not be null");
		}
		for (TestResult tr : testResults) {
			if (test.equals(tr.getTest())) {
				return tr;
			}
		}
		return null;
	}
	
	public Set<String> getTests() {
		Set<String> tests = new HashSet<String>();
		for (TestResult tr : testResults) {
			tests.add(tr.getTest());
		}
		return tests;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
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
		Result other = (Result) obj;
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		} else if (!file.equals(other.file)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Result [file=");
		builder.append(file);
		builder.append(", testResults=");
		builder.append(testResults);
		builder.append("]");
		return builder.toString();
	}
	
	private String toComparisonString() {
		Set<Runtime> runtimes = getRuntimes();
		if (runtimes.size() == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			boolean afterFirst = false;
			for (Runtime r : runtimes) {
				if (afterFirst) {
					sb.append("_");
				}
				sb.append(r.getLanguage());
				sb.append("-");
				sb.append(r.getClient());
				afterFirst = true;
			}
			return sb.toString();
		}
	}

	@Override
	public int compareTo(Result o) {
		String s1 = this.toComparisonString();
		String s2 = o.toComparisonString();
		return s1.compareTo(s2);
	}

}
