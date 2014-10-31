<?php
//Retrieves  risk and confidence interavals for the specfifc result set. (ie: RR or SMR or SMR_Adj etc)
require('../RIF4.php');

$resultSet = $_GET['resultSet'];
$studyId = $_GET['studyId'];
$invId =  $_GET['investigationId'];
$year  = (isset($_GET['year']) ? $_GET['year'] : null );

//Comma delimited
$res = $r->getResultSet( $resultSet /*, $studyId, $invId, $year */);

echo $res;


?>