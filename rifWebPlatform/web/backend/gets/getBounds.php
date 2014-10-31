<?php

require('../RIF4.php');
$res = $r->getBounds($_GET['table'],$_GET['id']);
echo $res[0].','.$res[1].','. $res[2].','.$res[3];


?>