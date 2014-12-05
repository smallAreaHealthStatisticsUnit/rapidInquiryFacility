<?php

require('../RIF4.php');
$res = $r->getFieldsStratifiedByAgeGroup($_GET['theme']);
echo  json_encode($res);

?>