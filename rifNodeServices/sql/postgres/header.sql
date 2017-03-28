--
-- Create processed CSV tables created from shapefiles simplification:
--
-- a) Shapefile tables, e.g:
--    * cb_2014_us_county_500k                            
--    * cb_2014_us_nation_5m                            
--    * cb_2014_us_state_500k          
-- b) Psuedo control tables copies of RIF40 control tables, e.g:                   
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
--    * tile_blocks_usa_2014                         
--    * tile_intersects_usa_2014 (partitioned in PostGres) 
--    * tile_limits_usa_2014    
-- g) Tiles table and view               
--    * t_tiles_usa_2014           
--    * tiles_usa_2014                           
--

--
-- Postgres specific parameters
--
-- Usage: psql -w -e -f %1
-- Connect flags if required: -U <username> -d <Postgres database name> -h <host> -p <port>
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

--
-- Make the same as the Windows code pager
--
SET client_encoding='WIN1252';