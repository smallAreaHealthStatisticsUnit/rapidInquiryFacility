<?php
//Retrieves field names only for result sets available
require('../RIF4.php');

$studyId = $_GET['studyId'];
$invId =  $_GET['investigationId'];
$year  = (isset($_GET['year']) ? $_GET['year'] : null );

$res =  array( 'srr' , 'smr' , 'sresrr');
echo json_encode($res);;

?>