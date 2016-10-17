/*
 * SQL statement name: 	insert_geography.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. geography_cb_2014_us_county_500k
 *						2: geography; e.g. cb_2014_us_500k
 *						3: geography description; e.g. "United states to county level"
 *						4: hierarchytable; e.g. hierarchy_cb_2014_us_500k
 *						5: geometrytable; e.g. geometry_cb_2014_us_500k
 *						6: tiletable; e.g. tiles_cb_2014_us_500k
 * 						7: SRID; e.g. 4269
 *						8: Default comparision area
 *						9: Default study area
 *						10: Min zoomlevel
 *						11: Max zoomlevel
 *
 * Description:			Insert into geography table
 * Note:				%%%% becomes %% after substitution
 */
INSERT INTO %1 (
geography, description, hierarchytable, geometrytable, tiletable, srid, defaultcomparea, defaultstudyarea, minzoomlevel, maxzoomlevel)
SELECT '%2' AS geography,
       '%3' AS description,
       '%4' AS hierarchytable,
	   '%5' AS geometrytable,
	   '%6' AS tiletable,
       %7   AS srid,
       '%8' AS defaultcomparea,
       '%9' AS defaultstudyarea,
	   %10  AS minzoomlevel,
	   %11  AS maxzoomlevel