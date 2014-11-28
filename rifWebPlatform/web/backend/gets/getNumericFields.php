<?php

require('../RIF4.php');
$res = $r->getOnlyNumericFields($_GET['geolevel']);
echo  json_encode($res);

?>