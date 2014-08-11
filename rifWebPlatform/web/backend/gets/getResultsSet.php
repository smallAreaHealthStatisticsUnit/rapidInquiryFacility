<?php
//Retrieves fields names only of result sets available
require('../RIF4.php');

$type = $_GET['type'];
$studyId = $_GET['studyId'];
$invId =  $_GET['investigationId'];
$year  = (isset($_GET['year']) ? $_GET['year'] : null );

//Comma delimited
$res = $r->getResultSet( $type, $studyId, $invId, $year );

echo $res;


?>