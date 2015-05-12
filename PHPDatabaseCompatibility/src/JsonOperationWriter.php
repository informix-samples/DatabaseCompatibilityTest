<?php

include "OperationWriter.php";

class JsonOperationWriter implements OperationWriter {

	private $fileHandle;
	
	function __construct($pathToFile) {
		$this->fileHandle = fopen($pathToFile, "w");
	}
	
	public function write($op) {
		fwrite($this->fieHandle, $op . "\n");
	}
	
	public function writeComment($comment) {
		$lines = $comment->split("\n");
		foreach ($lines as $line) {
			fwrite($this->fieHandle, "#" . $line . "\n");
		}
	}

	public function close() {
		fclose($this->fileHandle);
	}
}

?>