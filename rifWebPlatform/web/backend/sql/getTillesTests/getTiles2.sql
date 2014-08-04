
with 
a as (
	select st_MakeEnvelope ( -10.625000000000013 , 50.73645513701064 , -2.8124999999999902 ,48.92249926375824 , 4326 ) 
	as box
),
b as (
	select stward03 as wards  from ew_wards_4326 b , a where 
	 st_contains(a.box,b.geom)
)
 

select wards,st_asGeoJSON(geom,3,0) from (
	select distinct stward03 as wards
		from 
		  ew_wards_4326 c, b, a 
		where 
		  stward03 not in (select wards from b) and 
		  a.box && c.geom and 
		  st_contains (a.box , st_centroid(c.geom ) ) 
	UNION 
	select b.wards from b
 )d inner join ew_wards_4326 e on  d.wards =  e.stward03     


  


