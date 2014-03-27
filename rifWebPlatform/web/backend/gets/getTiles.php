<?php
/*
 * $_GET Array stores all parameters passed asyncronously by javascript. 
 *  x => nw corner of bounding box
 *  Y => nw corner of bounding box
 *  x2 => se corner of bounding box
 *	y2 => se corner of bounding box
 *	zoom => map zoom level
 *	tileId => Tile Id :http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
 *	geolevel => Name of geolevel (spatial table) selected (i.e: wards, district, neverland)
 *	field => selection field selected by user
*/

$zoom = $_GET['zoom'];
$tileId = $_GET['tileId'];
$geolevel = $_GET['geolevel']; 

/*
 * Check if dir corresponding to specific geolevel exists
 */
$geoDir = "../cachedTiles/$geolevel";
if(!is_dir($geoDir)){
   mkdir($geoDir);
}
/*
 * Check if topojson file exists 
 * - True: get the file and return 
 */   
$geo = "$geoDir/$tileId".'_'."$zoom.json";
$topo = "$geoDir/$tileId".'_'."$zoom.topojson";
if(file_exists($topo)){
    echo "x";
	echo "  ________";
    echo file_get_contents($topo);
	die();
}
/*
 * Create a Geojson file, to be replaced by PETER's PSQL function!
 *  - The Id object has to exist outside the properties
 *  - Properties have a single object representing the user's selected field (i.e area name | code | whatever)
 */
require('../RIF4.php');
$res = $r->getTiles($_GET);
$geoJson='{ "type": "FeatureCollection","features": [ ';
$i=0;$length=count($res);	
foreach ($res as $row){
	$geoJson_='{"type": "Feature","id": "' . $row['gid'] . '",  "geometry":';//gid outside the properties  object
	$geoJson_.=$row['geom']; //actual coordinates and geometry type definition
	$geoJson_.= /*',"properties": {"field": "'. $row['fieldscltd'] .'"}*/ '}';
	if($i< $length -1 ){
		$geoJson_.=',';
		$i++;
	}
	$geoJson.= $geoJson_;
}	
$geoJson.=']} ';
/*
 * Some queries may not return any geographical areas
 */
if( $length > 0 ){   
   file_put_contents($geo, $geoJson);
   //see https://github.com/mbostock/topojson/wiki/Command-Line-Reference#quantization
   $quantization = ($zoom < 7) ? 500 :
   ($zoom < 8) ? 5000 :
   ($zoom < 9 ) ? 10000 :
   ($zoom < 10 ) ? 100000 : 1000000;
   /* 
    The following command ouput unwanted verbose messages, hence the need of  a separator ________
    to be able then to split in Javascriot and get the output of file_get_contents($topo) only
   */
   shell_exec ("topojson  -o $topo -q $quantization -s 0  $geo"); 
   // Ugly, very ugly. 
   echo "________";
   echo file_get_contents($topo);
   die();
}

echo -1;
?>