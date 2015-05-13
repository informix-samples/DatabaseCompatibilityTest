<?php

include "SQLDataType.php";

class Binding {
	
	private $index = null;
	private $value;
	private $typeOfValue;
	
	function __construct($index, $value, $typeOfValue) {
		$this->index = $index;
		$this->value = $value;
		$this->typeOfValue = $typeOfValue;
	}
	public function getIndex() {
		return $this->index;
	}
	protected function setIndex($index) {
		$this->index = $index;
	}
	public function getValue() {
		return $this->value;
	}
	
	public function getTypeOfValue() {
		if ($this->typeOfValue == null) {
			return SqlDataType::OBJECT;
		} else {
			return $this->typeOfValue;
		}
	}
	
	public function bind($ps) {
		switch($this->typeOfValue) {
			case SQLDataType::BOOLEAN:
				$ps->bindParam($this->index, $this->value, PDO::PARAM_BOOL);
				break;
			case SQLDataType::BIGINT:
			case SQLDataType::INT:
			case SQLDataType::INT8:
				$ps->bindParam($this->index, $this->value, PDO::PARAM_INT);
				break;
			case SQLDataType::CHAR:
			case SQLDataType::VARCHAR:
			case SQLDataType::LVARCHAR:
			case SQLDataType::NCHAR:
				$ps->bindParam($this->index, $this->value, PDO::PARAM_STR);
				break;
			case SQLDataType::DATE:
				if (is_numeric($this->value)) {
					// PHP doesn't have a PDO::PARAM_ constant for date. So convert date to string
					$date = new DateTime();
					$date->setTimestamp($this->value / 1000);
					$date_str = $date->format("Y-m-d");
					$ps->bindParam($this->index, $date_str);
				} else {
					$ps->bindParam($this->index, $this->value);
				}
				break;
			case SQLDataType::DATETIME:
				if (is_numeric($this->value)) {
					// PHP doesn't have a PDO::PARAM_ constant for datetime. So convert date to string
					$date = new DateTime();
					$date->setTimestamp($this->value / 1000);
					$date_str = $date->format("Y-m-d H:j:s");
					$date_str = $date_str . "." . ($this->value % 1000);
					$ps->bindParam($this->index, $date_str);
				} else {
					$ps->bindParam($this->index, $this->value);
				}
				break;	
			default:
				$ps->bindParam($this->index, $this->value);
				break;
		}
	}
	
}
?>