<?php

include "BasicPHPSession.php";
include "DatabaseCredentials.php";

class BasicPHPClient {
	private $credentials = array();
	private $sessions = array();
	private $lastCredentialId = "default";
	private $lastSessionId = "default";
	
	public function newSession($id) {
		if ($id != null) {
			$this->lastSessionId = $id;
		}
		$session = new BasicPHPSession($this, $this->lastSessionId, $this->lastCredentialId);
		$this->sessions[$this->lastSessionId] = $session;
		return $session;
	}

	public function getPHPSession($id) {
		if ($id == null) {
			return $this->sessions[$this->lastSessionId];
		} else {
			return $this->sessions[$id];
		}
	}

	public function putPHPSession($id, $session) {
		$this->sessions[$id] = $session;
	}
	
	public function removePHPSession($id) {
		$ses = $this->sessions[$id];
		$ses->close();
		unset($this->sessions[$id]);
		return $ses;
	}

	public function getPHPSessions() {
		return $this->sessions;
	}
	
	public function newConnection($url, $user, $password) {
		return new PDO($url, $user, $password);
	}
	
	public function newCredential($credentialId) {
		if($credentialId != null) {
			$this->lastCredentialId = $credentialId;
		}
		$credential = new DatabaseCredentials($credentialId);
		$this->credentials[$this->lastCredentialId] = $credential;
		return $credential;
	}
	
	public function removeCredential($credentialId) {
		$dc = $this->credentials[$credentialId];
		unset($this->credentials[$credentialId]);
		return $dc;
	}

	public function getDatabaseCredential($credentialId) {
		if($credentialId != null) {
			return $this->credentials[$this->lastCredentialId];
		} else {
			return $this->credentials[$credentialId];
		}
	}
}

?>