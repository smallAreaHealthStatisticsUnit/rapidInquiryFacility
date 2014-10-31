<?php

require('../RIF4.php');
$res = $r->getIdentifiers($_GET['geolevel'],$_GET['identifier']);
$assoc = array();
foreach($res as $row ) {
	$id = "g" . $row[0];// SVG path IDs must start with a letter, g has been arbitrarily adopted
	$assoc[$id] = $row[1] ;
}

echo json_encode($assoc);

?>