-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 ddl
--
-- Copyright:
--
-- The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
-- that rapidly addresses epidemiological and public health questions using 
-- routinely collected health and population data and generates standardised 
-- rates and relative risks for any given health outcome, for specified age 
-- and year ranges, for any given geographical area.
--
-- Copyright 2014 Imperial College London, developed by the Small Area
-- Health Statistics Unit. The work of the Small Area Health Statistics Unit 
-- is funded by the Public Health England as part of the MRC-PHE Centre for 
-- Environment and Health. Funding for this project has also been received 
-- from the Centers for Disease Control and Prevention.  
--
-- This file is part of the Rapid Inquiry Facility (RIF) project.
-- RIF is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- RIF is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
-- to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
-- Boston, MA 02110-1301 USA
--
-- Author:
--
-- Peter Hambly, SAHSU
--

-- This script must be run from the installation directory

IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_ddl]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_ddl];
GO 
		   
IF EXISTS (SELECT *
           FROM   sys.table_types WHERE name = 'Sql_stmt_table')
	DROP TYPE [rif40].[Sql_stmt_table];
GO 

CREATE TYPE [rif40].[Sql_stmt_table] AS TABLE   
( sql_stmt NVARCHAR(MAX));  
GO  

CREATE PROCEDURE [rif40].[rif40_ddl](@rval INT OUTPUT, @sql_stmts rif40.Sql_stmt_table READONLY, @debug INTEGER=0)
AS
BEGIN
/*
 Function:		rif40_ddl()
 Parameter:		Success or failure [INTEGER], SQL statements table(sql_stmt VARCHAR(MAX)), debug flag (0/1) [Default: 0]
 Returns:		Success or failure [INTEGER], as  first parameter
 Description:	Run DDL.
 */
 
--
-- Defaults if set to NULL
--
	IF @debug IS NULL SET @debug=0;
	
	DECLARE c1_ddl CURSOR FOR
		SELECT sql_stmt
		  FROM @sql_stmts;
	DECLARE @sql_stmt			NVARCHAR(MAX);
--
	DECLARE @crlf  		VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @err_msg 	VARCHAR(MAX);
--
	OPEN c1_ddl;
	FETCH NEXT FROM c1_ddl INTO @sql_stmt;
	WHILE @@FETCH_STATUS = 0
	BEGIN	
		BEGIN TRY
			EXECUTE sp_executesql @sql_stmt;
			PRINT 'SQL[' + USER + '] OK> ' + @sql_stmt + ';';
		END TRY
		BEGIN CATCH		
--	 		[55999] SQL statement had error: %s%sSQL[%s]> %s;	
			SET @err_msg = formatmessage(55999, error_message(), @crlf, USER, @sql_stmt); 
			THROW 55999, @err_msg, 1;
--			PRINT 'SQL statement had error: ' + error_message() + @crlf + 'SQL[' + USER + ']> ' + @sql_stmt + ';';
		END CATCH;
--
		FETCH NEXT FROM c1_ddl INTO @sql_stmt;
	END;
	CLOSE c1_ddl;
	DEALLOCATE c1_ddl;
	
END;
GO

--
-- Eof