/*
 * SQL statement name: 	insert_geography.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. GEOGRAPHY_CB_2014_US_COUNTY_500K
 *						2: geography; e.g. CB_2014_US_500K
 *						3: geography description; e.g. United states to county level
 *						4: hierarchytable; e.g. HIERARCHY_CB_2014_US_500K
 *						5: geometrytable; e.g. GEOMETRY_CB_2014_US_500K
 *						6: tiletable; e.g. TILES_CB_2014_US_500K
 * 						7: SRID; e.g. 4269
 *						8: Default comparision area, e.g. GEOID
 *						9: Default study area, e.g. STATENS
 *						10: Min zoomlevel
 *						11: Max zoomlevel
 *           			12: Postal population table (quote enclosed or NULL)
 *      				13: Postal point column (quote enclosed or NULL)
 *						14: Partition (0/1)
 *						15: Max geojson digits
 *
 * Description:			Insert into geography table
 * Note:				%%%% becomes %% after substitution
 */
INSERT INTO %1 (
geography, description, hierarchytable, geometrytable, tiletable, srid, defaultcomparea, defaultstudyarea, minzoomlevel, maxzoomlevel,
		postal_population_table, postal_point_column, partition, max_geojson_digits)
SELECT '%2' AS geography,
       '%3' AS description,
       '%4' AS hierarchytable,
	   '%5' AS geometrytable,
	   '%6' AS tiletable,
       %7   AS srid,
       NULL AS defaultcomparea,	/* See: update_geography.sql */
       NULL AS defaultstudyarea,
	   %10  AS minzoomlevel,
	   %11  AS maxzoomlevel,
	   %12  AS postal_population_table,
       %13  AS postal_point_column,
       %14  AS partition, 
       %15  AS max_geojson_digits