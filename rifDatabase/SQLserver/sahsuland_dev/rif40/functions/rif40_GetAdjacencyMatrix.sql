-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 get adjacency matrix
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
             FROM sys.objects
            WHERE object_id = OBJECT_ID(N'[rif40].[sahsuland_GetAdjacencyMatrix]')
              AND type IN ( N'TF' )) /*  SQL table-valued-function */
	DROP FUNCTION [rif40].[sahsuland_GetAdjacencyMatrix];
GO 

IF EXISTS (SELECT *
             FROM sys.objects
            WHERE object_id = OBJECT_ID(N'[rif40].[rif40_GetAdjacencyMatrix]')
              AND type IN ( N'P' )) /*  SQL table-valued-function */
	DROP PROCEDURE [rif40].[rif40_GetAdjacencyMatrix];
GO 

CREATE FUNCTION [rif40].[sahsuland_GetAdjacencyMatrix](@study_id INTEGER)
RETURNS @rtnTable TABLE 
(
--
--  Columns returned by the function
--
	geolevel_id		INTEGER			NOT NULL,
	areaid			VARCHAR(200)	NOT NULL,
	num_adjacencies INTEGER			NOT NULL,
	adjacency_list	VARCHAR(8000)	NOT NULL
)
AS
BEGIN
	DECLARE c1 CURSOR FOR 
		SELECT b2.adjacencytable, b2.geography
		  FROM [rif40].[rif40_studies] b1, [rif40].[rif40_geographies] b2
		 WHERE b1.study_id  = @study_id	    
		   AND b2.geography = b1.geography;
	DECLARE @adjacencytable AS VARCHAR(30);
	DECLARE @geography AS VARCHAR(30);
	OPEN c1;
	FETCH NEXT FROM c1 INTO @adjacencytable, @geography;
	IF @adjacencytable IS NULL
		 DECLARE @error INT=CAST('Study geography has no adjacency table' AS INT); /* This is better than nothing! */
	CLOSE c1;
	DEALLOCATE c1;		   
--
	WITH b AS ( /* Tilemaker: has adjacency table */
		SELECT b1.area_id, b3.geolevel_id
		  FROM [rif40].[rif40_study_areas] b1, [rif40].[rif40_studies] b2, [rif40].[rif40_geolevels] b3
		 WHERE b1.study_id  = @study_id
		   AND b1.study_id  = b2.study_id	    
		   AND b2.geography = b3.geography
	)
	INSERT INTO @rtnTable(geolevel_id, areaid, num_adjacencies, adjacency_list)
	SELECT c1.geolevel_id, c1.areaid, c1.num_adjacencies, c1.adjacency_list
	  FROM [rif_data].[adjacency_sahsuland] c1, b
	 WHERE c1.geolevel_id   = b.geolevel_id
	   AND c1.areaid        = b.area_id;  
--
	RETURN;
END;
GO

CREATE PROCEDURE [rif40].[rif40_GetAdjacencyMatrix](@study_id INTEGER)
AS
BEGIN
	DECLARE c1 CURSOR FOR 
		SELECT b2.adjacencytable, b2.geography
		  FROM [rif40].[rif40_studies] b1, [rif40].[rif40_geographies] b2
		 WHERE b1.study_id  = @study_id	    
		   AND b2.geography = b1.geography;
	DECLARE @adjacencytable AS VARCHAR(30);
	DECLARE @geography AS VARCHAR(30);
	OPEN c1;
	FETCH NEXT FROM c1 INTO @adjacencytable, @geography;
	IF @adjacencytable IS NULL
		 RAISERROR('Study %d geography %s has no adjacency table', 1, 16, @study_id, @geography);
	CLOSE c1;
	DEALLOCATE c1;		   
--
	DECLARE @crlf AS VARCHAR(2)=CHAR(10)+CHAR(13);
	DECLARE @sql_stmt AS NVARCHAR(max);
	SET @sql_stmt='WITH b AS ( /* Tilemaker: has adjacency table */' + @crlf +
	'	SELECT b1.area_id, b3.geolevel_id' + @crlf +
	'	  FROM [rif40].[rif40_study_areas] b1, [rif40].[rif40_studies] b2, [rif40].[rif40_geolevels] b3' + @crlf +
	'	 WHERE b1.study_id  = @nstudy_id' + @crlf +
	'	   AND b1.study_id  = b2.study_id' + @crlf +	    
	'	   AND b2.geography = b3.geography' + @crlf +
	')' + @crlf +
	'SELECT c1.geolevel_id, c1.areaid, c1.num_adjacencies, c1.adjacency_list' + @crlf +
	'  FROM [rif_data].[' + @adjacencytable + '] c1, b' + @crlf +
	' WHERE c1.geolevel_id   = b.geolevel_id' + @crlf +
	'   AND c1.areaid        = b.area_id';
	/* INSERT INTO @rtnTable(geolevel_id, areaid, num_adjacencies, adjacency_list) */
	EXECUTE sp_executesql @sql_stmt, 
		N'@nstudy_id INTEGER', @study_id;   
--
	RETURN;
END;
GO

GRANT SELECT, REFERENCES ON [rif40].[sahsuland_GetAdjacencyMatrix] TO rif_user, rif_manager;
GO

GRANT EXECUTE ON [rif40].[rif40_GetAdjacencyMatrix] TO rif_user, rif_manager;
GO

--
-- Eof