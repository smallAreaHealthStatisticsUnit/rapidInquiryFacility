<?php
$r = RIF4::Instance();
/**
 * Singleton class
 *
 */ 
 class RIF4
{
	private static $inst ;
	private static $dbh=null;
	protected $session;
    /**
     * Call this method to get singleton
     *
     * @return UserFactory
     */
    public static function Instance()
    {
        if (!self::$inst) {
            self::$inst = new RIF4();
        }
        return self::$inst;
    }
	
	/**
     * Private ctor so nobody else can instance it
     *
     */
    private function __construct()
    {
		self::connect();	
    }

	public function __destruct() {
        $_SESSION = $this->session;
    }
	
	
	protected function  connect () {
     	try{
				self :: $dbh = new PDO("pgsql:dbname=rif4_canvas;host=localhost;user=postgres;password=Imperial1234", array( PDO::ATTR_PERSISTENT => true )); 
		    //self :: $dbh = new PDO("pgsql:dbname=rif4;host=localhost;user=postgres;password=fava" , array( PDO::ATTR_PERSISTENT => true )); 
			self :: $dbh-> setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION );
		}catch(PDOException $pe){
		 	die('Connection error: ' .$pe->getMessage());
		}
	}
	
	public function exec($sql){
		self :: $dbh->exec($sql);
	}
	
	/*public function getTiles($p){
		
		try{
			self :: $dbh->beginTransaction();
			
			$xtile = $p['tiley'];
			$ytile = $p['tilex'];
			$zoom = $p['zoom'];
			$slctField = $p['field']; 
			$geoLvl = $p['geolevel']; 
		
			
		    $n = pow(2, $zoom);
			$lon_1 = $xtile / $n * 360.0 - 180.0;
			$lat_1 = rad2deg(atan(sinh(pi() * (1 - 2 * $ytile / $n))));
			$lon_2 = ($xtile+1) / $n * 360.0 - 180.0;
			$lat_2 = rad2deg(atan(sinh(pi() * (1 - 2 * ($ytile+1) / $n))));
			
			$sql = "select gid ,  $slctField  as fieldScltd , st_asGeoJSON(geom,3,0) as geom 
				     from $geoLvl  x 
					  where x.geom &&
				       st_MakeEnvelope (  $lon_1, $lat_1, $lon_2 , $lat_2 , 4326 )";	
			echo $sql;		   
			$hndl = self::$dbh -> query($sql);		
			$stmt = $hndl -> fetchAll(PDO::FETCH_ASSOC);
			self :: $dbh->commit();
			return $stmt;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		    die( $pe->getMessage());
		}	
		
	}*/
	public function getTiles($p){
		
		try{
			self :: $dbh->beginTransaction();
			
			$geolevel = $p['geolevel'];
			$zoom = $p['zoom'];
			$tileId = $p['tileId'];
			$field = $p['field'];
			$x = $p['x'];
			$x2 = $p['x2'];
			$y = $p['y'];
			$y2 = $p['y2'];
			
			$sql = "select gid /*, $field as fieldScltd*/ , st_asGeoJSON(geom,3,0) as geom 
				     from $geolevel  x 
					  where x.geom &&
				       st_MakeEnvelope ( $x, $y , $x2, $y2 , 4326 )";	
		   
			$hndl = self::$dbh -> query($sql);		
			$stmt = $hndl -> fetchAll(PDO::FETCH_ASSOC);
			self :: $dbh->commit();
			return $stmt;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
		
	}
	
	public function getBounds($table, $gid){
		try{
		    $sql = "with a as 
				    (select st_extent(geom) as g from $table where gid = " . $gid . ")
				      select st_ymax(g),st_xmax(g),st_ymin(g),st_xmin(g)  from a";	
			
				
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());		
			$res = $hndl -> fetch(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getFullExtent($table){
		try{
		    $sql = "with a as 
				    (select st_extent(geom) as g from $table )
				      select st_ymax(g),st_xmax(g),st_ymin(g),st_xmin(g)  from a";	
					   
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());		
			$res = $hndl -> fetch(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getTabularData($id,$table){
		try{
		    $sql = "select gid,stward03,st_asText(geom)as geom from $table where gid=? " ;	
					   
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array($id));	
			$res = $hndl -> fetch(PDO::FETCH_ASSOC);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getGeoLvlAvlb( ){
		try{
		    $sql = "select f_table_name from geometry_columns " ;		   
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getIdentifiers( $table, $identifier ){
		try{
		    $sql = "select distinct gid,$identifier  from $table  " ;		   
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getGeometryCol( $table ){
		try{
		    $sql = "select f_geometry_column from geometry_columns where  f_table_name ='". $table ."'" ;		   
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetch(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res[0];
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getOnlyNumericFields( $table ){
		try{
		    $geom = $this->getGeometryCol( $table );
		    $sql = "select column_name from information_schema.columns where table_name='". $table ."'
         			and  NOT (column_name = ANY ( ARRAY['".$geom."' , 'gid'])) and 
					(data_type = ANY ( ARRAY ['smallint' ,  'integer' , 'bigint', 'decimal', 'numeric', 'real', 'double precision',
					 'smallserial', 'serial', 'bigserial' ]))" ;
			
			//echo $sql;	
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	
	}
	
	public function getFieldsAvlb( $table ){
		try{
		    $geom = $this->getGeometryCol( $table );
		    $sql = "select column_name from information_schema.columns where table_name='". $table ."'
         			and  NOT (column_name = ANY ( ARRAY['".$geom."' , 'gid']))  " ;
					
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getFieldsAsSingleArray( $table ){
		try{
		    $geom = $this->getGeometryCol( $table );
		    $sql = "select column_name from information_schema.columns where table_name='". $table ."'
					and  NOT column_name = ANY ( ARRAY['".$geom."' , 'gid', 'id'])";
					
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_NUM);
			self :: $dbh->commit();
			
			$length = count($res) - 1 ;
			$myFields = array();
			while($length >= 0){
				array_push($myFields, $res[$length--][0] );
			};
			
			return $myFields;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getCentroid($table){
		try{
		    $sql = "with a as 
					(select st_centroid(st_collect(geom)) as g from $table)
					  select st_x(g),st_y(g) from a" ;	
					   
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetch(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getTabularData($table, $fields){
		try{
			
			if ( !isset( $fields ) ){
				$fields = $this->getFieldsAsSingleArray( $table );
			}
			
			$cols = implode( ",", $fields );
			 
		    /*Need to create a uniqque id by using the GID which is a reference to the area plus a sequence identifying the stratum
			* This is because an area have multiple rows (Many age groups, gender etc..) 
			*/
			
			$sql = "drop sequence  if exists uniqueids;create temp sequence uniqueids";
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());
			
			$sql = "select gid||'_'||cast( nextval('uniqueids') as varchar) as id, $cols from $table ;";
			
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_ASSOC);
			self :: $dbh->commit();
		   /*
			* Return fields and data separately
			* Data comes in a standard array, avoiding to repeat column names 
			*/
			return array( $fields , $res );	
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
    
}

?>