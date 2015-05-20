 /*
  Database owner cannot also be a member of the 'rif40_manager' or 'rif40_user' groups
  in Microsoft SQL Server.  This function allows the person installing the database also 
  be a rif user and manager. (In the future, make permission of the DBA part of the 
  configuration process?)
  
  Returns 1 for yes, 0 for no (invalid roles also return 0/no)
 
  Actually we don't want the db_owner to be a RIF user at all because that would break 
  all illusion of Information Governance controls
 */
  CREATE FUNCTION dbo.rif40_is_member(@rolename VARCHAR(256)) RETURNS BIT
  AS
  BEGIN
	DECLARE @membership BIT;
	IF NOT EXISTS(SELECT 1 from sys.sysusers
		WHERE name=@rolename AND issqlrole=1 )
			SET @membership=0;
	ELSE
		BEGIN
			SET @membership = IS_MEMBER(@rolename);
			IF @membership = 0 
				SET @membership= IS_MEMBER('db_owner');
		END;
	RETURN @membership;
   END
  