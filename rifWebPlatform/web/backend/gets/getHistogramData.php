<?php

require('../RIF4.php');
$geolevel = $_GET['geolevel'];
$field = $_GET['field'];
$gids =  $_GET['gids'];
$res = $r->getSingleFieldValues( $geolevel, $field, $gids);
$values = array();
foreach($res as $value){
	array_push($values, $value[ $field ]); 	
}
echo json_encode($values);;

?>