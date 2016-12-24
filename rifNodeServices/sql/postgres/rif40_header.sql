--
-- Load processed geomtry and tiles tables into production database:
--       
-- a) integrate with RIF40 control tables, e.g:                   
--    * geography_usa_2014                               
--    * geolevels_usa_2014     
-- b) Processed geometry data (partitioned in PostGres), e.g:                          
--    * geometry_usa_2014                                  
-- c) Hierarchy table, e.g:
--    * hierarchy_usa_2014   
-- d) Lookup tables, e.g:
--    * lookup_cb_2014_us_county_500k             
--    * lookup_cb_2014_us_nation_5m                    
--    * lookup_cb_2014_us_state_500k      
-- e) Tiles table and view               
--    * t_tiles_usa_2014           
--    * tiles_usa_2014   
--

--
-- Postgres RIF40 specific parameters
--
-- Usage: psql -U rif40 -w -e -f %1
-- Connect flags if required: -d <Postgres database name> -h <host> -p <port>
--
\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing
