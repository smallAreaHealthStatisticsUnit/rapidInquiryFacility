<?php
require('../RIF4.php');
//check the GET parameters are fine!
$res = $r->getTableRows($_GET['geolevel'],$_GET['fields'],$_GET['gids']);

echo  json_encode($res);
?>