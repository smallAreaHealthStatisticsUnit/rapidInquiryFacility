<?php
require('../RIF4.php');
//check the GET parameters are fine!
$res = $r->getTabularData($_GET['geolevel'],$_GET['fields']);
echo  json_encode($res);
?>