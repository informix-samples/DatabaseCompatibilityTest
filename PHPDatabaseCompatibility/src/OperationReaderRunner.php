<?php

class OperationReaderRunner {
	
	private $client;
	private $opReader;
	
	public function __construct($client, $or) {
		$this->client = $client;
		$this->opReader = $or;
	}
	
	public function run() {
		$successfulOps = 0;
		$nonFatalErrors = 0;
		$fatalErrors = 0;
		$errors = array();
		try {
			while ($this->opReader->hasNext()) {
				$op = $this->opReader->next();
				if ($op != null) {
					$op->invoke($this->client);
					if ($op->getErrorCount() == 0) {
						$successfulOps++;
					} else {
						$nonFatalErrors += $op->getErrorCount();
						$errors = array_merge($errors, $op->getErrors());
					}
				}
			}
		} catch (Exception $e) {
			$fatalErrors = 1;
			echo ("exception in operation reader runner: " . $e);
			fwrite($this->opReader->getLogFileHandle(), "exception in operation reader runner: " . $e . PHP_EOL);
		}
		
		echo("<b>");
		if ($nonFatalErrors == 0 && $fatalErrors == 0) {
			echo("<font color='green'>TEST PASSED</font><br/>");
			fwrite($this->opReader->getLogFileHandle(), "TEST PASSED" . PHP_EOL);
		} else {
			echo("<font color='red'>TEST FAILED</font><br/>");
			fwrite($this->opReader->getLogFileHandle(), "TEST FAILED" . PHP_EOL);
		}
		echo("successful operations: " . $successfulOps . "<br/>");
		echo("errors: " . ($nonFatalErrors + $fatalErrors) . "<br/>");
		echo("fatal errors: " . $fatalErrors . "<br/>");
		echo("</b>");
		fwrite($this->opReader->getLogFileHandle(), "successful operations: " . $successfulOps . PHP_EOL);
		fwrite($this->opReader->getLogFileHandle(), "errors: " . ($nonFatalErrors + $fatalErrors) . PHP_EOL);
		fwrite($this->opReader->getLogFileHandle(), "fatal errors: " . $fatalErrors . PHP_EOL);
		fwrite($this->opReader->getLogFileHandle(), "------------------------------------" . PHP_EOL . PHP_EOL);
		
		$result = array();
		$result['language'] = "php";
		$result['client'] = (DatabaseCredentials::getProtocol() == "DRDA")? "pdo_ibm" : "pdo_informix";
		$result['server'] = "Informix";
		$result['test'] = $this->opReader->getTestName();
		$result['result'] = ($nonFatalErrors == 0 && $fatalErrors == 0);
		if ($nonFatalErrors > 0 || $fatalErrors > 0) {
			$result['detail'] = $errors[0];
		}
		fwrite($this->opReader->getResultFileHandle(), json_encode($result) . PHP_EOL);
	}
}

?>