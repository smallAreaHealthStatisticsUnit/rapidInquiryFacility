/*
Function returns the current value of a specified sequence (since SQL Server appears not to have a function that already does that)
*/

use sahsuland_dev
GO

SELECT name, type, type_desc
  FROM sys.objects
 WHERE object_id = OBJECT_ID(N'[rif40].[rif40_sequence_current_value]')
   AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' );
GO
SELECT SCHEMA_NAME(o.SCHEMA_ID) AS referencing_schema_name,
	   o.name AS referencing_object_name,
	   o.type_desc AS referencing_object_type_desc
  FROM sys.sql_expression_dependencies sed
	INNER JOIN sys.objects o ON sed.referencing_id = o.[object_id]
	LEFT OUTER JOIN sys.objects o1 ON sed.referenced_id = o1.[object_id]
 WHERE sed.referenced_entity_name = 'rif40_sequence_current_value';
 GO
WITH a AS (
	SELECT SCHEMA_NAME(o.SCHEMA_ID) AS referencing_schema_name,
		   o.name AS referencing_object_name,
		   o.type_desc AS referencing_object_type_desc
	  FROM sys.sql_expression_dependencies sed
		INNER JOIN sys.objects o ON sed.referencing_id = o.[object_id]
		LEFT OUTER JOIN sys.objects o1 ON sed.referenced_id = o1.[object_id]
	 WHERE sed.referenced_entity_name = 'rif40_sequence_current_value'
)
SELECT a.referencing_object_name AS rif40_sequence_current_value_ref_obj,
	   a.referencing_object_type_desc AS rif40_sequence_current_value_ref_type,
       SCHEMA_NAME(o.SCHEMA_ID) AS referencing_schema_name,
       o.name AS referencing_object_name,
       o.type_desc AS referencing_object_type_desc
  FROM a, sys.sql_expression_dependencies sed
	INNER JOIN sys.objects o ON sed.referencing_id = o.[object_id]
	LEFT OUTER JOIN sys.objects o1 ON sed.referenced_id = o1.[object_id]
 WHERE sed.referenced_entity_name = a.referencing_object_name
GO

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_sequence_current_value]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_sequence_current_value]
GO 

CREATE FUNCTION [rif40].[rif40_sequence_current_value](@l_sequence_name VARCHAR(max))
	RETURNS INT AS
BEGIN
DECLARE @current_val INT;
IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(@l_sequence_name) AND type in (N'SO'))
	BEGIN
		SELECT @current_val = convert(int,current_value )
		FROM sys.sequences WHERE object_id = OBJECT_ID(@l_sequence_name);
		RETURN @current_val;
	END
--ELSE
--error message about invalid sequence
	RETURN 0;
END
GO

