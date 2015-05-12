<?php

	include "BasicPHPClient.php";
	include "JsonOperationReader.php";
	include "OperationReaderRunner.php";
	
	$input_dir = "../tests";
	$output_dir = "../results";
	
	date_default_timezone_set("UTC");

	echo "main: starting compatibility test <br/>";
	$client = new BasicPHPClient();
	$or = null;
	try {
		// Get tests to run
		$files = array();
		if (!empty($_GET) && isset($_GET['test'])) {
			// Run single test
			$files[] = $_GET['test'];
		} else {
			// Run all tests
			$dir = opendir($input_dir);
			while (false !== ($entry = readdir($dir))) {
				if (substr($entry, -5) == ".json") {
					$files[] = $entry;
				}
			}
			sort($files);
		}
		
		// Get log file handles
		$driver = (DatabaseCredentials::getProtocol() == "DRDA")? "pdo_ibm" : "pdo_informix";
		$resultFilePrefix =  $output_dir . "/" . "results_php_" . $driver . ".debug";
		$logFilePath = $resultFilePrefix . ".log";
		$logFileHandle = fopen($logFilePath, "w");
		if ($logFileHandle == null || empty($logFileHandle)) {
			throw new Exception("could not open log file: $logFilePath");
		}
		$resultFilePath = $resultFilePrefix . ".json";
		$resultFileHandle = fopen($resultFilePath, "w");
		if ($resultFileHandle == null || empty($resultFileHandle)) {
			throw new Exception("could not open result file: $resultFilePath");
		}
		
		// Run each test
		foreach($files as $file) {
			echo "<br/> test case: " . $file . "<br/>";
			fwrite($logFileHandle, "starting test: " . $file . PHP_EOL);
			$or = new JsonOperationReader($input_dir, $file, $logFileHandle, $resultFileHandle);
			$osr = new OperationReaderRunner($client, $or);
			$osr->run();
		}
		
		fclose($logFileHandle);
		fclose($resultFileHandle);
	} catch (Exception $e) {
		echo "main: exception $e <br/>";
	}
	echo "<br/>main: done<br/>";
	

?>