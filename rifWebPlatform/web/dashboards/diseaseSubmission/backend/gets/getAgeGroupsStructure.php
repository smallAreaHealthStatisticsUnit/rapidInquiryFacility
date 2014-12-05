<?php

require('../RIF4.php');
$res = $r->getAgeGroups($_GET['geolevel']);
echo  json_encode($res);

?>