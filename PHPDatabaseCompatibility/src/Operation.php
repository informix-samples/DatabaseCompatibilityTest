<?php
include "Binding.php";

class Operation {
	
	private $operation_json = null;
	private $resource = null; // session | statement | preparedStatement | resultSet
	private $action = null; // create | execute | fetch | close | startTransaction | commitTransaction | rollbackTransaction
	private $credentialId = null;
	private $sessionId = null;
	private $statementId = null;
	private $sql = null;
	private $nfetch = null;
	private $bindings = null;
	private $expectedResults = null;
	private $errors = array();
	private $logFile = null;
	
	function __construct($json, $logFile)  {
		$this->operation_json = $json;
		$this->resource  = (isset($json['resource'])) ? $json['resource'] : null;
		$this->action  = (isset($json['action'])) ? $json['action'] : null;
		$this->credentialId  = (isset($json['credentialId'])) ? $json['credentialId'] : null;
		$this->sessionId  = (isset($json['sessionId'])) ? $json['sessionId'] : null;
		$this->statementId  = (isset($json['statementId'])) ? $json['statementId'] : null;
		$this->className  = (isset($json['className'])) ? $json['className'] : null;
		$this->sql  = (isset($json['sql'])) ? $json['sql'] : null;
		$this->nfetch  = (isset($json['nfetch'])) ? $json['nfetch'] : null;
		if (isset($json['bindings'])) {
			$bindings_input = $json['bindings'];
			$this->bindings = array();
			$i = 0;
			foreach ($bindings_input as $b) {
				$i++;
				if (is_array($b)) {
					$this->bindings[] = new Binding($b['index'], $b['value'], $b['type']);
				} else {
					$this->bindings[] = new Binding($i, $b, null);
				}
			}
		}
		$expectedResults  = (isset($json['expectedResults'])) ? $json['expectedResults'] : null;
		if ($expectedResults != null) {
			$this->expectedResults = array();
			// Convert expected to have all caps
			foreach ($expectedResults as $row) {
				$newRow = array();
				foreach ($row as $k => $v) {
					$newRow[strtoupper($k)] = $v;
				}
				$this->expectedResults[] = $newRow;
			}	
		}
		$this->logFile = $logFile;
	}
	
	public function invoke($client) {
		if ($this->resource == "credentials") {
			if($this->action == "create") {
				$credential = $client->newCredential($this->credentialId);
				$this->logMessage("creating credential: id = {$credential->getCredentialId()}, url = {$credential->getUrl()}");
			}
			if($this->action == "close") {
				/*
				 * { "resource" : "credential" ,
				* "action" : "close" ,
				* "credentialId" : "mydb"
				* }
				*/
				$credential = $client->removeCredential($this->credentialId);
				$this->logMessage("closing credential: id =  {$credential->getCredentialId()}, url =  {$credential->getUrl()}");
			}
		} else if ($this->resource == "session") {
			if ($this->action == "create") {
				$session = $client->newSession($this->sessionId);
				$this->logMessage("created new session id " . $session->getId());
			} else if ($this->action == "close") {
				$session = $client->removePHPSession($this->sessionId);
				$this->logMessage("closed session id " .  $session->getId());
			} else if ($this->action == "startTransaction") {
				$session = $client->getPHPSession($this->sessionId);
				$conn = $session->getConnection();
				$this->logMessage("starting transaction on session id " . $session->getId());
				$ret = $conn->beginTransaction();
				if (!$ret) {
					$this->logError("Start transaction failed: " . var_export($conn->errorInfo(), true));
					throw new Exception("Could not start transaction");
				}
			} else if ($this->action == "commitTransaction") {
				$session = $client->getPHPSession($this->sessionId);
				$conn = $session->getConnection();
				$this->logMessage("committing transaction on session id " . $session->getId());
				$ret = $conn->commit();
				if (!$ret) {
					$this->logError("Commit transaction failed: " . var_export($conn->errorInfo(), true));
					throw new Exception("Could not commit transaction");
				}
			} else if ($this->action == "rollbackTransaction") {
				$session = $client->getPHPSession($this->sessionId);
				$conn = $session->getConnection();
				$this->logMessage("rolling back transaction on session id " . $session->getId());
				$ret = $conn->rollback();
				if (!$ret) {
					$this->logError("Rollback transaction failed: " . var_export($conn->errorInfo(), true));
					throw new Exception("Could not rollback transaction");
				}
			} else {
				throw new Exception("Unsupported session action " . $this->action);
			}
		} else if ($this->resource == "statement") {
			if ($this->action == "execute") {
				$session = $client->getPHPSession($this->sessionId);
				$this->logMessage("executing statement ( sql: {$this->sql})");
				if ($this->expectedResults == null) {
					$session->executeStatement($this->sql);
				} else {
					$actualResults = $session->executeQuery($this->sql, $this->nfetch);
					$this->compareResults($this->expectedResults, $actualResults);
				}
			} else {
				$this->logMessage("ignoring action {$this->action} on statement. php only supports executing a statement");
			}
		} else if ($this->resource == "preparedStatement") {
			if ($this->action == "create") {
				/*-
				 * { "resource" : "preparedStatement" ,
				* "action" : "create" ,
				* "sql" : "select tabname from systables where tabid > 99" ,
				* "sessionId" : "mySession" , // optional
				* "statementId" : "abc" // optional
				* }
				*/
				$session = $client->getPHPSession($this->sessionId);
				$c = $session->getConnection();
				$pstmt = $c->prepare($this->sql);
				$session->putPreparedStatement($this->statementId, $pstmt);
				$this->logMessage("created prepared statement id " . $session->getLastPreparedStatementId());
			} else if ($this->action == "execute") {
				$session = $client->getPHPSession($this->sessionId);
				$pstmt = $session->getPreparedStatement($this->statementId);
				if ($pstmt == null) {
					$c = $session->getConnection();
					$pstmt = $c->prepare($this->sql);
					$session->putPreparedStatement($this->statementId, $pstmt);
				}
				$this->logMessage("executing prepared statement (id: {$session->getLastPreparedStatementId()})");
				if ($this->bindings != null) {
					foreach ($this->bindings as $binding) {
						$binding->bind($pstmt);
					}
				}
				if ($pstmt->execute()) {
					$actualResults = $this->fetchRows($pstmt, $this->nfetch);
					if ($this->expectedResults != null) {
						$this->compareResults($this->expectedResults, $actualResults);
					}
				} else {
					$this->logError("FAIL: prepared statement execution returned false. " . var_export($pstmt->errorInfo(), true));
				}
			} else if ($this->action == "fetch") {
				$session = $client->getPHPSession($this->sessionId);
				$pstmt = $session->getPreparedStatement($this->statementId);
				if ($pstmt == null) {
					throw new Exception("Cannot run fetch on an empty prepared statement: statement id " . $this->statementId);
				}
				$this->logMessage("running fetch on prepared statement (id: {$this->statementId})");
				$actualResults = $this->fetchRows($pstmt, $this->nfetch);
				if ($this->expectedResults != null) {
					$this->compareResults($this->expectedResults, $actualResults);
				}
			} else if ($this->action == "close") {
				$session = $client->getPHPSession($this->sessionId);
				$session->removePreparedStatement($this->statementId);
				$this->logMessage("closed prepared statement id " . $session->getLastPreparedStatementId());
			} else {
				throw new Exception("unsupported action {$this->action} on prepared statement");
			}
		} else {
			throw new Exception("Unsupported resource type " . $this->resource);
		}
	}
	
