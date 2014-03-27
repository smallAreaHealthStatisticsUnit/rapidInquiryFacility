
with 
a as (
	select st_MakeEnvelope ( -5.625000000000013 , 50.73645513701064 , -2.8124999999999902 ,48.92249926375824 , 4326 ) 
	as box
),
b as (
	select stward03 as wards from ew_wards_4326 b , a where 
	 st_contains(a.box,b.geom)
),
c AS (	 

	select distinct b1.stward03 AS stward03, b1.geom 
	  from (
		select distinct stward03, geom
		  from ew_wards_4326, b, a 
		 where st_intersects(a.box , ew_wards_4326.geom ) 
		   and stward03 not in (select wards from b)) b1, ew_wards_4326 , a 
         where st_contains (a.box , st_centroid(b1.geom ) ) 
), d AS (
SELECT c.stward03 AS wards
  FROM c
  UNION
  SELECT b.wards
    FROM b
    )
SELECT d.wards, st_asGeoJSON(geom,3,0)  
  FROM d inner join ew_wards_4326 e on d.wards = e.stward03;
  


