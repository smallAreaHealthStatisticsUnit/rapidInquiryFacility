<?php

require('../RIF4.php');

$type = $_GET['type'];
$studyId = $_GET['studyId'];
$invId =  $_GET['investigationId'];
$year  = (isset($_GET['year']) ? $_GET['year'] : null );

/*$res = $r->getResultSet( $type, $studyId, $invId, $year );
$values = array();
foreach($res as $value){
	array_push($values, $value[ $field ]); 	
}*/

$res =  array('Rates', 'RR','RR_adj','Smr_BYM','Smr_CAR', 'Smr_HET' );
echo json_encode($res);;

?>