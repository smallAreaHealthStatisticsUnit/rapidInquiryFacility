/*
Function: 	rif40_db_name_check()
Parameters:	Column name, value
Returns:	NONE
Description:	Check column name value obeys DB naming conventions; i.e. Oracles. Value is assumed to be in upper case; even on Postgres where the convention is lower case	
*/

use sahsuland_dev
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_db_name_check]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_db_name_check]
GO 


CREATE PROCEDURE [rif40].[rif40_db_name_check](@l_columnname VARCHAR(max), @l_value VARCHAR(max))
	AS
BEGIN
    
    
    SELECT REGEXP_REPLACE(l_value, '[[:upper:]]{1,}[[:upper:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g') AS invalid_characters,
		       CASE
				WHEN LENGTH(REGEXP_REPLACE(l_value, '[[:upper:]]{1,}[[:upper:]{0,}[:digit:]{0,}_{0,}]{0,}', '', 'g')) > 0 THEN TRUE
				ELSE FALSE END::BOOLEAN AS is_invalid;
--