	function fetchRows($stmt, $nfetch = null) {
		$results = array();
		if ($nfetch == null) {
			$row = $stmt->fetch(PDO::FETCH_ASSOC);
			while ($row !== false) {
				$results[] = $row;
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
			}
		} else {
			for ($i = 0; $i < $nfetch; $i++) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if ($row === false) {
					break;
				}
				$results[] = $row;
			}
		}
		return $results;
	}
	
	function compareResults($expectedResult, $actualResult)  {
		$comparison = $this->doCompareResults($expectedResult, $actualResult);
		if ($comparison) {
			$this->logMessage("comparing results : success");
		} else {
			$this->logError("comparing results : FAIL. Results are not equal. Expected: " . var_export($expectedResult, true) . " Actual: " . var_export($actualResult, true));
		}
		return $comparison;
	}
	
	function doCompareResults($expectedResult, $actualResult) {
		if ($expectedResult == $actualResult) {
			// if equality comparison returns true, return true
			return true;
		}
		
		// else to a more in depth comparison
		if (sizeof($expectedResult) != sizeof($actualResult)) {
			return false;
		}
		for ($i = 0; $i < sizeof($actualResult); $i++) {
			$actualRow = $actualResult[$i];
			$expectedRow = $expectedResult[$i];
			foreach ($actualRow as $name => $value) {
				if ($value != $expectedRow[$name]) {
					// Values are not equal. But there are some special checks to try.
					// If one or both numbers are floats, compare with error tolerance
					if (is_float($value) || is_float($expectedRow[$name])) {
						$precision = ini_get('precision');
 						$errorTolerance = pow(10, floor(log10($value) - ($precision - 1)));
						$diff = abs($value - $expectedRow[$name]);
						if ($diff < $errorTolerance) {
							$this->logMessage("Float comparison: values are not strictly equal, but comparison succeeding because difference is less than error tolerance");
							$this->logMessage("Float comparison details: returned value = $value, expected value = $expectedRow[$name], precision = $precision, difference = $diff, error tolerance = $errorTolerance");
						} else {
							return false;
						}
					}  else {
						// Check if values are datetime strings
						try {
							$actualDateTime = DateTime::createFromFormat("Y-m-d H:j:s.u", $value);
							$expectedDatetime = DateTime::createFromFormat((strlen($expectedRow[$name]) == 19)? "Y-m-d H:j:s" : "Y-m-d H:j:s.u", $expectedRow[$name]);
							if ($actualDateTime && $expectedDatetime) {
								// They are valid datetimes, try to compare them again after conversion to datetime objects
								if ($actualDateTime->format("Y-m-d H:j:s.u") != $expectedDatetime->format("Y-m-d H:j:s.u")) {
									// datetime objects are not equal
									return false;
								}
							} else {
								// They are not datetimes, return false (objects are not equal)
								return false;
							}
						} catch (Exception $e) {
							// Exception trying to convert object to datetime, return false (objects are not equal)
							return false;
						}
					}
				}
			} 
		}
		return true;
	}
	
	private function logMessage($msg) {
		fwrite($this->logFile, $msg . PHP_EOL);
	}

	private function logError($errmsg) {
		$this->errors[] = $errmsg;
		$this->logMessage($errmsg);
	}
	
	function getErrors() {
		return $this->errors;
	}
	
	function getErrorCount() {
		return sizeof($this->errors);
	}
	
	function __toString() {
		return json_encode($this->operation_json);
	}
}
?>