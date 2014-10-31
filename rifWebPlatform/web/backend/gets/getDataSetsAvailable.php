<?php

require('../RIF4.php');
$res = $r->getDataSetsAvailable($_GET['geolevel']);
echo json_encode($res);


?>