<?php

interface OperationReader {

	public function hasNext();
	public function next();
	public function close();
	public function getLogFileHandle();
	public function getResultFileHandle();
	public function getTestName();
}
?>