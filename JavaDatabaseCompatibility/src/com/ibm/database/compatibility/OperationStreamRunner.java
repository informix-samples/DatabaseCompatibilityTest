package com.ibm.database.compatibility;

import java.io.FileNotFoundException;
import java.io.IOException;

public class OperationStreamRunner implements Runnable {

	private final JdbcClient client;
	private final OperationStream os;

	public OperationStreamRunner(JdbcClient client, OperationStream os) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		JdbcClient client = new BasicJdbcClient();
		OperationStream os = null;
		try {
			os = new JsonOperationStream("/tmp/ops.json");
			OperationStreamRunner osr = new OperationStreamRunner(client, os);
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
