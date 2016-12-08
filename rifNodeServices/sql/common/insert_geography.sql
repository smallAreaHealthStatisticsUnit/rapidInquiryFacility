/*
 * SQL statement name: 	insert_geography.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. geography_cb_2014_us_county_500k
 *						2: geography; e.g. cb_2014_us_500k
 *						3: geography description; e.g. United states to county level
 *						4: hierarchytable; e.g. hierarchy_cb_2014_us_500k
 *						5: geometrytable; e.g. geometry_cb_2014_us_500k
 *						6: tiletable; e.g. tiles_cb_2014_us_500k
 * 						7: SRID; e.g. 4269
 *						8: Default comparision area
 *						9: Default study area
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
       '%8' AS defaultcomparea,
       '%9' AS defaultstudyarea,
	   %10  AS minzoomlevel,
	   %11  AS maxzoomlevel,
	   %12  AS postal_population_table,
       %13  AS postal_point_column,
       %14  AS partition, 
       %15  AS max_geojson_digits