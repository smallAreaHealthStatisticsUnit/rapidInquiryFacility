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
            self :: $dbh = new PDO("pgsql:host=localhost;dbname=rif4_canvas;user=postgres;password=Imperial1234", array( PDO::ATTR_PERSISTENT => true )); 
		    //self :: $dbh = new PDO("pgsql:dbname=rif4;host=localhost;user=postgres;password=fava" , array( PDO::ATTR_PERSISTENT => true )); 
			self :: $dbh-> setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION );
		}catch(PDOException $pe){
		 	die('Connection error: ' .$pe->getMessage());
		}
	}
	
	public function exec($sql){
		self :: $dbh->exec($sql);
	}
	
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
			$geos = array();
			foreach($res as $geo){
				array_push($geos, $geo[0]); 	
			}
			return $geos;
			
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
         			and  NOT (column_name = ANY ( ARRAY['".$geom."' , 'gid' , 'row_index'])) and 
					(data_type = ANY ( ARRAY ['smallint' ,  'integer' , 'bigint', 'decimal', 'numeric', 'real', 'double precision',
					 'smallserial', 'serial', 'bigserial' ]))" ;
				
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_NUM);
			self :: $dbh->commit();
			
			$fields = array();
			foreach($res as $field){
				array_push($fields, $field[0]); 	
			}
			
			return $fields;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	
	}
	
	public function getFieldsAvlb( $table ){
		try{
		    $geom = $this->getGeometryCol( $table );
		    $sql = "select column_name from information_schema.columns where table_name='". $table ."'
         			and  NOT (column_name = ANY ( ARRAY['".$geom."' , 'gid', 'row_index']))  " ;
			
	
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_NUM);
			self :: $dbh->commit();
			
			$fields = array();
			foreach($res as $field){
				array_push($fields, $field[0]); 	
			}
			
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
					and  NOT column_name = ANY ( ARRAY['".$geom."' , 'gid', 'id','row_index'])";
					
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
	
	public function getTabularData($table, $fields, $from , $to){
		try{
			
			/*
			 * Assumes the corresponding tabulat data is stored in a table called $table + '_data'
			 */
			
			if ( !isset( $fields ) ){
				$fields = $this->getFieldsAsSingleArray( $table );
			};

			$cols = implode( ",", $fields );
			$limit = $to - $from;
			
			$plgsql = 
			"DO $$
			DECLARE
			current_stmt varchar ;
			data_stmt varchar;
			countrow integer ;
		
			 BEGIN  
			   
			   select count(*)  into countrow  from information_schema.tables where table_name ='datatable' ;
			   
			   IF countrow = 1 THEN
				current_stmt := 
				'drop table  if exists currentselection;		
				create  table currentselection as 
				  with a as(
					  select 
					   concat(concat(gid,''_''), row_index)  as rowid
					   from  $table  order by rowid asc  limit   $limit    offset   $from 
				   )select  rowid from a where rowid not in (select  rowid from datatable)';
				
				data_stmt := 
				'create table datatable2 as 
					select  rowid from currentselection 
					  union 
					   select  rowid from datatable;  
				drop table datatable;		
				alter table datatable2 rename  to datatable ;';
				
			   ELSE 
				current_stmt := 
				 'create  table currentselection as 
				  select 
				   concat(concat(gid,''_''), row_index)  as rowid 
				    from $table order by rowid asc limit  $limit    offset   $from ';	
				
				data_stmt := 
				'create table datatable as 
					select rowid from currentselection' ; 
					
			   END IF;
			   
			   EXECUTE (current_stmt);
			   EXECUTE (data_stmt);
			   
			END;
			$$"."language plpgsql;";
			//echo $plgsql;
			$hndl = self::$dbh -> query($plgsql);

			$sql = "select rowid as id, $cols from currentselection a inner join  $table b on a.rowid = b.gid ||'_'|| b.row_index";
			//echo $sql;
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_ASSOC);
			self :: $dbh->commit();
			
		    return $res;	
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	
	public function getTableRows($table, $fields, $gids){
		try{
			
			$cols = implode( ",", $fields );
			$whereClause = "";
			if(isset($gids)){
			    $whereClause =  $this->getGidClause($gids, " = ", " 	or ");
			}
			
			$plgsql = 
			"DO $$
			DECLARE
			current_stmt varchar ;
			data_stmt varchar;
			countrow integer ;
		
			 BEGIN  
			   
			   select count(*)  into countrow  from information_schema.tables where table_name ='datatable' ;
			   
			   IF countrow = 1 THEN
				current_stmt := 
				'drop table  if exists currentselection;		
				create  table currentselection as 
				  with a as(
					  select 
					   concat(concat(gid,''_''), row_index)  as rowid
					   from  $table  $whereClause 
				   )select  rowid from a where rowid not in (select  rowid from datatable)';
				
				data_stmt := 
				'create table datatable2 as 
					select  rowid from currentselection 
					  union 
					   select  rowid from datatable;  
				drop table datatable;		
				alter table datatable2 rename  to datatable ;';
				
			   ELSE 
				current_stmt := 
				 'create  table currentselection as 
				  select 
				   concat(concat(gid,''_''), row_index)  as rowid 
				    from $table  $whereClause ';	
				
				data_stmt := 
				'create table datatable as 
					select rowid from currentselection' ; 
					
			   END IF;
			   
			   EXECUTE (current_stmt);
			   EXECUTE (data_stmt);
			   
			END;
			$$"."language plpgsql;";
			
			$hndl = self::$dbh -> query($plgsql);
			
			$sql = "select rowid as id, $cols from currentselection a inner join  $table b on a.rowid = b.gid ||'_'|| b.row_index";
			
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			$res = $hndl -> fetchAll(PDO::FETCH_ASSOC);
			self :: $dbh->commit();
	
			return $res;	
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}	
	}
	public function getDataSetsAvailable( $geolevel){
		/*
		 * This has to be replaced with a query that check for 
		 * all data tables available for the specific geolevel
		 */
		 $dataset= $geolevel . "_data";
		return array( $dataset); 
	}
	
	public function dropDatatable(){
		try{
			
			$sql = "drop table  if exists datatable;drop table  if exists currentselection;";
			$hndl = self::$dbh -> query($sql);
			self :: $dbh->commit();	
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	
	}
	
	
    private function getGidClause($gids, $operator, $andor){
		$where = array();
		$whereClause = "  where  ";
		foreach($gids as $gid){
			array_push($where, " gid $operator $gid "); 	
		}
		$whereClause .= implode(  $andor , $where );
		return $whereClause;
	}
	
	public function getAgeGroups($geolevel){
		try{
			//age group definition for particular geolevel
			$sql = "select distinct age_group as agegroup, minage, maxage from age_groups order by age_group asc";
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());
			$res = $hndl -> fetchAll(PDO::FETCH_ASSOC);
			return $res;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	}
	
	public function getPyramidData($geolevel, $field, $year, $gids){
	
		try{

			$agegrouptable = $geolevel ."_agegroups";
			
			$gidClause = "";
			if(isset($gids)){
				$gidClause .= $this->getGidClause( $gids, '=' , 'or');
			}
			
			$groupby = "";
			
		    $sql = "select age_group, sex, random() * sum($field) 
					  from $agegrouptable 
					      $gidClause
					      group by age_group,sex
						   order by age_group asc";
			
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			
			$csv = "agegroup,sex,$field" . "\n";
			
			while ($row = $hndl -> fetch(PDO::FETCH_ASSOC)){
				$csv .=  implode($row, ',') . "\n" ;
			}
			
			self :: $dbh->commit();
			
			return $csv;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	
	}

		public function getPyramidDataByYear($geolevel, $field, $year, $gids){
	
		try{

			$agegrouptable = $geolevel ."_agegroups";
			
			$yearClause = "";
			if(isset($year)){
				$yearClause = "year = " . $year;
			}
			
			$gidClause = "";
			if(isset($gids)){
				//$gidClause = " and ";
				$gidClause .= $this->getGidClause( $gids, '=' , 'or');
			}
			
			
		    $sql = "select age_group, sex, random() * sum($field) 
					  from $agegrouptable 
					      $gidClause $yearClause
					       group by age_group,sex
						    order by age_group";
			
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			
			$csv = "agegroup,sex,$field" . "\n";
			
			while ($row = $hndl -> fetch(PDO::FETCH_ASSOC)){
				$csv .=  implode($row, ',') . "\n" ;
			}
			
			self :: $dbh->commit();
			
			return $csv;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	}
	
	
	//Retrieves  risk vaules ( no confidence intervals) for each area id in study given a result set  .
	public function getRiskResults($resSet){
		
		try{
		 
			// Column x_order will be used for the x axis in the area chart
			// This is required by the plug in drawing the area chart
			
			// Column x_order will be used for the x axis in the area chart
			// This is required by the plug in drawing the area chart
			
			$sql = "select gid, round($resSet,3) as $resSet , row_number() over (ORDER BY $resSet) as x_order 
			from atlas_leu_f order by x_order  " ;

			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			
			$csv = "gid,$resSet,x_order" . "\r\n";
			
			while ($row = $hndl -> fetch(PDO::FETCH_ASSOC)){
				$csv .=  implode($row, ',') . "\r\n" ;
			}
			
			self :: $dbh->commit();
			
			return $csv;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	
	}
	
	/*public function getMinMaxResultSet($resSet, $studyId, $invId){
		try{
		 	
			$sql = "select gid, round($resSet,3) as $resSet , row_number() over (ORDER BY $resSet) as x_order 
			from atlas_leu_f where $resSet <=2 order by $resSet  " ;

			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			
			$csv = "gid,$resSet,x_order" . "\n";
			
			while ($row = $hndl -> fetch(PDO::FETCH_ASSOC)){
				$csv .=  implode($row, ',') . "\n" ;
			}
			
			self :: $dbh->commit();
			
			return $csv;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	}*/
	
	/*
	 * Retrieve disease mapping results:
	 * Area level value and confidence intervals
     * Type can be: Relative risk , smoothed relative risk, Relative risk adj , smoothed relative risk adj
	 *
	 */
	public function getResultSet( $resultSet /*, $studyId, $invId, $year*/  ){
		
		try{
            
         /*
          *  Column x_order will be used for the x axis in the area chart
		  *  This is required by the plug in drawing the area chart
          *  
          *   $resultSet is  ! HARDCODED !
          *
          */
		 $sql = "select gid,round(rr_unadj,4) as $resultSet, round(rrl95_unadj,2) as llsrr,round(rru95_unadj,2) as ulsrr, row_number() over (ORDER BY  rr_unadj) as x_order
				  from atlas_leu_f ";
			
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());	
			
			$csv = "gid,$resultSet,llsrr,ulsrr,x_order" . "\n";
			
			while ($row = $hndl -> fetch(PDO::FETCH_ASSOC)){
				$csv .=  implode($row, ',') . "\n" ;
			}
			
			self :: $dbh->commit();
			
			return $csv;
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
			
	}
	public function getResultFigures( $gid ){
	    try{
            
		 $sql = "select code, pop_m as pop, 
		          round( (pop_m / 10) * random()) as observed,
		            round( (pop_m / 10) * random()) as expected 
					  from atlas_leu_f a inner join atlas_data b on a.gid = b.gid  
					     where a.gid = $gid ";
			
			$hndl = self::$dbh -> prepare($sql);
			$hndl ->execute(array());
			$res = $hndl -> fetchAll(PDO::FETCH_ASSOC);
			return $res[0];
			
		}catch(PDOException $pe){
			self :: $dbh->rollback();
		 	die( $pe->getMessage());
		}
	
	}
	
	
	public function getFieldsStratifiedByAgeGroup($theme){
		/*
		 * Retrieve all fields for the given theme stratifified by age group and gender
		 * HARDCODED FOR NOW
		 *
		 */
		return array("popcount");
			
	}
	
	
	public function getSingleFieldValues($table, $field, $gids){
		
		
		$gidClause = "";
		if(isset($gids)){
			$gidClause .= $this->getGidClause( $gids, '=' , 'or');
		}
		
		$sql = "select $field from $table $gidClause";

		$hndl = self::$dbh -> prepare($sql);
	    $hndl ->execute(array());	
		
		$res = $hndl -> fetchAll(PDO::FETCH_ASSOC);
		return $res;
	
	}
	
}

?>