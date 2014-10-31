<?php

require('../RIF4.php');
$res = $r->getGeoLvlAvlb();
echo  json_encode($res);

?>