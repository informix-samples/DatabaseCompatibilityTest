package repro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class PstmtCursorRepro {

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
		PreparedStatement pstmt2 = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
				
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
			System.out.println("creating tables...");
			stmt.execute("DROP TABLE IF EXISTS test1");
			stmt.execute("CREATE TABLE test1 (id INT,name CHAR(20))");
			stmt.execute("DROP TABLE IF EXISTS test2");
			stmt.execute("CREATE TABLE test2 (id INT,name VARCHAR(20))");
			stmt.close();
			
			System.out.println("inserting data...");
			pstmt1 = connection.prepareStatement("INSERT INTO test1 VALUES (?,?)");
			pstmt2 = connection.prepareStatement("INSERT INTO test2 VALUES (?,?)");
			for (int i = 0; i < 4; i++) {
				pstmt1.setInt(1, i);
				pstmt1.setString(2, "a"+ i);
				pstmt1.execute();
				pstmt2.setInt(1, i);
				pstmt2.setString(2, "b" + i);
				pstmt2.execute();
			}
			pstmt1.close();
			pstmt2.close();
			
			System.out.println("preparing two queries...");
			pstmt1 = connection.prepareStatement("SELECT * FROM test1 ORDER BY id");
			pstmt2 = connection.prepareStatement("SELECT * FROM test2 ORDER BY id");
			rs1 = pstmt1.executeQuery();
			rs2 = pstmt2.executeQuery();
			getRowsFromResultSet(rs1, 1, 2);
			getRowsFromResultSet(rs2, 2, 2);
			getRowsFromResultSet(rs1, 1, null);
			getRowsFromResultSet(rs2, 2, null);
			rs1.close();
			rs2.close();
			pstmt1.close();
			pstmt2.close();
			connection.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static void getRowsFromResultSet(ResultSet rs, int resultSetId, Integer nfetch) throws Exception {
		System.out.println("Getting " + nfetch + " rows from result set " + resultSetId);
		ResultSetMetaData rsmd = rs.getMetaData();
		int fetched = 0;
		while ((nfetch == null || fetched < nfetch) && rs.next()) {
			for (int i=1; i <= rsmd.getColumnCount(); ++i) {
				System.out.print(rsmd.getColumnLabel(i) + ":");
				System.out.print(rs.getObject(i));
				System.out.print("  ");
			}
			System.out.println();
			fetched++;
		}
	}
}
