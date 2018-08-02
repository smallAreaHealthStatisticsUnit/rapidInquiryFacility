TileMaker/TileViewer TODO LIST
==============================

* Peter Hambly, October 2017

# TileMaker/TileViewer

TileMaker is currently working with some minor faults but needs to:

* Run the generated scripts
* Support very large shapefiles (e.g. COA2011)
* Needs a manual!
* GUI's needs to be merged and brought up to same standard as the rest of the RIF. The TileViewer screen is in better shape 
  than the TileMaker screen. Probably the best solution is to use Angular.
* Support for database logons
* Needs to calculate geographic centroids using the database

## Minor issues

The following minor issues from PH's development notes will be explained properly, reviewed and then categorised:

* US geography and centroids fixes
* Add area name to results map table
* Area tests (area_check.sql) is failing for Halland - suspect area is too small, could be projection is wrong. 
* Use of the Geography datatype in SQL Server - needs to be converted to geometry
* NVarchar support for areaName
* Get fetch views to handle zoomlevel beyond max zoomlevel (returning the usual NULL geojson)
* Add tileid to tile topoJSON/GeoJSON; include in error messages; add version number 
  (yyyymmddhh24mi) for caching (i.e. there is no need to age them out if auto compaction is running)
* Fix blank name properties
* Add all properties from lookup table
* Missing name in level2 sahsuland (caused by mixed case field names)
* Add parent area_id, name
* Resize not working correctly
* Add all Shapefile DBF fields to lookup table;
* UUID support
* Add Winston logging to backend
* Separate DB logons using UUID; add username/password support
* Convert v4_0_create_sahsuland.sql to use tileMaker sahsuland, and remaining test scripts)
* Tilemaker drop scripts. Probably needed for v4_0_drop.sql and hence sahsuland_dev rebuild
* SAHSULAND was using Nevada north 1927, now using OSGB at present. The original plan
  was for standard test configurations:
  * SAHSULAND: relocated to Utah: reprojected to 1983 North American Projection (EPSG:4269)
	* Obsolete t_rif40_sahsu_geometry/t_rif40_sahsu_maptiles; use rif40_geolevels lookup_table/tile_table
  * DOGGERLAND: relocated to 54°20'0"N 5°42'59"E on the Dogger Bank. This is the site of wreck of SMS Blucher. 
    https://www.google.co.uk/maps/place/54%C2%B020'00.0%22N+5%C2%B042'59.0%22E/@54.3332107,0.9702213,5.92z/data=!4m5!3m4!1s0x0:0x0!8m2!3d54.3333333!4d5.7163889 
  * USA: USA to county level [this is OK]
  
  These will need to use a suitable projection within bounds and also be translated to the desired place. i.e. using proj4 in Node.
  
* Fix zoomlevel field miss-set from config file (defaults are wrong)
* Convert remaining use of geography:: datatype in SQL Server to geometry::. The geography:: datatype is used in the build
  to intersect tiles and will may have issues. Production SQL Server is using the geometry:: datatype. This will be parked if 
  it is not a problem.
* JSZip 3.0 upgrade required (forced to 2.6.0) for present
* Tilemaker etc:
  * Add old files in DBF file to lookup tables;  
  * Drive database script generator from XML config file (not internal data structures);
  * Missing comments on other columns from shapefile via extended attributes XML file;
  * Dump SQL to XML/JSON files (Postgres and SQL Server) so Kevin does not need to generate it;
  * Add trigger verification code from Postgres to tilemaker build tables (t_rif40_geolelvels, rif40_geographies);
  * Fix in Node:
    - Triangles (to keep QGIS happy)
    - Self-intersections, e.g. at or near point -76.329400888614401 39.31505881204005
    - Too few points in geometry component at or near point -91.774770828512843 46.946012696542709
  * Check Turf JS centroid code (figures are wrong);
  * Compare Turf/PostGIS/SQL Server area and centroid caculations;
* Tilemaker Get methods: 
  * ZIP results;
  * Run front end and batch from XML config file.
  * Add CSV files meta to XML config;
  
Note: no bounding box (bbox) in tiles.

## Parked TODO (todon't) list (as required):

* MS Sahsuland projection problem; must be fully contained within the projection or area calculations and intersections will fail. 
  This needs to be detected. Currently geography datatype caluclates area wrong even after the polygon has been reorientated (i.e. 
  follow the right hand rule). The script: area_check.sql fails on SQL Server at this point. PostGIS will complain:

	transform: couldn't project point (-77.0331 -12.1251 0):
		latitude or longitude exceeded limits (-14)
  
   PostGIS, SQL Server and PROJ.4 (i.e. the projection file)  does not  have these bounds. Each projection's bounds are unique, and are 
   traditionally published by the authority that designed the projection. One of the primary sources for this data is from 
   https://www.epsg-registry.org. This would require an additional table. 

   So it is not easy to check if the geometry not within projected bounds, although possible.
* Read DBF header so shapefile reader knows number of expected records; add to status update
* Timeout recovery (switches to batch mode).
* Favicon support: https://github.com/expressjs/serve-favicon
* Database connection; clean, check OK and ST_Union(); area support [and checks]; PK support. Turf probably removes requirement 
  for any DB port, subject to acceptable performance
* Handle when areaID == name; allow NULL name
* Detect area mismatch between shapefiles	
* Prevent submit whilst running shpConvert method
* Prevent tab change during map draw and aoccordion setup or JQuery UI and Leaflet do bad things unless tkey have focus
* Fix customFileUpload styling so it uses the correct JQuery UI class style; the .css() function won't work on form file upload buttons
* Add support for XML config file so shpConvert can do all processing without further input;
* Restrict geolevels to a minimum 3, or more if the total topojson_size < ~20-30M (possibly browser dependent). 

* Add areaKm2 (using bounding box) as jsonfile property. Needs turf.
* Calucation of quantization and the max zoomlevel using area. Enforcement in browser. 
* Hover support for area name, area_km2 and shapefile supplied data at highest resolution
* SQL server RHR force to support mixed LH and RH in multipolygons
  [c.%1.ReorientObject() is used as c.%1.STUnion(%1.STStartPoint()) does NOT work] where %1 is the geometry column 
  [Needs Turf support]
* Display of zoomlevel contextual information: total topojson size, suppressed or not. 
* Status in write JSON file Re-test COA2011: json memory and timeout issues are solved
* Add simplify to zoomlevel 11, spherical simplify limit (in Steraradians) [Probably no, only use quantization at max zoomlevel] .
* Duplicate file names in zip files. Flattening of directory structure causes duplicates which are not detected
* Add convertedTopojson array meta data to topojson config
* Add startup parameterisation (db, if, port etc) using cjson
* Test json file
* Change audit trail: Unions, linestring to polygon conversions, ST_invalid => ST_MakeValid geomtery validators; 
* Add tests:
	- Unsupported projection files (modify proj data slightly...)
	- Wrong shapefile (by bounds) in set
	- No shapefile with only 1 area if > 1 shapefile
	- Total area mismatch between shapefiles
* Add GID, shapefile fields to lookup tables;
* Add areaid as well as <geolevel_name> in lookup tables;
  
  