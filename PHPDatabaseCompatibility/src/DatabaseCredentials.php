<?php

class DatabaseCredentials {
	private $credentialId;
	private $host;
	private $port;
	private $databaseName;
	private $server;
	private $user;
	private $password;
	private $additionalConnectionProperties;
	private $url;
	
	function __construct ($credentialId) {
		$this->credentialId = $credentialId;
		$this->getConnectionProperties();
	}
	
	public static function getProtocol() {
		if (isset($_GET['protocol'])) {
			return $_GET['protocol'];
		} else {
			return "DRDA";
		}
	}
	
	private function getConnectionProperties() {
		if (isset($_ENV["VCAP_SERVICES" ])) {
			$vcap_services = json_decode($_ENV["VCAP_SERVICES"]);
			if (DatabaseCredentials::getProtocol() == "SQLI") {
				$dbInfo = $vcap_services->{'timeseriesdatabase'}[0]->credentials;
				$this->server = "blusrv";
			} else {
				$dbInfo = $vcap_services->{'sqldb'}[0]->credentials;
			}
			$this->host = $dbInfo->host;
			$this->port = $dbInfo->port;
			$this->databaseName = $dbInfo->db;
			$this->user = $dbInfo->username;
			$this->password = $dbInfo->password;
			$this->additionalConnectionProperties = "DB_LOCALE=en_US.utf8;CLIENT_LOCALE=en_US.utf8";
		} else {
			include_once "ConnectionProperties.php";
			$this->host = HOST;
			$this->port = PORT;
			$this->databaseName = DATABASE_NAME;
			$this->server = SERVER;
			$this->user = USER;
			$this->password = PASSWORD;
			$this->additionalConnectionProperties = ADDITIONAL_CONNECTION_PROPERTIES;
		}
	}
	
	function getUrl () {
		if (DatabaseCredentials::getProtocol() == "DRDA") {
			return "ibm:DRIVER={IBM DB2 ODBC DRIVER};DATABASE={$this->databaseName};HOSTNAME={$this->host};PORT={$this->port};PROTOCOL=TCPIP;{$this->additionalConnectionProperties}";
		} else {
			return "informix:host={$this->host};service={$this->port};database={$this->databaseName};server={$this->server};protocol=onsoctcp;{$this->additionalConnectionProperties}";
		}
	}
	
	function getCredentialId() {
		return $this->credentialId;
	}
	
	function getUser() {
		return $this->user;
	}
	
	function getPassword() {
		return $this->password;
	}
}