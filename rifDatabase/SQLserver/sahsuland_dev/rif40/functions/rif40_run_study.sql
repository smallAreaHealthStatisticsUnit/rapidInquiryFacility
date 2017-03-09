-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 run study
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
IF EXISTS (SELECT *
           FROM   sys.objects
           WHERE  object_id = OBJECT_ID(N'[rif40].[rif40_run_study]')
                  AND type IN ( N'P' ))
	DROP PROCEDURE [rif40].[rif40_run_study]
GO 

CREATE PROCEDURE [rif40].[rif40_run_study](@rval INT OUTPUT, @study_id int, @debug int=0, @recursion_level int=0)
AS
BEGIN
--
-- Defaults if set to NULL
--
	IF @debug IS NULL SET @debug=0;
	IF @recursion_level IS NULL SET @recursion_level=0;
	
/*
Function:	rif40_run_study()
Parameter:	Study ID, enable debug (INTEGER: default 0), recursion level (internal parameter DO NOT USE)
Returns:	Success or failure [INTEGER]
			Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
			Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Run study 

Runs as INVOKER. Alternate version calling this function runs as DEFINER USER

Check study state - 

C: created, not verfied; 
V: verified, but no other work done; 
E - extracted imported or created, but no results or maps created; 
R: results computed; 
U: upgraded record from V3.1 RIF (has an indeterminate state; probably R).

Define transition
Create extract, call: rif40_sm_pkg.rif40_create_extract()
Runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
Compute results, call: rif40_sm_pkg.rif40_compute_results()
Do update. This forces verification
(i.e. change in study_State on rif40_studies calls rif40_sm_pkg.rif40_verify_state_change)
Recurse until complete
 */
	SET @rval=0 /* Failure */;
	
--	
-- Error: Recursion %i, rif40_run_study study %i had error.
--
	DECLARE @err_msg VARCHAR(MAX) = formatmessage(55216, @recursion_level, @study_id);
	THROW 55216, @err_msg, 1;
		
	RETURN @rval;
END;
GO

GRANT EXECUTE ON [rif40].[rif40_run_study] TO rif_user, rif_manager;
GO

--
-- Eof