<?php 

	include("../src/DatabaseCredentials.php");
	
	ini_set("precision", 17);
	
	$credentials = new DatabaseCredentials("test");
	$conn = new PDO($credentials->getUrl(), $credentials->getUser(), $credentials->getPassword());
	
	$conn->exec("DROP TABLE IF EXISTS testtab");
	$conn->exec("CREATE TABLE testtab (i0 SMALLFLOAT)");
	
	$value = 84.823006;
	
	$pstmt = $conn->prepare("INSERT INTO testtab VALUES (?)");
	$pstmt->bindParam(1, $value);
	$ret = $pstmt->execute();
	if ($ret == false) {
		echo ("Failure on insert statement: " . var_export($pstmt->errorInfo(), true));
		exit(-1);
	}

	$pstmt = $conn->prepare("SELECT * FROM testtab WHERE i0 = ?");
	$pstmt->bindParam(1, $value);
	$ret = $pstmt->execute();
	if ($ret == false) {
		echo ("Failure on query: " . var_export($pstmt->errorInfo(), true));
		exit(-1);
	}
	$result = $pstmt->fetchAll(PDO::FETCH_ASSOC);
	$row = $result[0];
	
	echo "inserted value: " . $value . "<br/>";
	echo "returned value: " . $row["I0"]. "<br/>";
	echo "are they equal? " . ($value == $row["I0"]) . "<br/><br/>";

	echo "inserted value - is string? " . is_string($value) . "<br/>";
	echo "inserted value - is number? " . is_numeric($value) . "<br/>";
	echo "inserted value - is float? " . is_float($value) . "<br/>";
	echo "returned value - is string? " . is_string($row["I0"]) . "<br/>";
	echo "returned value - is number? " . is_numeric($row["I0"]) . "<br/>";
	echo "returned value - is float? " . is_float($row["I0"]) . "<br/><br/>";
	
	echo "inserted value as float: " . floatval($value). "<br/>";
	echo "returned value as float: " . floatval($row["I0"]). "<br/>";
	
?>