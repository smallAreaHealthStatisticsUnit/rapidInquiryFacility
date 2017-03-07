/*
Saves error messages into rif40_ErrorLog table
Also saves them in the general rif40_log program 
*/


IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_ErrorLog]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_ErrorLog]
END
GO

 -- Create our error log table
 CREATE TABLE [rif40].[rif40_ErrorLog]
 (
     [Error_Number] 			INT 		NOT NULL,
     [Error_LINE] 				INT 		NOT NULL,
     [Error_Location]			sysname  	NULL
		CONSTRAINT dfltError_Location DEFAULT ('Unknown'),
     [Error_Message] 			VARCHAR(MAX),
     [SPID] 					INT, 
     [Program_Name] 			VARCHAR(255),
     [Client_Address] 			VARCHAR(255),
     [Authentication] 			VARCHAR(50),
     [Error_User_Application] 	VARCHAR(100),
     [Error_Date] 				datetime NULL
		CONSTRAINT dfltErrorLog_error_date DEFAULT (GETDATE()),
     [Error_User_System] 		sysname 	NOT NULL
		CONSTRAINT dfltErrorLog_error_user_system DEFAULT (SUSER_SNAME())
 )
 GO

--permissions??
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_ErrorLog] TO [rif_manager]
GO
/*
GRANT SELECT,INSERT ON [rif40].[rif40_ErrorLog] TO [public]
GO
 */
 
--------------------------------------------
 -- create the proc 
 --------------------------------------------

IF EXISTS (SELECT * FROM sys.objects WHERE type = N'P' AND 
object_id = OBJECT_ID(N'[rif40].[ErrorLog_proc]'))
BEGIN
	DROP PROCEDURE [rif40].[ErrorLog_proc]
END
GO

 CREATE PROCEDURE [rif40].[ErrorLog_proc]
 (
     @Error_Number INT = NULL,
	 @Error_LINE INT = NULL,
     @Error_Location sysname = NULL,
     @Error_Message VARCHAR(4000) = NULL,
     @UserID INT = NULL
 ) 
AS
 BEGIN
 
 	IF @Error_Message IS NOT NULL 
		exec [rif40].[rif40_log] 'ERROR',null,@Error_Message;
	
     BEGIN TRY
     
        INSERT INTO [rif40].[rif40_ErrorLog]
         (
             [Error_Number]
			 ,[Error_LINE]
             ,[Error_Location]
             ,[Error_Message]
             ,[SPID]
             ,[Program_Name]
             ,[Client_Address]
             ,[Authentication]
             ,[Error_User_System]
             ,[Error_User_Application]
         )
         SELECT 
              [Error_Number]             = COALESCE(@Error_Number, ERROR_NUMBER(), 'Unknown')  
			 ,[Error_Line]				 = COALESCE(@Error_LINE, ERROR_LINE(), 'Unknown')  
             ,[Error_Location]           = COALESCE(@Error_Location, ERROR_MESSAGE(), 'Unknown')
             ,[Error_Message]            = COALESCE(@Error_Message, ERROR_MESSAGE(), 'Unknown')
             ,[SPID]                     = @@SPID -- SESSION_id/connection_number
             ,[Program_Name]             = ses.program_name
             ,[Client_Address]           = con.client_net_address
             ,[Authentication]           = con.auth_scheme           
             ,[Error_User_System]        = SUSER_SNAME()
             ,[Error_User_Application]   = @UserID
        FROM sys.dm_exec_sessions ses
         LEFT JOIN sys.dm_exec_connections con
             ON con.session_id = ses.session_id
         WHERE ses.session_id = @@SPID;

         
    END TRY
    BEGIN CATCH
         -- We even failed at the log entry so let's get basic
         INSERT INTO [rif40].[rif40_ErrorLog]
         (
             ERROR_NUMBER
			 ,ERROR_LINE
             ,ERROR_LOCATION
             ,ERROR_MESSAGE
         )
         VALUES 
        (
             -100
			 ,-100
             ,NULLIF(OBJECT_NAME(@@PROCID), 'Unknown')
             ,'Error Log Procedure Errored out'
         );
    END CATCH
	
END
GO
 
--
-- Eof