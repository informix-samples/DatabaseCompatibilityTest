<?php

interface OperationWriter {
	
	public function write($op);
	public function writeComment($comment);
	public function close();
}
?>