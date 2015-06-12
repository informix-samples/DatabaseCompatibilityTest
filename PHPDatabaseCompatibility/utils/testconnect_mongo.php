<?php

	echo "Testing connection to TimeseriesDB with MongoDB PHP driver<br/>";
	echo "----------------------------------" . "<br/>";

	$value = getenv ( "VCAP_SERVICES" ); 
	$vcap_services = json_decode($_ENV["VCAP_SERVICES" ]); 
	$dbInfo = $vcap_services->{'timeseriesdatabase'}[0]->credentials; 
	$url = $dbInfo->json_url;
	$database = $dbInfo->db;
	echo "url = " . $url . "<br/>";
	echo "----------------------------------" . "<br/>";
	
	try {
		$mongoClient = new MongoClient($url);
		$db = $mongoClient->$database;
		$table = $db->systables;
		echo "query systables...<br/>";
		$query = array("tabid" => array('$gt' => 99));
		$cursor = $table->find($query);
		foreach ($cursor as $document) {
	    	echo $document["tabname"] . "<br/>";
		}
	} catch (Exception $e) {
		echo "exception: " . $e->getMessage() . "<br/>";
	}
	echo "----------------------------------" . "<br/>";
	echo "done<br/>";
?>