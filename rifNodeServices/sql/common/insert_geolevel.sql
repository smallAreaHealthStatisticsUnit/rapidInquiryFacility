/*
 * SQL statement name: 	insert_geolevel.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. geolevels_cb_2014_us_county_500k
 *						2: geography; e.g. cb_2014_us_500k
 *						3: Geolevel name; e.g. cb_2014_us_county_500k
 *						4: Geolevel id; e.g. 3
 *						5: Geolevel description; e.g. "The State-County at a scale of 1:500,000"
 *						6: lookup table; e.g. lookup_cb_2014_us_county_500k
 * 						7: shapefile; e.g. cb_2014_us_county_500k.shp
 *						8: shapefile table; e.g. cb_2014_us_county_500k
 *
 * Description:			Insert into geography table
 * Note:				%%%% becomes %% after substitution
 */
INSERT INTO %1 (
   geography, geolevel_name, geolevel_id, description, lookup_table,
   lookup_desc_column, shapefile, shapefile_table, shapefile_area_id_column, shapefile_desc_column,
   resolution, comparea, listing)
SELECT '%2' AS geography,
       '%3' AS geolevel_name,
       %4 AS geolevel_id,
       '%5' AS description,
       '%6' AS lookup_table,
       'areaname' AS lookup_desc_column,
       '%7.shp' AS shapefile,
       '%8' AS shapefile_table,
       'areaid' AS shapefile_area_id_column,
       'areaname' AS shapefile_desc_column,
       1 AS resolution,
       1 AS comparea,
       1 AS listing