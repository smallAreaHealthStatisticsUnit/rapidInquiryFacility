<?php

require('../RIF4.php');
$res = $r->getFieldsAsSingleArray($_GET['table']);
//echo $_GET['table'];
echo  json_encode($res);

?>