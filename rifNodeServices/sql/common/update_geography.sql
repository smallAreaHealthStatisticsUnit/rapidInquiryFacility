/*
 * SQL statement name: 	update_geography.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. GEOGRAPHY_CB_2014_US_COUNTY_500K
 *						2: geography; e.g. CB_2014_US_500K
 *						3: Default comparision area, e.g. GEOID
 *						4: Default study area, e.g. STATENS
 *
 * Description:			Insert into geography table
 * Note:				%%%% becomes %% after substitution
 */
UPDATE %1
   SET defaultcomparea  = '%3',
       defaultstudyarea = '%4'
 WHERE geography = '%2'