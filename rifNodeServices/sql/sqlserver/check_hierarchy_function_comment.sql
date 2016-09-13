DECLARE @CurrentUser sysname  /*
 * SQL statement name: 	check_hierarchy_function_comment.sql
 * Type:				Microsoft SQL Server T/sql anonymous block
 * Parameters:
 *						1: function name; e.g. check_hierarchy_cb_2014_us_500k
 *
 * Description:			Create hierarchy check function comment
 * Note:				%%%% becomes %% after substitution
 */
SELECT @CurrentUser = user_name(); 
EXECUTE sp_addextendedproperty 'MS_Description',   
   'Procedure: 		check_hierarchy_%1()
Parameters:		Geography, hierarchy table, type: "missing", "spurious additional" or "multiple hierarchy", 
				Error count (OUT)
Returns:		Nothing
Description:	Diff geography hierarchy table using dynamic method 4
				Also tests the hierarchy, i.e. all a higher resolutuion is contained by one of the next higher and so on

Example of dynamic SQL. Note the use of an array return type to achieve method 4

WITH /* missing */ a1 AS (
        SELECT COUNT(*) AS cb_2014_us_nation_5m_total
          FROM (
                SELECT cb_2014_us_nation_5m FROM hierarchy_cb_2014_us_500k
                EXCEPT
                SELECT cb_2014_us_nation_5m FROM lookup_cb_2014_us_nation_5m) as1)
, a2 AS (
        SELECT COUNT(*) AS cb_2014_us_state_500k_total
          FROM (
                SELECT cb_2014_us_state_500k FROM hierarchy_cb_2014_us_500k
                EXCEPT
                SELECT cb_2014_us_state_500k FROM lookup_cb_2014_us_state_500k) as2)
, a3 AS (
        SELECT COUNT(*) AS cb_2014_us_county_500k_total
          FROM (
                SELECT cb_2014_us_county_500k FROM hierarchy_cb_2014_us_500k
                EXCEPT
                SELECT cb_2014_us_county_500k FROM lookup_cb_2014_us_county_500k) as3)
SELECT ARRAY[a1.cb_2014_us_nation_5m_total, a2.cb_2014_us_state_500k_total, a3.cb_2014_us_county_500k_total] AS res_array
FROM a1, a2, a3;

Or: 

WITH /* multiple hierarchy */ a2 AS (
        SELECT COUNT(*) AS cb_2014_us_state_500k_total
          FROM (
                SELECT cb_2014_us_state_500k, COUNT(DISTINCT(cb_2014_us_nation_5m)) AS total
                  FROM hierarchy_cb_2014_us_500k
                 GROUP BY cb_2014_us_state_500k
                HAVING COUNT(DISTINCT(cb_2014_us_nation_5m)) > 1) as2)
, a3 AS (
        SELECT COUNT(*) AS cb_2014_us_county_500k_total
          FROM (
                SELECT cb_2014_us_county_500k, COUNT(DISTINCT(cb_2014_us_state_500k)) AS total
                  FROM hierarchy_cb_2014_us_500k
                 GROUP BY cb_2014_us_county_500k
                HAVING COUNT(DISTINCT(cb_2014_us_state_500k)) > 1) as3)
SELECT ARRAY[a2.cb_2014_us_state_500k_total, a3.cb_2014_us_county_500k_total] AS res_array
FROM a2, a3;
',
   'user', @CurrentUser,   
   'procedure', '%1'