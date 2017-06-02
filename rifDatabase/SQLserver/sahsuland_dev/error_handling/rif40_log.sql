/*
A general logging function.

I am not certain where or how the log messages should be recorded in the long term.  I just want to be able
to have a log function to call in the triggers.

Currently output messages to screen.
*/

IF EXISTS (SELECT * FROM sys.objects WHERE type = N'P' AND 
object_id = OBJECT_ID(N'[rif40].[rif40_log]'))
BEGIN
	DROP PROCEDURE [rif40].[rif40_log]
END
GO

CREATE PROCEDURE [rif40].[rif40_log]
 (
	@debug_level VARCHAR(50) = NULL,
	@function_name VARCHAR(max) = NULL,
	@msg	VARCHAR(max) = NULL
)
AS
BEGIN
	IF @debug_level <> 'ERROR'
		print 'LOG '+@debug_level+' '+@function_name+': '+COALESCE(@msg, 'NULL Message');
END
GO
