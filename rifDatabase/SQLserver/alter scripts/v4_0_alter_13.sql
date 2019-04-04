-- ************************************************************************
-- *
-- * THIS IS A SCHEMA ALTER SCRIPT - IT CAN BE RE-RUN BUT THEY MUST BE RUN 
-- * IN NUMERIC ORDER
-- *
-- ************************************************************************
--
-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- $Id$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Inquiry Facility (RIF) - RIF alter script 13: Issue #138 State 1: study setup modal errors fixes; 
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
-- Working directory: c:/Users/Peter/Documents/GitHub/rapidInquiryFacility/rifDatabase/SQLserver/alter scripts
-- Usage: sqlcmd -U rif40 -d <database name> -b -m-1 -e -r1 -i v4_0_alter_13.sql -v pwd="%cd%"
-- Connect flags if required: -P <password> -S<myServerinstanceName>
--
-- The middleware must be down for this to run
--
-- MAKE SURE YOU ADD TO run_alter_scripts.bat for the installer
--
SET QUOTED_IDENTIFIER ON;
-- SET STATISTICS TIME ON;

--
-- Set schema variable used by scripts etc to RIF_DATA
--
:SETVAR SchemaName "rif_data"
--

BEGIN TRANSACTION;
GO

PRINT 'Running SAHSULAND schema alter script 13: Issue #138 State 1: study setup modal errors fixes';
GO

/*
 * Alter 13: Issue #138 State 1: study setup modal errors fixes; 

 1. Rebuild rif40_numerator_outcome_columns view
 
 */
:r ..\sahsuland_dev\rif40\views\rif40_numerator_outcome_columns.sql

--
-- Testing stop
--
/*
ROLLBACK;
 */
COMMIT TRANSACTION;
GO

--
--  Eof 