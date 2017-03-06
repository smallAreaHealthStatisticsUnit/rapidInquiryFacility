/*
Takes username and role name and returns 1 if user has the role and 0 if user does not have role

Simplified equivalent to Postgres's pg_has_role function
*/

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_has_role]')
                  AND type IN ( N'FN', N'IF', N'TF', N'FS', N'FT' ))
  DROP FUNCTION [rif40].[rif40_has_role]
GO 


CREATE FUNCTION [rif40].[rif40_has_role](@l_username VARCHAR(max), @l_rolename VARCHAR(max))
	RETURNS int AS
BEGIN
	DECLARE @user_id INT, @role_id INT, @user_has_role INT;
	SET @user_id = (
		SELECT principal_id
		from [sys].[database_principals]
		where name=@l_username);
	SET @role_id = (
		SELECT principal_id
		from [sys].[database_principals]
		where name=@l_rolename);
	IF @user_id IS NULL OR @role_id IS NULL
		RETURN 0;
	
	SET @user_has_role = (
		SELECT count(*)
		FROM [sys].[database_role_members]
		WHERE [role_principal_id]=@role_id 
		AND [member_principal_id]=@user_id);
	RETURN @user_has_role;
END;
GO	