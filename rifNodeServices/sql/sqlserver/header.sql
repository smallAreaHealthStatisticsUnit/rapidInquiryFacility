--
-- Create processed CSV tables created from shapefiles simplification:
-- a) Shaqpefile tables, e.g:
--    * cb_2014_us_county_500k                            
--    * cb_2014_us_nation_5m                            
--    * cb_2014_us_state_500k          
-- b) Psuedo comntrol tables copies of RIF40 control tables, e.g:                   
--    * geography_usa_2014                               
--    * geolevels_usa_2014     
-- c) Processed geometry data (partitioned in PostGres), e.g:                          
--    * geometry_usa_2014                                  
-- d) Hierarchy table, e.g:
--    * hierarchy_usa_2014   
-- e) Lookup tables, e.g:
--    * lookup_cb_2014_us_county_500k             
--    * lookup_cb_2014_us_nation_5m                    
--    * lookup_cb_2014_us_state_500k             
-- f) Tables used to calculate tile interesections
--    * t_tiles_usa_2014   
--    * tile_blocks_usa_2014                         
--    * tile_intersects_usa_2014 (partitioned in PostGres) 
--    * tile_limits_usa_2014                           
--    * tiles_usa_2014    
--
-- MS SQL Server specific parameters
--
-- Usage: sqlcmd -E -b -m-1 -e -r1 -i %1 -v pwd="%cd%"
-- Connect flags if required: -U <username>/-E -S<myServerinstanceName>
--
-- You must set the current schema if you cannot write to the default schema!
-- You need create privilege for the various object and the bulkadmin role
--
-- USE <my database>;
--
SET QUOTED_IDENTIFIER ON;
-- SET STATISTICS TIME ON;