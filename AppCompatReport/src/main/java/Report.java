import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Report {

	private final Set<Result> results = new HashSet<Result>();

	public static void main(String[] args) throws IOException {
		Report r = new Report();
		r.addResult("../JavaDatabaseCompatibility/results/results_java_ifxjdbc.json");
		r.addResult("../JavaDatabaseCompatibility/results/results_java_db2jcc.json");
		r.addResult("../PHPDatabaseCompatibility/results/results_php_pdo_ibm.json");
		r.addResult("../PHPDatabaseCompatibility/results/results_php_pdo_informix.json");
		String html = r.generateHtml();
		FileWriter fw = new FileWriter("/tmp/file.html");
		fw.write(html);
		fw.flush();
		fw.close();
	}

	public void addResult(String path) throws IOException {
		this.addResult(new File(path));
	}

	public void addResult(File file) throws IOException {
		this.results.add(new Result(file));
	}

	public String generateHtml() {
		// order the results alphabetically by the runtime
		final Result[] orderedResults = new Result[results.size()];
		int i = 0;
		for (Result r : results) {
			orderedResults[i++] = r;
		}
		Arrays.sort(orderedResults);
		
		// determine unique runtimes (language+driver+server tuples)
		// determine union of all 'test' fields
		final Set<Runtime> runtimes = new TreeSet<Runtime>();
		final Set<String> tests = new TreeSet<String>();
		for (Result result : orderedResults) {
			runtimes.addAll(result.getRuntimes());
			tests.addAll(result.getTests());
		}
		System.out.println("runtimes: " + Arrays.toString(runtimes.toArray()));
		System.out.println("test: " + Arrays.toString(tests.toArray()));

		// group results by test across runtimes to help create rows
		final Map<String,List<TestResult>> testRuns = new TreeMap<String,List<TestResult>>();
		for (String test : tests) {
			List<TestResult> runs = new ArrayList<TestResult>();
			testRuns.put(test, runs);
			for (Result result : orderedResults) {
				runs.add(result.getTestResult(test));
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n");
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<style>\n");
		sb.append("table, th, td {\n");
		sb.append("border: 1px solid black;");
		sb.append("}\n");
		sb.append("</style>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");

		sb.append("<table border=\"1\">\n"); // style=\"width:100%\">\n");
		sb.append("<tr>\n");
		sb.append("<th></th>\n");
		for (Runtime r : runtimes) {
			sb.append("<th>");
			sb.append(r.getLanguage());
			sb.append("-");
			sb.append(r.getClient());
			sb.append("</th>\n");
		}
		sb.append("</tr>\n");


		for (Entry<String, List<TestResult>> entry : testRuns.entrySet()) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append(entry.getKey());
			sb.append("</td>\n");
			for (TestResult tr : entry.getValue()) {
				if (tr.getDetail() != null) {
					sb.append("<td title=\"");
					sb.append(tr.getDetail());
					sb.append("\">");
				} else {
					sb.append("<td>");
				}
				sb.append(tr.getResult());
				sb.append("</td>\n");				
			}
			sb.append("</tr>\n");
		}
		sb.append("</table>\n");

		sb.append("</body>\n");
		sb.append("</html>\n");

		return sb.toString();
	}

}
