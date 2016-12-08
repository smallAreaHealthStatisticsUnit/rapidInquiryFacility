/*
 * SQL statement name: 	insert_geolevel.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. GEOLEVELS_CB_2014_US_COUNTY_500K
 *						2: geography; e.g. CB_2014_US_500K
 *						3: Geolevel name; e.g. CB_2014_US_COUNTY_500K
 *						4: Geolevel id; e.g. 3
 *						5: Geolevel description; e.g. "The State-County at a scale of 1:500,000"
 *						6: lookup table; e.g. LOOKUP_CB_2014_US_COUNTY_500K
 * 						7: shapefile; e.g. cb_2014_us_county_500k.shp
 *						8: shapefile table; e.g. CB_2014_US_COUNTY_500K
 *						9: covariate_table; e.g. CB_2014_US_500K_COVARIATES_CB_2014_US_COUNTY_500K
 *						10: shapefile_area_id_column; e.g. COUNTYNS
 *
 * Description:			Insert into geography table
 * Note:				%%%% becomes %% after substitution
 */
INSERT INTO %1 (
   geography, geolevel_name, geolevel_id, description, lookup_table,
   lookup_desc_column, shapefile, shapefile_table, shapefile_area_id_column, shapefile_desc_column,
   resolution, comparea, listing, covariate_table)
SELECT '%2' AS geography,
       '%3' AS geolevel_name,
       %4 AS geolevel_id,
       '%5' AS description,
       '%6' AS lookup_table,
       'AREANAME' AS lookup_desc_column,
       '%7' AS shapefile,
       '%8' AS shapefile_table,
       %10 AS shapefile_area_id_column,
       'AREANAME' AS shapefile_desc_column,
       1 AS resolution,
       1 AS comparea,
       1 AS listing,
	   %9 AS covariate_table