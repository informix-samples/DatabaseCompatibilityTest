<?php

include "PHPSession.php";

class BasicPHPSession implements PHPSession {
	
	const DEFAULT_ID = "default";
	private $client;
	private $sessionId;
	private $credentialsId;
	private $connection;
// 	private $lastStatementId = self::DEFAULT_ID;
// 	private $statements = array();
	private $lastPreparedStatementId = self::DEFAULT_ID;
	private $preparedStatements = array();
	private $lastResultSetId = self::DEFAULT_ID;
	private $resultSets = array();
	
	function __construct($client, $sessionId, $credentialsId) {
		$this->client = $client;
		$this->sessionId = $sessionId;
		$this->credentialsId = $credentialsId;
	}
	
	public function getConnection() {
		if ($this->connection == null) {
			$credentials = $this->client->getDatabaseCredential($this->credentialsId);
			$this->connection = $this->client->newConnection($credentials->getUrl(), $credentials->getUser(), $credentials->getPassword());
		}
		return $this->connection;
	}

	public function getId() {
		return $this->sessionId;
	}
	
	public function executeStatement($sql) {
		$c = $this->getConnection();
		return $c->exec ($sql);
	}

	public function executeQuery($sql, $nfetch = null) {
		$c = $this->getConnection();
		$stmt = $c->query($sql);
		if ($nfetch == null) {
			return $stmt->fetchAll(PDO::FETCH_ASSOC);
		} else {
			$result = array();
			for ($i = 0; $i < neftch; $i++) {
				$row = $stmt->fetch(PDO::FETCH_ASSOC);
				if ($row === false) {
					break;
				}
				$result[] = $row;
			}
			return $result;
		}
	}
	
// 	public function createStatement($id) {
// 		if ($id == null) {
// 			$id = $this->lastStatementId;
// 		} else {
// 			$this->lastStatementId = $id;
// 		}
// 		$c = $this->getConnection();
// 		$stmt = $c->createStatement();
// 		$this->statements[$id] = $stmt;
// 		return $stmt;
// 	}
	
// 	public function getStatement($id) {
// 		if ($id == null) {
// 			$id = $this->lastStatementId;
// 		} else {
// 			$this->lastStatementId = $id;
// 		}
// 		if (!isset($this->statements[$id])) {
// 			return $this->createStatement($id);
// 		}
// 		return $this->statements[$id];
// 	}
	
// 	public function putStatement($id, $s) {
// 		if ($id == null) {
// 			$id = $this->lastStatementId;
// 		} else {
// 			$this->lastStatementId = $id;
// 		}
// 		return $this->statements[$id] = $s;
// 	}
	
// 	public function removeStatement($id) {
// 		if ($id == null) {
// 			$id = $this->lastStatementId;
// 		} else {
// 			$this->lastStatementId = $id;
// 		}
// 		$s = $this->statements[$id];
// 		unset ($this->statements[$id]);
// 		if ($s != null) {
// 			try {
// 				$s->close();
// 			} catch (Exception $e) {
// 				// do nothing
// 			}
// 		}
// 		return $s;
// 	}
	
	public function getPreparedStatement($id) {
		if ($id == null) {
			$id = $this->lastPreparedStatementId;
		} else {
			$this->lastPreparedStatementId = $id;
		}
		return $this->preparedStatements[$id];
	}
	
	public function putPreparedStatement($id, $ps) {
		if ($id == null) {
			$id = $this->lastPreparedStatementId;
		} else {
			$this->lastPreparedStatementId = $id;
		}
		return $this->preparedStatements[$id] =  $ps;
	}
	
	public function removePreparedStatement($id) {
		if ($id == null) {
			$id = $this->lastPreparedStatementId;
		} else {
			$this->lastPreparedStatementId = $id;
		}
		$ps = $this->preparedStatements[$id];
		unset($this->preparedStatements[$id]);
		// PHP doesn't have close function on prepared statement
// 		if ($ps != null) {
// 			try {
// 				$ps->close();
// 			} catch (Exception $e) {
// 				// do nothing
// 			}
// 		}
		return $ps;
	}

	public function getResultSet($id) {
		if ($id == null) {
			$id = $this->lastResultSetId;
		} else {
			$this->lastResultSetId = $id;
		}
		return $this->resultSets[$id];
	}

	public function putResultSet($id, $resultSet) {
		if ($id == null) {
			$id = $this->lastResultSetId;
		} else {
			$this->lastResultSetId = $id;
		}
		return $this->resultSets[$id] = $resultSet;
	}

	public function removeResultSet($id) {
		if ($id == null) {
			$id = $this->lastResultSetId;
		} else {
			$this->lastResultSetId = $id;
		}
		$rs = $this->resultSets[$id];
		unset($this->resultsSets[$id]);
		if ($rs != null) {
			try {
				$rs.close();
			} catch (Exception $e) {
				// do nothing
			}
		}
		return $rs;
	}

	public function close(){
		$this->connection.close();
	}
	
// 	public function getLastStatementId() {
// 		return $this->lastStatementId;
// 	}

	public function getLastPreparedStatementId() {
		return $this->lastPreparedStatementId;
	}

	public function getLastResultSetId() {
		return $this->lastResultSetId;
	}
}