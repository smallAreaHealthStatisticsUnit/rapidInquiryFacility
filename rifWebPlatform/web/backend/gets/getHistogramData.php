<?php

require('../RIF4.php');
$field = $_GET['field'];
$res = $r->getSingleFieldValues($_GET['geolevel'], $field);
$values = array();
foreach($res as $value){
	array_push($values, $value[ $field ]); 	
}
echo json_encode($values);;

?>