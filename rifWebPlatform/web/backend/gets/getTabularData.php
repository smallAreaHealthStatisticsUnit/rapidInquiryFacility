<?php

require('../RIF4.php');
$res = $r->getTabularData($_GET['gid'],$_GET['table']);
//MUST CHECK : Only one row is returned!
echo  json_encode($res);

?>