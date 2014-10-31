<?php

require('../RIF4.php');
$res = $r->getFullExtent($_GET['table']);
echo $res[0].','.$res[1].','. $res[2].','.$res[3];


?>