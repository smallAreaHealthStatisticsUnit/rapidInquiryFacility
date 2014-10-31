<?php

require('../RIF4.php');
$res = $r->getCentroid($_GET['table']);
echo $res[0].','.$res[1];

?>