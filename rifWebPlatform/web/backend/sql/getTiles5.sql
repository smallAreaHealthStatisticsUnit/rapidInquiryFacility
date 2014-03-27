with a as (
select
 st_MakeEnvelope ( -5.625000000000013 , 54.16243396806781 , -2.8124999999999902 ,52.48278022207821 , 4326 ) as box
), b as (
select stward03 from ew_wards_4326 x where st_DWithin( st_makeLine(st_setSRID(st_makePoint(-5.625000000000013 , 54.16243396806781 ),4326)
, st_setSRID(st_makePoint(-2.8124999999999902 , 54.16243396806781),4326)), x.geom, 0)
),c as (
select stward03 from ew_wards_4326 x where 
st_DWithin( st_makeLine(st_setSRID(st_makePoint(-5.625000000000013 , 54.16243396806781 ),4326), 
 st_setSRID(st_makePoint(-5.625000000000013 , 52.48278022207821),4326)), x.geom, 0)
),d as(
select stward03 from ew_wards_4326 x , a 
where st_contains(a.box,x.geom))select stward03 as code , st_asGeoJSON(geom,3,0) as geom 
from ew_wards_4326 x 
where stward03 in ( select stward03 from d) 
or stward03 in ( select stward03 from c)
 or stward03 in ( select stward03 from d where stward03 not in (select stward03 from c))