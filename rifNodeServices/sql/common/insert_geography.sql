/*
 * SQL statement name: 	insert_geography.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. geography_cb_2014_us_county_500k
 *						2: geography; e.g. cb_2014_us_500k
 *						3: geography description; e.g. "United states to county level"
 *						4: hierarchytable; e.g. hierarchy_cb_2014_us_500k
 * 						5: SRID; e.g. 4269
 *						6: Default comparision area
 *						7: Default study area
 *
 * Description:			Insert into geography table
 * Note:				%%%% becomes %% after substitution
 */
INSERT INTO %1 (
geography, description, hierarchytable, srid, defaultcomparea, defaultstudyarea)
SELECT '%2' AS geography,
       '%3' AS description,
       '%4' AS hierarchytable,
       %5 AS srid,
       '%6' AS defaultcomparea,
       '%7' AS defaultstudyarea