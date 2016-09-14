/*
 * SQL statement name: 	add_unique_key.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. cb_2014_us_nation_5m
 *						2: constraint name; e.g. cb_2014_us_nation_5m_uk
 *						3: fields; e.g. areaid
 *
 * Description:			Add unique key constraint
 * Note:				%%%% becomes %% after substitution
 */
ALTER TABLE %1 ADD CONSTRAINT %2 UNIQUE(%3)