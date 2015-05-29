/*
Function returns the current value of a specified sequence (since SQL Server appears not to have a function that already does that)
*/

use sahsuland_dev
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

