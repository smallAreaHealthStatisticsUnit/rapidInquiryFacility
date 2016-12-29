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
-- MS SQL Server specific parameters
--
-- Usage: sqlcmd -U rif40 -d <database name> -b -m-1 -e -r1 -i %1 -v pwd="%cd%"
-- Connect flags if required: -P <password> -S<myServerinstanceName>
--
-- You must set the current schema if you cannot write to the default schema!
-- You need create privilege for the various object and the bulkadmin role
--
-- USE <my database>;
--
SET QUOTED_IDENTIFIER ON;
-- SET STATISTICS TIME ON;

--
-- Set schema variable used by scripts etc to RIF_DATA
--
:SETVAR SchemaName "rif_data"
--
