package repro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class BooleanRepro {

	public static void main(String[] args) {
		
		final String url = System.getenv("URL");
		final String user = System.getenv("USER");
		final String password = System.getenv("PASSWORD");
		if (url == null || user == null || password == null) {
			System.err.println("You must set URL, USER, and PASSWORD in the environment");
			System.exit(-1);
		}
		
		Connection connection = null;
		Statement stmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
				
		try {
			final String className;
			if (url.substring(0, 8).equals("jdbc:ids")) {
				className = "com.ibm.db2.jcc.DB2Driver";
			} else {
				className = "com.informix.jdbc.IfxDriver";
			}
			Class.forName(className);
			System.out.println("Loading driver " + className);
			
			connection = DriverManager.getConnection(url, user, password);
			stmt = connection.createStatement();
			System.out.println("creating table...");
			stmt.execute("DROP TABLE IF EXISTS test1");
			stmt.execute("CREATE TABLE test1 (id INT, b boolean)");
			
			System.out.println("inserting data...");
			pstmt1 = connection.prepareStatement("INSERT INTO test1 VALUES (?,?)");
			for (int i = 0; i < 4; i++) {
				pstmt1.setInt(1, i);
				pstmt1.setBoolean(2, (i % 2) == 0);
				pstmt1.execute();
			}
			pstmt1.close();
			
			System.out.println("running query...");
			pstmt1 = connection.prepareStatement("SELECT * FROM test1 WHERE b = ?");
			pstmt1.setBoolean(1, true);
			rs1 = pstmt1.executeQuery();
			while (rs1.next()) {
				System.out.println("Row: " + rs1.getInt(1) + ", " + rs1.getBoolean(2));
			}
			rs1.close();
			pstmt1.close();
			connection.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
