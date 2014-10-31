<?php

require('../RIF4.php');
$res = $r->getFieldsAvlb($_GET['table']);
echo  json_encode($res);

?>