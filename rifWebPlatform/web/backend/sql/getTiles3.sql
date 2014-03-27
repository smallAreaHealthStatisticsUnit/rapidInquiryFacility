
insert into  slctd (code)
 select distinct stward03 as code from ew_wards_4326 where 
	ew_wards_4326.geom && st_MakeEnvelope ( -10.625000000000013 , 50.73645513701064 , -2.8124999999999902 ,48.92249926375824 , 4326 ) 



-- select stward03 as code,st_asGeoJSON(geom,3,0) as geom from slctd inner join ew_wards_4326 on slctd.code=ew_wards_4326.stward03 	

  


