<?php

require('../RIF4.php');
$res = $r->getIdentifiers($_GET['geolevel'],$_GET['identifier']);
$assoc = array();
foreach($res as $row ) { 
	$assoc[$row[0]] = $row[1] ;
}

echo json_encode($assoc);

?>