CREATE OR REPLACE FUNCTION makegrid(geometry, integer)
RETURNS geometry AS
'SELECT ST_Collect(st_setsrid(ST_POINT(x/1000000::float,y/1000000::float),st_srid($1))) FROM 
  generate_series(floor(st_xmin($1)*1000000)::int, ceiling(st_xmax($1)*1000000)::int,$2) as x ,
  generate_series(floor(st_ymin($1)*1000000)::int, ceiling(st_ymax($1)*1000000)::int,$2) as y 
WHERE st_intersects($1,ST_SetSRID(ST_POINT(x/1000000::float,y/1000000::float),ST_SRID($1)))'
LANGUAGE sql