<?php
//Retrieves field names only for result sets available
require('../RIF4.php');

$studyId = $_GET['studyId'];
$invId =  $_GET['investigationId'];
$year  = (isset($_GET['year']) ? $_GET['year'] : null );

//$res =  array( 'srr' , 'smr' , 'sresrr');
$res =  array( 'rr_unadj' , 'car_ssrr_unadj' , 'bym_ssrr_unadj', 'smrr_adj' );
echo json_encode($res);;

?>