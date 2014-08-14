<?php
//Retrieves  risk vaules ( no confidence intervals) for each area id in study given a result set  .
require('../RIF4.php');

$type = $_GET['type'];
$studyId = $_GET['studyId'];
$invId =  $_GET['investigationId'];

//Comma delimited
$res = $r->getRiskResults( $type, $studyId, $invId, $year );

echo $res;


?>