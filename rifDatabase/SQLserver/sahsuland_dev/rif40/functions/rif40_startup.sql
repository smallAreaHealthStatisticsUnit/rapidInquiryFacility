/*
MS SQL Server has logon triggers, but currently keeping this as a standalone procedure explicitly called by Java to be consistent with the Postgres version.

Note: Not fully implemented.

Function (Procedure, otherwise cannot use consistent error handling):
 	rif40_startup()
Parameters:	No check boolean; default FALSE (do the checks)
Description:	Startup functions - for calling from /usr/local/pgsql/etc/psqlrc
		Postgres has no ON-LOGON trigger
		Java users will need to run this first
		
		Check running non privileged, SET search path, setup logging
		If no checks flag is set, no further checks or object creation carried out
*/

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_startup]')
                  AND type IN ( N'P', N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP PROCEDURE [rif40].[rif40_startup]
GO 

CREATE PROCEDURE [rif40].[rif40_startup](@l_skip_checks int=0)
 AS
BEGIN
--
-- Defaults if set to NULL
--
	IF @l_skip_checks IS NULL SET @l_skip_checks=0;
	IF @l_skip_checks = 1
	BEGIN
		RETURN;
	END;
	
	-- Must be rif40 or have rif_user or rif_manager role
	IF (IS_MEMBER(N'[rif_manager]') = 0 AND IS_MEMBER(N'[rif_user]') = 0)
		BEGIN TRY
			DECLARE @err1 VARCHAR(max);
			SET @err1 = formatmessage(51000, SUSER_SNAME());
			THROW 51000, @err1, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif_startup]';
			THROW 51000, @err1, 1; --rethrow
		END CATCH;		
		
	-- User is a rif user; and MUST be non privileged; streaming replication for backup is allowed
	--check for superuser/database creation privilege/update system catalog privilege
	IF (IS_SRVROLEMEMBER(N'sysadmin') = 1 
		or IS_SRVROLEMEMBER (N'dbcreator' )=1
		or IS_MEMBER(N'db_owner')=1 
		or HAS_PERMS_BY_NAME(null, null, 'CREATE ANY DATABASE')=1)
			BEGIN TRY
			DECLARE @err2 VARCHAR(max);
			SET @err2 = formatmessage(51150, SUSER_SNAME());
			THROW 51150, @err2, 1;
		END TRY
		BEGIN CATCH
			EXEC [rif40].[ErrorLog_proc] @Error_Location='[rif40].[rif_startup]';
			THROW 51150, @err2, 1; --rethrow
		END CATCH;			
	
	--check for tables to exist and whether database features are installed and enabled
		
END
GO

GRANT EXECUTE ON [rif40].[rif40_startup] TO rif_user, rif_manager;
GO

