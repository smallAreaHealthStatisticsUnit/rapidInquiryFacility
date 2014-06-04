<?php
require('../RIF4.php');
//check the GET parameters are fine!
$res = $r->getTabularData($_GET['geolevel'],$_GET['fields'],$_GET['from'],$_GET['to'], $_GET['gids']);
echo  json_encode($res);
?>