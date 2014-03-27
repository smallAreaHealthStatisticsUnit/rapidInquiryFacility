<?php

require('../RIF4.php');
$res = $r->getFieldsAvlb($_GET['geolevel']);
echo  json_encode($res);

?>