<?php
/*
   Retrieve Result figures on click
 	RESULT FIGURES
	Area level
	Population
	Observed
	Expected
 */

require('../RIF4.php');
$gid = $_GET['gid'];
$year = $_GET['year'];
$gender = $_GET['gender'];

$res = $r->getResultFigures( $gid ); // only GID for now, in reality we'll need year and gender too.
echo json_encode($res);


?>