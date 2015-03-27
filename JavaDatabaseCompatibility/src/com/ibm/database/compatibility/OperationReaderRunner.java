package com.ibm.database.compatibility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class OperationReaderRunner implements Runnable {

	private final JdbcClient client;
	private final OperationReader os;

	public OperationReaderRunner(JdbcClient client, OperationReader os) {
		this.client = client;
		this.os = os;
	}

	@Override
	public void run() {
		try {
			while (os.hasNext()) {
				Operation op = os.next();
				if (op != null) {
					//System.out.println(op.toString());
					op.invoke(client);
				}
			}
		} catch (SQLException e) {
			System.err.println(e.getErrorCode() + ":" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		JdbcClient client = new BasicJdbcClient();
		JsonOperationReader os = null;
		try {
			os = new JsonOperationReader("/tmp/ops.json");
			OperationReaderRunner osr = new OperationReaderRunner(client, os);
			osr.run();
		} catch (FileNotFoundException e) {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e1) {
				}
			}
			e.printStackTrace();
		}
	}

}
