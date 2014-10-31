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
			//self :: $dbh = new PDO("pgsql:dbname=sahsuland;host=wpea-pch;user=federicof;password=se11afield2012", array( PDO::ATTR_PERSISTENT => true )); 
			self :: $dbh-> setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION );
		}catch(PDOException $pe){
		 	die('Connection error: ' .$pe->getMessage());
		}
	}
	
	public function exec($sql){
		self :: $dbh->exec($sql);
	}
	
	public function getTiles($p){
		/*
		  Parameters:
			p['y'];
			p['x'];
			p['y2'];
			p['x2'];
			p['zoom'];
			p['tileId'];
			p['field']; 
		*/
		try{
			self :: $dbh->beginTransaction();
			/*$sql = "with 
			     a as (
				   select st_MakeEnvelope (  ".$p['x']." , ".$p['y']." , ".$p['x2']." , ".$p['y2']." , 4326 ) as box
				 ),b as (
				   select stward03 
				    from ew_wards_4326 x 
					 where st_DWithin( 
					  st_makeLine(
					   st_setSRID(
					    st_makePoint(".$p['x']." , ".$p['y']." ),4326),
					   st_setSRID(	
						st_makePoint(".$p['x']." , ".$p['y2']." ),4326)) , x.geom,0)
				 ),c as (
				   select stward03 
				    from ew_wards_4326 x 
					 where st_DWithin( 
					  st_makeLine(
					   st_setSRID(
					    st_makePoint(".$p['x']." , ".$p['y']." ),4326),
					   st_setSRID(
						st_makePoint(".$p['x2']." , ".$p['y']." ),4326)) , x.geom,0)
				 ),d as(
				   select stward03 from ew_wards_4326 x , a 
				    where st_contains(a.box,x.geom) 
				)
				select gid , stward03 as fieldScltd , st_asGeoJSON(geom,3,0) as geom 
				 from ew_wards_4326 x 
				  where stward03 in ( select stward03 from d) 
				  or stward03 in ( select stward03 from b)
				  or stward03 in ( select stward03 from c where stward03 not in (select stward03 from b))";
			*/
			
			$sql = "select gid , ". $p['field'] . " as fieldScltd , st_asGeoJSON(geom,3,0) as geom 
				     from ew_wards_4326 x 
					  where x.geom &&
				       st_MakeEnvelope (  ".$p['x']." , ".$p['y']." , ".$p['x2']." , ".$p['y2']." , 4326 )";	
					   
			$hndl = self::$dbh -> query($sql);		
			$stmt = $hndl -> fetchAll(PDO::FETCH_ASSOC);
			self :: $dbh->commit();
			return $stmt;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
		
	}
	
	public function getBounds($gid){
		try{
		    $sql = "with a as 
				    (select st_extent(geom) as g from ew_wards_4326 where gid=?)
				      select st_ymax(g),st_xmax(g),st_ymin(g),st_xmin(g)  from a";	
					   
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array($gid));		
			$res = $hndl -> fetch(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
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
		    $sql = " " ;		   
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
		    $sql = "select column_name from information_schema.columns where table_name= ? " ;		   
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array($table));	
			$res = $hndl -> fetchAll(PDO::FETCH_NUM);
			self :: $dbh->commit();
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
    
}

?>