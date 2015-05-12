<?php

include "OperationReader.php";
include "Operation.php";

class JsonOperationReader implements OperationReader {
	
	private $testName;
	private $fileHandle;
	private $logFileHandle;
	private $resultFileHandle;
	private $nextLine = null;
	
	function __construct($inputDir, $testFile, $logFileHandle, $resultFileHandle) {
		$this->testName = $testFile;
		$testFilePath = $inputDir . "/" . $testFile;
		$this->fileHandle = fopen($testFilePath, "r");
		if ($this->fileHandle == null || empty($this->fileHandle)) {
			throw new Exception("could not open test file: $testFilePath");
		}
		$this->logFileHandle = $logFileHandle;
		$this->resultFileHandle = $resultFileHandle;
	}
	
	public function getTestName() {
		return $this->testName;
	}

	public function hasNext() {
		return !feof($this->fileHandle);
	}
	
	public function next() {
		$line = fgets($this->fileHandle);
		if ($line == null || $line == "") {
			return null;
		}
		if (substr($line, 0, 1) == "#") {
			// ignore comments
			if ($this->hasNext()) {
				return $this->next();
			} else { 
				return null;
			}
		} else {
			//echo "input from file: $line <br/>";
			//echo var_dump(json_decode($line));
			return new Operation(json_decode($line, true), $this->logFileHandle);
		}
	}

	public function getLogFileHandle() {
		return $this->logFileHandle;
	}
	
	public function getResultFileHandle() {
		return $this->resultFileHandle;
	}
	
	public function close() {
		fclose($this->fileHandle);
	}

}
?>