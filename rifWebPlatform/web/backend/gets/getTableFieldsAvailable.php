<?php

require('../RIF4.php');
$res = $r->getFieldsAsSingleArray($_GET['geolevel']);
echo  json_encode($res);

?>