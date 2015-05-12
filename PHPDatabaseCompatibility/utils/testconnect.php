<?php

	echo "Testing connection to SQLDB <br/>";
	echo "----------------------------------" . "<br/>";

	$key = "VCAP_SERVICES"; 
	$value = getenv ( $key ); 
	echo $key . ":" . $value . "<br/>"; 
	echo "----------------------------------" . "<br/>"; 
	$vcap_services = json_decode($_ENV["VCAP_SERVICES" ]); 
	$dbInfo = $vcap_services->{'sqldb'}[0]->credentials; 
	
	$conn_string = "DRIVER={IBM DB2 ODBC DRIVER};DATABASE=$dbInfo->db;" .
	"HOSTNAME=$dbInfo->host;PORT=$dbInfo->port;PROTOCOL=TCPIP;UID=$dbInfo->username;PWD=$dbInfo->password;";
	echo $conn_string . "<br/>";
	echo "----------------------------------" . "<br/>";
	
	$conn = db2_connect($conn_string,'','');
	if ($conn) {
		echo "connection successful<br/>";
		$sql = "select NAME, USERID from PEOPLE";
		echo "executing query: $sql <br/>";
		$stmt = db2_prepare($conn, $sql);
		if ($stmt) {
			$result = db2_execute($stmt);
			if (!$result) {
				echo "exec errormsg: " .db2_stmt_errormsg($stmt) . "<br/>";
			}
			while ($row = db2_fetch_array($stmt)) {
				print "$row[0] | $row[1] <br/>";
			}
		} else {
			echo "exec errormsg: " . db2_stmt_errormsg($stmt) . "<br/>";
		}
		db2_close($conn);
	} else {
		echo "failed ".db2_conn_errormsg() . "<br/>";
	}
	echo "done<br/>";
?>