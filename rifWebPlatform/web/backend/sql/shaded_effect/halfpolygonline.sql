/*
 * 1. Create a straight line passing by geometric centre
 *	-Get bounding box
 *	-Get Centroid
 *	-Draw straight line passing by centroid
 * 		
 * 2. Cut polygon using line created in 1  	
 *	
 * 3. Repeat step 1 and 2 as desired	  
*/

CREATE OR REPLACE FUNCTION halfpolygonline(geometry) returns  geometry  as $$
 with 
 a as(
  select ST_ExteriorRing(st_envelope($1)) as envelope,st_centroid($1) as centroid 
 ),

  b as(
  select st_MakeLine(
   ST_PointN(
    a.envelope,1 
   ),
   ST_PointN( 
    a.envelope,2 
   )
  )as sideleft,
  st_MakeLine(
   ST_PointN(
    a.envelope,3 
   ),
    ST_PointN( 
     a.envelope,4 
    )
   ) as sideright,
   st_MakeLine(
    ST_PointN(
     a.envelope,1 
    ),
    ST_PointN( 
     a.envelope,4 
    )
   )as sidebottom from a
  )
  
  select ST_LineMerge (st_collect(ST_ShortestLine(a.centroid,b.sideleft) , ST_ShortestLine(a.centroid,b.sideright) ))
	from a,b;

$$ LANGUAGE sql;  