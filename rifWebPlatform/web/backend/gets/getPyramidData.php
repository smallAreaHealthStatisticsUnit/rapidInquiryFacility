<?php

require('../RIF4.php');
$res = $r->getPyramidData($_GET['geolevel'],$_GET['field'],$_GET['year'],$_GET['gids']);
echo  ($res);

?>