/*
 Insert ids and TileId to "temp" table 
*/
insert into slctd (code,tileId) 
 select distinct stward03 as code ,'65_41' 
  from ew_wards_4326 
   where stward03 not in (select code from slctd) 
    and ew_wards_4326.geom && 
	    st_MakeEnvelope ( 2.8124999999999902 , 54.16243396806781 , 5.625000000000013 , 52.48278022207821 , 4326 )
		

/*
 select deduplicated areas to draw based on tiled id 
*/		
select stward03 as code,st_asGeoJSON(geom,3,0) as geom 
 from slctd 
  inner join ew_wards_4326 on slctd.code=ew_wards_4326.stward03 
   where tileid = '65_41'		