// ************************************************************************
//
// GIT Header
//
// $Format:Git ID: (%h) %ci$
// $Id: 7ccec3471201c4da4d181af6faef06a362b29526 $
// Version hash: $Format:%H$
//
// Description:
//
// Rapid Enquiry Facility (RIF) - Generate DB load scripts
//
// Copyright:
//
// The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
// that rapidly addresses epidemiological and public health questions using 
// routinely collected health and population data and generates standardised 
// rates and relative risks for any given health outcome, for specified age 
// and year ranges, for any given geographical area.
//
// Copyright 2014 Imperial College London, developed by the Small Area
// Health Statistics Unit. The work of the Small Area Health Statistics Unit 
// is funded by the Public Health England as part of the MRC-PHE Centre for 
// Environment and Health. Funding for this project has also been received 
// from the Centers for Disease Control and Prevention.  
//
// This file is part of the Rapid Inquiry Facility (RIF) project.
// RIF is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// RIF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
// to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
// Boston, MA 02110-1301 USA
//
// Author:
//
// Peter Hambly, SAHSU

const serverLog = require('../lib/serverLog'),
	  nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon'),
	  httpErrorResponse = require('../lib/httpErrorResponse');

const os = require('os'),
	  fs = require('fs'),
	  path = require('path');
	  
/*
 * Function: 	getSqlFromFile()
 * Parameters:	File name (in directory: rapidInquiryFacility\rifNodeServices\sql\postgres),
 *				dbbase type as a string ("PostGres" or "MSSQLServer"); can be undefined (uses common directory),
 *				0 or more parameters
 * Returns:		SQL statement with parameters replaced
 * Description:	Get SQL statement file from directory (postgres/sqlserver/common); replace %N with 0..N parameters
 *				SQL statements.
 *
 *				Argument replacement:
 *
 *				%1 is replaced by arguments[2] and so on to max arguments
 *				Unreplaced arguments are not detected
 *				%%1 becomes %1
 *				%%%% becomes %% after substitution
 */
var getSqlFromFile = function getSqlFromFile(fileName, dbType, parameters) {
	var dir="sql/";
	
	if (dbType == undefined) {	
		dir+="common";
	}	
	else if (dbType == "PostGres") {	
		dir+="postgres";
	}
	else if (dbType == "MSSQLServer") {	
		dir+="sqlserver";
	}
	else {
		throw new Error("getSqlFromFile(): Invalid dbType: " + dbType);
	}
	var sqlBuffer=fs.readFileSync(dir + "/" + fileName);
	if (sqlBuffer == undefined) {
		throw new Error("getSqlFromFile(): No SQL in file: " + dir + "/" + fileName);
	}
	var sqlText=sqlBuffer.toString();
	
	if (parameters) { // Replace %1 with arguments[2] etc; ignore %%
		for (var i = 2; i < arguments.length; i++) {
//			console.error("getSqlFromFile(): replace %" + (i-1) + " with: " + arguments[i]);
			sqlText=sqlText.replace(new RegExp('%' + (i-1), 'g'), arguments[i]); 
				// No negative lookbehind in javascript so cannot ignore %%1
		}
	}
	// Replace %% with %
	sqlText=sqlText.replace(new RegExp('%%', 'g'), '%');
	
	return sqlText;
} // End of getSqlFromFile()
		
/*
 * Function: 	CreateDbLoadScripts()
 * Parameters:	Internal response object, HTTP request object, HTTP response object, dir, csvFiles object, callback to call at end of processing
 * Description:	Convert geoJSON to CSV; save as CSV files; create load scripts for Postgres and MS SQL server
 */		
var CreateDbLoadScripts = function CreateDbLoadScripts(response, req, res, dir,  csvFiles, endCallback) {
	
	scopeChecker(__file, __line, {
		response: response,
		message: response.message,
		fields: response.fields,
		geographyName: response.fields["geographyName"],
		min_zoomlevel: response.fields["min_zoomlevel"],
		max_zoomlevel: response.fields["max_zoomlevel"],
		srid: response.fields["srid"],
		dir: dir,
		serverLog: serverLog,
		req: req,
		res: res,
		httpErrorResponse: httpErrorResponse,
		nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon,
		callback: endCallback
	});

	/*
	 * Function: 	createSQLScriptHeader()
	 * Parameters:	Script file name (full path), dbbase type as a string ("PostGres" or "MSSQLServer")
	 * Description:	Create header for SQL script
	 */		
	var createSQLScriptHeader=function createSQLScriptHeader(scriptName, dbType) {
		var newStream;
		try {
			newStream = fs.createWriteStream(scriptName, { flags : 'w' });	
			newStream.on('finish', function pgStreamClose() {
				response.message+="\n" + dbType + "streamClose(): " + scriptName;
			});		
			newStream.on('error', function pgStreamError(e) {
				serverLog.serverLog2(__file, __line, dbType + "StreamError", 
					"WARNING: Exception in " + dbType + " SQL script write to file: " + scriptName, req, e, response);										
			});
			
			// Comment syntax is the same in SQL server (sqlcmd) and Postgres; as is transaction control
			
			var header=getSqlFromFile("header.sql", undefined /* Common */);
			newStream.write(header);
			header=getSqlFromFile("header.sql", dbType);
			newStream.write(header);
		}
		catch (e) {
			serverLog.serverLog2(__file, __line, dbType + "StreamError", 
				"WARNING: Exception in " + dbType + " SQL script stream create; file: " + scriptName, req, e, response);		
		}
		return newStream;
	} // End of createSQLScriptHeader()
	
	/*
	 * Function: 	pad()
	 * Parameters:	Padding string, string, left pad boolean
	 * Description:	RPAD/LPAD implmentation: 
	 *				http://stackoverflow.com/questions/2686855/is-there-a-javascript-function-that-can-pad-a-string-to-get-to-a-determined-leng
	 */
	function pad(pad, str, padLeft) {
		if (typeof str === 'undefined') {
			return pad;
		}
		if (padLeft) {
			return (pad + str).slice(-pad.length);
		} 
		else {
			return (str + pad).substring(0, pad.length);
		}
	}

	/*
	 * Function: 	addSQLStatements()
	 * Parameters:	Database stream, format file stream, CSV files object, srid (spatial reference identifier), 
	 *				dbbase type as a string ("PostGres" or "MSSQLServer")
	 * Description:	Add SQL statements
	 */		
	var addSQLStatements=function addSQLStatements(dbStream, csvFiles, srid, dbType) {
		
		/*
		 * Function: 	beginTransaction()
		 * Parameters:	None
		 * Description:	Begin transaction SQL statements
		 */	 
		function beginTransaction() {
			var sqlStmt=new Sql("Start transaction");
			if (dbType == "PostGres") {		
				sqlStmt.sql="BEGIN";	
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="BEGIN TRANSACTION";	
			}				
			sql.push(sqlStmt);
		} // End of beginTransaction()
		
		/*
		 * Function: 	commitTransaction()
		 * Parameters:	None
		 * Description:	Commit transaction SQL statements
		 */	 
		function commitTransaction() {
			var sqlStmt=new Sql("Commit transaction");
			if (dbType == "PostGres") {		
				sqlStmt.sql="END";	
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="COMMIT";	
			}				
			sql.push(sqlStmt);
		} // End of commitTransaction()	
		
		/*
		 * Function: 	analyzeTables()
		 * Parameters:	None
		 * Description:	Analze and describe all tables: SQL statements
		 *				Needs to be in a separate transaction (do NOT start one!)
		 */	 	
		function analyzeTables() {
			sql.push(new Sql("Analyze tables"));
			var tableList=[];
			for (var i=0; i<csvFiles.length; i++) {
				tableList.push(csvFiles[i].tableName);
				tableList.push("lookup_" + csvFiles[i].tableName);
			}
			tableList.push("geolevels_" + response.fields["geographyName"].toLowerCase());
			tableList.push("geography_" + response.fields["geographyName"].toLowerCase());
			tableList.push("hierarchy_" + response.fields["geographyName"].toLowerCase());
			
			for (var i=0; i<tableList.length; i++) {												
				var sqlStmt=new Sql("Describe table " + tableList[i], 
					getSqlFromFile("describe_table.sql", dbType, tableList[i] /* Table name */)); 
				sql.push(sqlStmt);
				var sqlStmt=new Sql("Analyze table " + tableList[i], 
					getSqlFromFile("analyze_table.sql", dbType, tableList[i] /* Table name */)); 
				sql.push(sqlStmt);
			} // End of for csvFiles loop			
		} // End of analyzeTables()		 

		/*
		 * Function: 	createGeolevelsLookupTables()
		 * Parameters:	None
		 * Description:	Create geoelvels lookup tables: SQL statements
		 */	 
		function createGeolevelsLookupTables() {
			sql.push(new Sql("Geolevels lookup tables"));
			for (var i=0; i<csvFiles.length; i++) {									
				var sqlStmt=new Sql("Drop table lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("drop_table.sql", dbType, "lookup_" + csvFiles[i].tableName /* Table name */)); 
				sql.push(sqlStmt);	
				
				var sqlStmt=new Sql("Create table lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("create_lookup_table.sql", undefined /* Common */, csvFiles[i].tableName /* Table name */)); 
				sql.push(sqlStmt);				
				
				var sqlStmt=new Sql("Insert table lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("insert_lookup_table.sql", undefined /* Common */, csvFiles[i].tableName /* Table name */)); 
				sql.push(sqlStmt);			
				
				var sqlStmt=new Sql("Add primary key lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
						"lookup_" + csvFiles[i].tableName 	/* Table name */, 
						csvFiles[i].tableName 				/* Primary key */)); 
				sql.push(sqlStmt);

				var sqlStmt=new Sql("Comment table lookup_" + csvFiles[i].tableName,
					getSqlFromFile("comment_table.sql", 
						dbType, 
						"lookup_" + csvFiles[i].tableName,		/* Table name */
						"Lookup table for " + csvFiles[i].geolevelDescription 	/* Comment */)
					);
				sql.push(sqlStmt);			
			}				
		} // End of createGeolevelsLookupTables()
		
		/*
		 * Function: 	analyzeTables()
		 * Parameters:	None
		 * Description:	Create hierarchy table: SQL statements
		 */	 
		function createHierarchyTable() {
			sql.push(new Sql("Hierarchy table"));	
			
			var sqlStmt=new Sql("Drop table hierarchy_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, "hierarchy_" + response.fields["geographyName"].toLowerCase() /* Table name */)); 
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Create table hierarchy_" + response.fields["geographyName"].toLowerCase());
			sqlStmt.sql="CREATE TABLE hierarchy_" + response.fields["geographyName"].toLowerCase() + " (\n";
			var pkField=undefined;
			for (var i=0; i<csvFiles.length; i++) {	
				if (i == 0) {
					sqlStmt.sql+="	" + csvFiles[i].tableName + "	varchar(100)  NOT NULL";
				}
				else {
					sqlStmt.sql+=",\n	" + csvFiles[i].tableName + "	varchar(100)  NOT NULL";
				}
			
				if (csvFiles[i].geolevel == csvFiles.length && pkField == undefined) {
					pkField=csvFiles[i].tableName;
					response.message+="\nDetected hierarchy_" + response.fields["geographyName"].toLowerCase() +
						" primary key: " + pkField + "; file: " + i + "; geolevel: " + csvFiles[i].geolevel;
				}
			}
			sqlStmt.sql+=")";
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Add primary key hierarchy_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
					"hierarchy_" + response.fields["geographyName"].toLowerCase() 	/* Table name */, 
					pkField 														/* Primary key */)); 
			sql.push(sqlStmt);			
					
			for (var i=0; i<csvFiles.length; i++) {	// Add non unique indexes
				if (csvFiles[i].geolevel != csvFiles.length && csvFiles[i].geolevel != 1) {		
					var sqlStmt=new Sql("Add index to: hierarchy_" + response.fields["geographyName"].toLowerCase(), 
						"CREATE INDEX hierarchy_" + response.fields["geographyName"].toLowerCase() + "_" + csvFiles[i].tableName +
						" ON  hierarchy_" + response.fields["geographyName"].toLowerCase() + "(" + csvFiles[i].tableName + ")");
					sql.push(sqlStmt);
				}
			}
			
			var sqlStmt=new Sql("Comment table: hierarchy_" + response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"hierarchy_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
					"Hierarchy lookup table for " + response.fields["geographyDesc"]	/* Comment */)
				);
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Create function check_hierarchy_" + 
					response.fields["geographyName"].toLowerCase(),		
				getSqlFromFile("check_hierarchy_function.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase() 			/* Geography */)
				);	
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Comment function check_hierarchy_" + 
					response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("check_hierarchy_function_comment.sql", 
					dbType, 
					"check_hierarchy_" + response.fields["geographyName"].toLowerCase() /* Function name */)
				);			
			sql.push(sqlStmt);			
			
			var sqlStmt=new Sql("Insert into hierarchy_" + response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("insert_hierarchy.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase()			/* Geography */)
				);
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Check intersctions  for geograpy: " + 
					response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("check_intersections.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase() 				/* Geography */)
				);
			sql.push(sqlStmt);
			
		} // End of createHierarchyTable()
		
		/*
		 * Function: 	createGeolevelsTable()
		 * Parameters:	None
		 * Description:	Create geolevels meta data table: geolevels_<geographyName> 
		 *				SQL statements
		 */	 
		function createGeolevelsTable() {
			sql.push(new Sql("Geolevels meta data"));	
			
			var sqlStmt=new Sql("Drop table geolevels_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, "geolevels_" + response.fields["geographyName"].toLowerCase() /* Table name */)); 
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Create geolevels meta data table",
				"CREATE TABLE geolevels_" + response.fields["geographyName"].toLowerCase() + " (\n" +
				"       geography                       VARCHAR(50)  NOT NULL,\n" +
				"       geolevel_name                   VARCHAR(30)  NOT NULL,\n" +
				"       geolevel_id			        	integer	     NOT NULL,\n" +		
				"       description                     VARCHAR(250) NOT NULL,\n" +
				"       lookup_table                    VARCHAR(30)  NOT NULL,\n" +
				"       lookup_desc_column              VARCHAR(30)  NOT NULL,\n" +		
				"       shapefile                       VARCHAR(512) NOT NULL,\n" +
				"       shapefile_table                 VARCHAR(30)  NULL,\n" +
				"       shapefile_area_id_column        VARCHAR(30)  NOT NULL,\n" +			
				"       shapefile_desc_column           VARCHAR(30)  NULL,\n" +
				"       resolution                      integer      NULL,\n" +			
				"       comparea                        integer      NULL,\n" +
				"       listing                         integer      NULL,\n" +			
				"       CONSTRAINT geolevels_" + response.fields["geographyName"].toLowerCase() + "_pk" + 
						" PRIMARY KEY(geography, geolevel_name)\n" +
				")");			
			sql.push(sqlStmt);	
			
			var sqlStmt=new Sql("Comment geolevels meta data table");			
			if (dbType == "PostGres") {		
				sqlStmt.sql="COMMENT ON TABLE geolevels_" + response.fields["geographyName"].toLowerCase() + 
					" IS 'Geolevels: hierarchy of level with a geography.'";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="DECLARE @CurrentUser sysname\n" +   
					"SELECT @CurrentUser = user_name();\n" +   
					"EXECUTE sp_addextendedproperty 'MS_Description',\n" +   
					"   'Geolevels: hierarchy of level with a geography.',\n" +   
					"   'user', @CurrentUser, \n" +   
					"   'table', 'geolevels_" + response.fields["geographyName"].toLowerCase() + "'";
			}
			sql.push(sqlStmt);
			
			var fieldArray = ['geography', 'geolevel_name', 'geolevel_id', 'description', 'lookup_table',
							  'lookup_desc_column', 'shapefile', 'shapefile_table', 'shapefile_area_id_column', 'shapefile_desc_column',
							  'resolution', 'comparea', 'listing'];
			var fieldDescArray = [
				'Geography (e.g EW2001)',
				'Name of geolevel. This will be a column name in the numerator/denominator tables',
				'ID for ordering (1=lowest resolution). Up to 99 supported.',
				'Description',
				'Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is "Westminster"',
				'Lookup table description column name.',
				'Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead',
				'Table containing GIS shape file data.',
				'Column containing the AREA_IDs in SHAPEFILE_TABLE',
				'Column containing the AREA_ID descriptions in SHAPEFILE_TABLE',
				'Can use a map for selection at this resolution (0/1)',
				'Able to be used as a comparison area (0/1)',
				'Able to be used in a disease map listing (0/1)'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment geography meta data column");	
				if (dbType == "PostGres") {		
					sqlStmt.sql="COMMENT ON COLUMN geolevels_" + response.fields["geographyName"].toLowerCase() + "." + fieldArray[l] +
						" IS '" + fieldDescArray[l] + "'";
				}
				else if (dbType == "MSSQLServer") {	
					sqlStmt.sql="DECLARE @CurrentUser sysname\n" +   
						"SELECT @CurrentUser = user_name();\n" +   
						"EXECUTE sp_addextendedproperty 'MS_Description',\n" +   
						"   '" + fieldDescArray[l] + "',\n" +   
						"   'user', @CurrentUser, \n" +   
						"   'table', 'geolevels_" + response.fields["geographyName"].toLowerCase() + "'," +   
						"   'column', '" + fieldArray[l] + "'";
				}
				sql.push(sqlStmt);			
			}		
			
			for (var i=0; i<csvFiles.length; i++) { // Main file process loop	
				var sqlStmt=new Sql("Insert geolevels meta data for: " + csvFiles[i].tableName, 
						"INSERT INTO geolevels_" + response.fields["geographyName"].toLowerCase() + "(\n" +
						"   geography, geolevel_name, geolevel_id, description, lookup_table,\n" +
						"   lookup_desc_column, shapefile, shapefile_table, shapefile_area_id_column, shapefile_desc_column,\n" + 
						"   resolution, comparea, listing)\n" +
						"SELECT '" + response.fields["geographyName"] + "' AS geography,\n" + 
						"       '" + csvFiles[i].tableName + "' AS geolevel_name,\n" +
						"       " + csvFiles[i].geolevel + " AS geolevel_id,\n" +
						"       '" + csvFiles[i].geolevelDescription + "' AS description,\n" + 
						"       'lookup_" + csvFiles[i].tableName + "' AS lookup_table,\n" +
						"       'areaname' AS lookup_desc_column,\n" +
						"       '" + csvFiles[i].file_name + "' AS shapefile,\n" +
						"       '" + csvFiles[i].tableName + "' AS shapefile_table,\n" +
						"       'areaid' AS shapefile_area_id_column,\n" +
						"       'areaname' AS shapefile_desc_column,\n" +
						"       1 AS resolution,\n" +
						"       1 AS comparea,\n" +
						"       1 AS listing");
				sql.push(sqlStmt);
			}				
		} // End of createGeolevelsTable()

		/*
		 * Function: 	createGeographyTable()
		 * Parameters:	None
		 * Description:	Create geography meta data table: geography_<geographyName> 
		 *				SQL statements
		 */			
		function createGeographyTable() {
			sql.push(new Sql("Geography meta data"));	
			
			var sqlStmt=new Sql("Drop table geography_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, 
					"geography_" + response.fields["geographyName"].toLowerCase() /* Table name */)); 
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Create geography meta data table",
				"CREATE TABLE geography_" + response.fields["geographyName"].toLowerCase() + " (\n" +
				"       geography               VARCHAR(50)  NOT NULL,\n" +
				"       description             VARCHAR(250) NOT NULL,\n" +  
				"       hierarchytable          VARCHAR(30)  NOT NULL,\n" + 
				"       srid                    integer      NULL DEFAULT 0,\n" + 
				"       defaultcomparea         VARCHAR(30)  NULL,\n" + 
				"       defaultstudyarea        VARCHAR(30)  NULL,\n" + 
				"       CONSTRAINT geography_" + response.fields["geographyName"].toLowerCase() + "_pk" + 
						" PRIMARY KEY(geography)\n" +
				")");			
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Populate geography meta data table", 
				"INSERT INTO geography_" + response.fields["geographyName"].toLowerCase() + " (\n" +
					"geography, description, hierarchytable, srid, defaultcomparea, defaultstudyarea)\n" + 
					"SELECT '" + response.fields["geographyName"] + "' AS geography,\n" +
					"       '" + response.fields["geographyDesc"] + "' AS description,\n" + 
					"       'hierarchy_" + response.fields["geographyName"].toLowerCase() + "' AS hierarchytable,\n" + 
					"       " + response.fields["srid"] + " AS srid,\n" + 
					"       '" + defaultcomparea + "' AS defaultcomparea,\n" + 
					"       '" + defaultstudyarea + "' AS defaultstudyarea");
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Comment geography meta data table");			
			if (dbType == "PostGres") {		
				sqlStmt.sql="COMMENT ON TABLE geography_" + response.fields["geographyName"].toLowerCase() + 
					" IS 'Hierarchial geographies. Usually based on Census geography.'";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="DECLARE @CurrentUser sysname\n" +   
	"SELECT @CurrentUser = user_name();\n" +   
	"EXECUTE sp_addextendedproperty 'MS_Description',\n" +   
	"   'Hierarchial geographies. Usually based on Census geography.',\n" +   
	"   'user', @CurrentUser, \n" +   
	"   'table', 'geography_" + response.fields["geographyName"].toLowerCase() + "'";
			}
			sql.push(sqlStmt);
			
			var fieldArray = ['geography', 'description', 'hierarchytable', 'srid', 'defaultcomparea', 'defaultstudyarea'];
			var fieldDescArray = ['Geography name', 
				'Description', 
				'Hierarchy table', 
				'Projection SRID', 
				'Default comparison area: lowest resolution geolevel', 
				'Default study area: highest resolution geolevel'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment geography meta data column");	
				if (dbType == "PostGres") {		
					sqlStmt.sql="COMMENT ON COLUMN geography_" + response.fields["geographyName"].toLowerCase() + "." + fieldArray[l] +
						" IS '" + fieldDescArray[l] + "'";
				}
				else if (dbType == "MSSQLServer") {	
					sqlStmt.sql="DECLARE @CurrentUser sysname\n" +   
		"SELECT @CurrentUser = user_name();\n" +   
		"EXECUTE sp_addextendedproperty 'MS_Description',\n" +   
		"   '" + fieldDescArray[l] + "',\n" +   
		"   'user', @CurrentUser, \n" +   
		"   'table', 'geography_" + response.fields["geographyName"].toLowerCase() + "'," +   
		"   'column', '" + fieldArray[l] + "'";
				}
				sql.push(sqlStmt);			
			}
		} // End of createGeographyTable()

		/*
		 * Function: 	createShapeFileTable()
		 * Parameters:	None
		 * Description:	Create shapefile derived data table: csvFiles[i].tableName 
		 *				SQL statements
		 */			
		function createShapeFileTable() {
			var sqlStmt;	

			var sqlStmt=new Sql("Drop table " + csvFiles[i].tableName, 
				getSqlFromFile("drop_table.sql", dbType, csvFiles[i].tableName /* Table name */)); 		
			sql.push(sqlStmt);
			
			var columnList=Object.keys(csvFiles[i].rows[0]);
			var sqlStmt=new Sql("Create table", "CREATE TABLE " + csvFiles[i].tableName + " (");
			for (var j=0; j<columnList.length; j++) {
				if (j > 0) {
					sqlStmt.sql+=",\n";
				}
				else {
					sqlStmt.sql+="\n";
				}
				if (columnList[j] == "GID") {
					sqlStmt.sql+="\t" + pad("                               ", columnList[j].toLowerCase(), false) + "\tinteger	NOT NULL";
				}
				else if (columnList[j] == "AREA_KM2") {
					sqlStmt.sql+="\t" + pad("                               ", columnList[j].toLowerCase(), false) + "\tnumeric";
				}
				else if (columnList[j] == "AREAID") {		
					if (dbType == "PostGres") {	
						sqlStmt.sql+="\t" + pad("                               ", 
							columnList[j].toLowerCase(), false) + "\ttext	NOT NULL";
					}
					else if (dbType == "MSSQLServer") {	
						sqlStmt.sql+="\t" + pad("                               ", 
							columnList[j].toLowerCase(), false) + "\tvarchar(100)	NOT NULL";					
					}
				}
				else if (columnList[j] == "AREANAME") {		
					if (dbType == "PostGres") {	
						sqlStmt.sql+="\t" + pad("                               ", 
							columnList[j].toLowerCase(), false) + "\ttext	NOT NULL";
					}
					else if (dbType == "MSSQLServer") {	
						sqlStmt.sql+="\t" + pad("                               ", 
							columnList[j].toLowerCase(), false) + "\tvarchar(1000)	NOT NULL";
					}
				}
				else {
					sqlStmt.sql+="\t" + pad("                               ", columnList[j].toLowerCase(), false) + "\ttext";
				}
			}
			sqlStmt.sql+=")";
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Comment table");
			if (dbType == "PostGres") {	
				sqlStmt.sql="COMMENT ON TABLE " + csvFiles[i].tableName + " IS '" + csvFiles[i].geolevelDescription + "'";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="DECLARE @CurrentUser sysname\n" +   
"SELECT @CurrentUser = user_name();\n" +   
"EXECUTE sp_addextendedproperty 'MS_Description',\n" +   
"   '" + csvFiles[i].geolevelDescription + "',\n" +   
"   'user', @CurrentUser, \n" +   
"   'table', '" + csvFiles[i].tableName + "'";
			}
			sql.push(sqlStmt);
			
			// Needs to be SQL to psql command (i.e. COPY FROM stdin)
			var sqlStmt=new Sql("Load table from CSV file");
			if (dbType == "PostGres") {	
				sqlStmt.sql="\\copy " + csvFiles[i].tableName + " FROM '" + csvFiles[i].tableName + 
					".csv' DELIMITER ',' CSV HEADER";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="BULK INSERT " + csvFiles[i].tableName + "\n" + 
"FROM '$(pwd)/" + csvFiles[i].tableName + ".csv'" + '	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line\n' + 
"WITH\n" + 
"(\n" + 
"	FORMATFILE = '$(pwd)/mssql_" + csvFiles[i].tableName + ".fmt',		-- Use a format file\n" +
"	TABLOCK					-- Table lock\n" + 
")";
			}
			sql.push(sqlStmt);
			
			var sqlStmt=new Sql("Row check: " + csvFiles[i].rows.length);
			if (dbType == "PostGres") {	
				sqlStmt.sql="DO LANGUAGE plpgsql $$\n" + 
"DECLARE\n" + 
"	c1 CURSOR FOR\n" + 
"		SELECT COUNT(gid) AS total\n" + 
"		  FROM " + csvFiles[i].tableName + ";\n" + 		
"	c1_rec RECORD;\n" + 
"BEGIN\n" + 
"	OPEN c1;\n" + 
"	FETCH c1 INTO c1_rec;\n" + 
"	CLOSE c1;\n" + 
"	IF c1_rec.total = " + csvFiles[i].rows.length + " THEN\n" + 
"		RAISE INFO 'Table: " + csvFiles[i].tableName + " row check OK: %', c1_rec.total;\n" + 
"	ELSE\n" + 
"		RAISE EXCEPTION 'Table: " + csvFiles[i].tableName + " row check FAILED: expected: " + 
		csvFiles[i].rows.length + " got: %', c1_rec.total;\n" + 
"	END IF;\n" + 
"END;\n" +
"$$";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="DECLARE c1 CURSOR FOR SELECT COUNT(gid) AS total FROM " + csvFiles[i].tableName + ";\n" + 
"DECLARE @c1_total AS int;\n" + 
"OPEN c1;\n" + 
"FETCH NEXT FROM c1 INTO @c1_total;\n" + 
"IF @c1_total = "+ csvFiles[i].rows.length + "\n" +  
"	PRINT 'Table: " + csvFiles[i].tableName + " row check OK: ' + CAST(@c1_total AS VARCHAR);\n" + 
"ELSE\n" + 
"	RAISERROR('Table: " + csvFiles[i].tableName + " row check FAILED: expected: " + 
				csvFiles[i].rows.length + " got: %i', 16, 1, @c1_total);\n" + 
"CLOSE c1;\n" + 
"DEALLOCATE c1";
			}
			sql.push(sqlStmt);	

			var sqlStmt=new Sql("Add primary key " + csvFiles[i].tableName, 
				getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
					csvFiles[i].tableName	/* Table name */, 
					'gid'					/* Primary key */)); 
			sql.push(sqlStmt);		
			
			var sqlStmt=new Sql("Add unique key");
			sqlStmt.sql="ALTER TABLE " + csvFiles[i].tableName + " ADD CONSTRAINT " + csvFiles[i].tableName + "_uk UNIQUE(areaid)";
			sql.push(sqlStmt);

			sql.push(new Sql("Add geometric  data"));			
			if (dbType == "PostGres") {				
				var sqlStmt=new Sql("Add geometry column: geographic centroid");
				sqlStmt.sql="SELECT AddGeometryColumn('" + csvFiles[i].tableName + "','geographic_centroid', 4326, 'POINT', 2, false)"; 
				sql.push(sqlStmt);
				
				var sqlStmt=new Sql("Update geometry column: geographic centroid");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" + 
	"   SET geographic_centroid = ST_GeomFromText(geographic_centroid_wkt, 4326)";
				sql.push(sqlStmt);
	
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					var sqlStmt=new Sql("Add geometry column for zoomlevel: " + k);
					sqlStmt.sql="SELECT AddGeometryColumn('" + csvFiles[i].tableName + "','geom_" + k + 
						"', 4326, 'MULTIPOLYGON', 2, false)";
					sql.push(sqlStmt);
				}
				
				var sqlStmt=new Sql("Add geometry column for original SRID geometry");
				sqlStmt.sql="SELECT AddGeometryColumn('" + csvFiles[i].tableName + "','geom_orig', 4269, 'MULTIPOLYGON', 2, false)";
				sql.push(sqlStmt);
				
				var sqlStmt=new Sql("Update geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n   SET ";
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					sqlStmt.sql+="" +
	"\tgeom_" + k + " = \n" +
	"\t\tCASE ST_IsCollection(ST_GeomFromText(wkt_" + k + ", 4326)) /* Convert to Multipolygon */\n" +
	"\t\t\tWHEN true THEN 	ST_GeomFromText(wkt_" + k + ", 4326)\n" +
	"\t\t\tELSE 			ST_Multi(ST_GeomFromText(wkt_" + k + ", 4326))\n" +
	"\t\tEND,\n";
				}
				sqlStmt.sql+="" +
	"\tgeom_orig = ST_Transform(\n" +
	"\t\tCASE ST_IsCollection(ST_GeomFromText(wkt_" + response.fields["max_zoomlevel"] + ", 4326)) /* Convert to Multipolygon */\n" +
	"\t\t\tWHEN true THEN 	ST_GeomFromText(wkt_" + response.fields["max_zoomlevel"] + ", 4326)\n" +
	"\t\t\tELSE 			ST_Multi(ST_GeomFromText(wkt_" + response.fields["max_zoomlevel"] + ", 4326))\n" +
	"\t\tEND, " + response.fields["srid"] + ")";
				sql.push(sqlStmt);
				
				var sqlStmt=new Sql("Make geometry columns valid");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n   SET ";
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					sqlStmt.sql+="" +		
	"\tgeom_" + k + " = CASE ST_IsValid(geom_" + k + ")\n" + 
"			WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_" + k + "), 3 /* Remove non polygons */)\n" +
"			ELSE geom_" + k + "\n" +
"		END,\n";	
				}
				sqlStmt.sql+="" +
	"\tgeom_orig = CASE ST_IsValid(geom_orig)\n" +
"			WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_orig), 3 /* Remove non polygons */)\n" +
"			ELSE geom_orig\n" +
"		END";	
				sql.push(sqlStmt);
						
			}
			else if (dbType == "MSSQLServer") {					
				var sqlStmt=new Sql("Add geometry column: geographic centroid");
				sqlStmt.sql="ALTER TABLE " + csvFiles[i].tableName + " ADD geographic_centroid geography"; 
				sql.push(sqlStmt);	
				
				var sqlStmt=new Sql("Update geometry column: geographic centroid");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" + 
	"   SET geographic_centroid = geography::STGeomFromText(geographic_centroid_wkt, 4326)";
				sql.push(sqlStmt);	
				
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					var sqlStmt=new Sql("Add geometry column for zoomlevel: " + k);
					sqlStmt.sql="ALTER TABLE " + csvFiles[i].tableName + " ADD geom_" + k + " geography"; 
					sql.push(sqlStmt);
				}
				
				var sqlStmt=new Sql("Add geometry column for original SRID geometry");
				sqlStmt.sql="ALTER TABLE " + csvFiles[i].tableName + " ADD geom_orig geography";
				sql.push(sqlStmt);
				
				var sqlStmt=new Sql("Update geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n   SET ";
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					sqlStmt.sql+="\tgeom_" + k + " = geography::STGeomFromText(wkt_" + k + ", 4326).MakeValid(),\n";
				}	

// Needs codeplex SQL Server Spatial Tools:  http://sqlspatialtools.codeplex.com/wikipage?title=Current%20Contents&referringTitle=Home				
				sqlStmt.sql+="" +
	"\tgeom_orig = /* geography::STTransform(geography::STGeomFromText(wkt_" + response.fields["max_zoomlevel"] + ", 4326).MakeValid(), " + 
					response.fields["srid"] + ") NOT POSSIBLE */ NULL"; 
				sql.push(sqlStmt);
			}

			sql.push(new Sql("Test geometry and make valid if required"));
			
			var sqlStmt=new Sql("Check validity of geometry columns");
			var selectFrag=undefined;
			for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
				var sqlFrag=undefined;
				if (dbType == "PostGres") {		
					sqlFrag="SELECT areaname,\n" +
"       " + k + "::Text AS geolevel,\n" +					
"       ST_IsValidReason(geom_" + k + ") AS reason\n" +
"  FROM " + csvFiles[i].tableName + "\n" +
" WHERE NOT ST_IsValid(geom_" + k + ")\n";
				}
				else if (dbType == "MSSQLServer") {		
					sqlFrag="SELECT areaname,\n" +
"       " + k + " AS geolevel,\n" +					
"       geom_" + k + ".IsValidDetailed() AS reason\n" +
"  FROM " + csvFiles[i].tableName + "\n" +
" WHERE geom_" + k + ".STIsValid() = 0\n";
				}
				if (selectFrag) {
					selectFrag+="UNION\n" + sqlFrag;
				}
				else {	
					selectFrag=sqlFrag;
				}
			}		
			if (dbType == "PostGres") {		
				selectFrag+="UNION\n" +
"SELECT areaname,\n" +
"       'geom_orig'::Text AS geolevel,\n" +					
"       ST_IsValidReason(geom_orig) AS reason\n" +
"  FROM " + csvFiles[i].tableName + "\n" +
" WHERE NOT ST_IsValid(geom_orig)\n" +			
" ORDER BY 1, 2;\n";
			}
			else if (dbType == "MSSQLServer") {			
				selectFrag+="ORDER BY 1, 2;\n";
			}
			
			if (dbType == "PostGres") {
				sqlStmt.sql="DO LANGUAGE plpgsql $$\n" + 
"DECLARE\n" + 
"	c1 CURSOR FOR\n" + selectFrag + 		
"	c1_rec RECORD;\n" + 
"	total INTEGER:=0;\n" +
"BEGIN\n" +  
"	FOR c1_rec IN c1 LOOP\n" + 
"		total:=total+1;\n" +
"		RAISE INFO 'Area: %, geolevel: %: %', c1_rec.areaname, c1_rec.geolevel, c1_rec.reason;\n" + 
"	END LOOP;\n" + 
"	IF total = 0 THEN\n" + 
"		RAISE INFO 'Table: " + csvFiles[i].tableName + " no invalid geometry check OK';\n" + 
"	ELSE\n" + 
"		RAISE EXCEPTION 'Table: " + csvFiles[i].tableName + " no invalid geometry check FAILED: % invalid', total;\n" + 
"	END IF;\n" + 
"END;\n" +
"$$";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="DECLARE c1 CURSOR FOR\n" + selectFrag +
"DECLARE @areaname AS VARCHAR(30);\n" +
"DECLARE @geolevel AS int;\n" +
"DECLARE @reason AS VARCHAR(90);\n" +
"DECLARE @nrows AS int;\n" +
"SET @nrows=0;\n" +
"OPEN c1;\n" +
"FETCH NEXT FROM c1 INTO @areaname, @geolevel, @reason;\n" +
"WHILE @@FETCH_STATUS = 0\n" +
"BEGIN\n" +
"		SET @nrows+=1;\n" +
"	    PRINT 'Area: ' + @areaname + ', geolevel: ' + CAST(@geolevel AS VARCHAR) + ': ' +RTRIM(@reason);\n" +
"       FETCH NEXT FROM c1 INTO @areaname, @geolevel, @reason;\n" +   
"END\n" +
"IF @nrows = 0\n" +
"	PRINT 'Table: " + csvFiles[i].tableName + " no invalid geometry check OK';\n" +
"ELSE\n" +
"	RAISERROR('Table: " + csvFiles[i].tableName + " no invalid geometry check FAILED: %i invalid', 16, 1, @nrows);\n" +
"CLOSE c1;\n" +
"DEALLOCATE c1";	
			}		
			sql.push(sqlStmt);		

//
// In SQL server, all polygons must have right hand orientation or bad things happen - like the area ~ one hemisphere
// as used to detect the problem
//
			sql.push(new Sql("Make all polygons right handed"));
			for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
				var sqlStmt=new Sql("Make all polygons right handed for zoomlevel: " + k);
				if (dbType == "MSSQLServer") {		
					sqlStmt.sql="WITH a AS (\n" +
"	SELECT gid, geom_" + k + ",\n" +
"		   CAST(area_km2 AS NUMERIC(21,6)) AS area_km2,\n" +
"		   CAST((geom_" + k + ".STArea()/(1000*1000)) AS NUMERIC(21,6)) AS area_km2_calc\n" +
"	  FROM " + csvFiles[i].tableName + "\n" +
"), b AS (\n" +
"	SELECT a.gid,\n" + 
"	       a.geom_" + k + ",\n" +
"          a.area_km2,\n" +
"	       a.area_km2_calc,\n" +
"          CASE WHEN a.area_km2 > 0 THEN CAST(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2) AS NUMERIC(21,6))\n" +
"				WHEN a.area_km2 = a.area_km2_calc THEN 0\n" +
"	        	ELSE NULL\n" +
"	   	   END AS pct_km2_diff \n" +
"  FROM a\n" +
")\n" +
"UPDATE " + csvFiles[i].tableName + "\n" +
"   SET geom_" + k + " = c.geom_" + k + ".ReorientObject()\n" +
"  FROM " + csvFiles[i].tableName + " c\n" +
" JOIN b ON b.gid = c.gid\n" +
" WHERE b.pct_km2_diff > 200 /* Threshold test */";
				}
				else if (dbType == "PostGres") {	
					sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" +
"   SET geom_" + k + " = ST_ForceRHR(geom_" + k + ")";
				}
				sql.push(sqlStmt);
			}
			if (dbType == "PostGres") { // No geom_orig in SQL Server
				var sqlStmt=new Sql("Make all polygons right handed for original geometry", 
"UPDATE " + csvFiles[i].tableName + "\n" +
"   SET geom_orig = ST_ForceRHR(geom_orig)");
				sql.push(sqlStmt);
			}
			
			sql.push(new Sql("Test Turf and DB areas agree to within 1%"));
			
			var sqlStmt=new Sql("Test Turf and DB areas agree to within 1%");
			if (dbType == "PostGres") { // No geom_orig in SQL Server
				sqlStmt.sql="DO LANGUAGE plpgsql $$\n" +
"DECLARE c1 CURSOR FOR\n" +
"	WITH a AS (\n" +
"		SELECT areaname,\n" + 
"			   area_km2 AS area_km2,\n" +
"			   ST_Area(geography(geom_" + response.fields["max_zoomlevel"] + "))/(1000*1000) AS area_km2_calc\n" +
"		  FROM " + csvFiles[i].tableName + "\n" +
"	), b AS (\n" +
"	SELECT SUBSTRING(a.areaname, 1, 30) AS areaname,\n" +
"		   a.area_km2,\n" +
"		   a.area_km2_calc,\n" +
"		   CASE WHEN a.area_km2 > 0 THEN 100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2)\n" +
"				WHEN a.area_km2 = a.area_km2_calc THEN 0\n" +
"				ELSE NULL\n" +
"		   END AS pct_km2_diff \n" +
"	  FROM a\n" +
"	)\n" +
"	SELECT b.areaname, b.area_km2, b.area_km2_calc, b.pct_km2_diff\n" +
"	  FROM b\n" +
"	 WHERE b.pct_km2_diff > 1 /* Allow for 1% error */\n" +
"	   AND b.area_km2_calc > 10 /* Ignore small areas <= 10 km2 */;\n" +
"\n" +
"	c1_rec RECORD;\n" +
"	total INTEGER:=0;\n" +
"BEGIN\n" +
"	FOR c1_rec IN c1 LOOP\n" +
"		total:=total+1;\n" +
"		RAISE INFO 'Area: %, area km2: %:, calc: %, diff %',\n" +
"			c1_rec.areaname, c1_rec.area_km2, c1_rec.area_km2_calc, c1_rec.pct_km2_diff;\n" +
"	END LOOP;\n" +
"	IF total = 0 THEN\n" +
"		RAISE INFO 'Table: " + csvFiles[i].tableName + " no invalid areas check OK';\n" +
"	ELSE\n" +
"		RAISE EXCEPTION 'Table: " + csvFiles[i].tableName + " no invalid areas check FAILED: % invalid', total;\n" +
"	END IF;\n" +
"END;\n" +
"$$";
			}
			else if (dbType == "MSSQLServer") {
sqlStmt.sql="DECLARE c1 CURSOR FOR\n" +
"	WITH a AS (\n" +
"		SELECT areaname,\n" +
"			   CAST(area_km2 AS NUMERIC(15,2)) AS area_km2,\n" +
"			   CAST((geom_" + response.fields["max_zoomlevel"] + ".STArea()/(1000*1000)) AS NUMERIC(15,2)) AS area_km2_calc\n" +
"		  FROM " + csvFiles[i].tableName + "\n" +
"	), b AS (\n" +
"	SELECT SUBSTRING(a.areaname, 1, 30) AS areaname,\n" +
"		   a.area_km2,\n" +
"		   a.area_km2_calc,\n" +
"		   CASE WHEN a.area_km2 > 0 THEN CAST(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2) AS NUMERIC(15,2))\n" +
"				WHEN a.area_km2 = a.area_km2_calc THEN 0\n" +
"				ELSE NULL\n" +
"		   END AS pct_km2_diff\n" +
"	  FROM a\n" +
"	)\n" +
"	SELECT b.areaname, b.area_km2, b.area_km2_calc, b.pct_km2_diff\n" +
"	  FROM b\n" +
"	 WHERE b.pct_km2_diff > 5 /* Allow for 5% error */\n" +
"	   AND b.area_km2_calc > 10 /* Ignore small areas <= 10 km2 */;\n" +
"DECLARE @areaname AS VARCHAR(30);\n" +
"DECLARE @area_km2 AS NUMERIC(15,2);\n" +
"DECLARE @area_km2_calc AS NUMERIC(15,2);\n" +
"DECLARE @pct_km2_diff AS NUMERIC(15,2);\n" +
"DECLARE @nrows AS int;\n" +
"SET @nrows=0;\n" +
"OPEN c1;\n" +
"FETCH NEXT FROM c1 INTO @areaname, @area_km2, @area_km2_calc, @pct_km2_diff;\n" +
"WHILE @@FETCH_STATUS = 0\n" +
"BEGIN\n" +
"		SET @nrows+=1;\n" +
"		PRINT 'Area: ' + @areaname + ', area km2: ' + CAST(@area_km2 AS VARCHAR) +  + ', calc: ' +\n" +
"			CAST(@area_km2_calc AS VARCHAR) + ', diff: ' + CAST(@pct_km2_diff AS VARCHAR);\n" +
"		FETCH NEXT FROM c1 INTO @areaname, @area_km2, @area_km2_calc, @pct_km2_diff;\n" +
"END\n" +
"IF @nrows = 0\n" +
"	PRINT 'Table: " + csvFiles[i].tableName + " no invalid areas check OK';\n" +
"ELSE\n" +
"	RAISERROR('Table: " + csvFiles[i].tableName + " no invalid areas check FAILED: %i invalid', 16, 1, @nrows);\n" +
"CLOSE c1;\n" +
"DEALLOCATE c1";
			}
			sql.push(sqlStmt);
		
			sql.push(new Sql("Create spatial indexes"));
			for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
				var sqlStmt=new Sql("Index geometry column for zoomlevel: " + k);
				if (dbType == "PostGres") {		
					sqlStmt.sql="CREATE INDEX " + csvFiles[i].tableName + "_geom_" + k + "_gix ON " + csvFiles[i].tableName + 
						" USING GIST (geom_" + k + ")";
				}
				else if (dbType == "MSSQLServer") {	
					sqlStmt.sql="CREATE SPATIAL INDEX " + csvFiles[i].tableName + "_geom_" + k + "_gix ON " + 
						csvFiles[i].tableName + " (geom_" + k + ")";
				}
				sql.push(sqlStmt);
			}			
			if (dbType == "PostGres") {		
				var sqlStmt=new Sql("Index geometry column for original SRID geometry",
					"CREATE INDEX " + csvFiles[i].tableName + "_geom_orig_gix ON " + csvFiles[i].tableName + 
					" USING GIST (geom_orig)");					
				sql.push(sqlStmt);
			}
			else if (dbType == "MSSQLServer") {	
				var sqlStmt=new Sql("Index geometry column for original SRID geometry",
					"CREATE SPATIAL INDEX " + csvFiles[i].tableName + "_geom_orig_gix ON " + csvFiles[i].tableName + 
					" (geom_orig)");
				sql.push(sqlStmt);
			}

			sql.push(new Sql("Reports"));	
			var sqlStmt=new Sql("Areas and centroids");			
			if (dbType == "PostGres") {
				sqlStmt.sql="WITH a AS (\n" +
"	SELECT areaname,\n" +
"		   ROUND(area_km2::numeric, 2) AS area_km2,\n" +
"		   ROUND(\n" +
"				(ST_Area(geography(geom_" + response.fields["max_zoomlevel"] + "))/(1000*1000))::numeric, 2) AS area_km2_calc,\n" +
"		   ROUND(ST_X(geographic_centroid)::numeric, 4)||','||ROUND(ST_Y(geographic_centroid)::numeric, 4) AS geographic_centroid,\n" +
"		   ROUND(ST_X(ST_Centroid(geom_" + response.fields["max_zoomlevel"] + 
				"))::numeric, 4)||','||ROUND(ST_Y(ST_Centroid(geom_" + 
				response.fields["max_zoomlevel"] + "))::numeric, 4) AS geographic_centroid_calc,\n" +
"		   ROUND(ST_Distance_Sphere(ST_Centroid(geom_" + response.fields["max_zoomlevel"] + 
				"), geographic_centroid)::numeric/1000, 2) AS centroid_diff_km\n" +
"	  FROM " + csvFiles[i].tableName + "\n" +
"	 GROUP BY areaname, area_km2, geom_" + response.fields["max_zoomlevel"] + ", geographic_centroid\n" +
")\n" +
"SELECT a.areaname,\n" + 
"       a.area_km2,\n" + 
"	   a.area_km2_calc,\n" + 
"	   ROUND(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2_calc), 2) AS pct_km2_diff,\n" +
"	   a.geographic_centroid,\n" + 
"      a.geographic_centroid_calc,\n" +
"	   a.centroid_diff_km\n" +
"  FROM a\n" +
" ORDER BY 1\n" +
" LIMIT 100";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="WITH a AS (\n" +
"	SELECT areaname, geom_" + response.fields["max_zoomlevel"] + ",\n" +
"		   CAST(area_km2 AS NUMERIC(15,2)) AS area_km2,\n" +
"		   CAST((geom_" + response.fields["max_zoomlevel"] + ".STArea()/(1000*1000)) AS NUMERIC(15,2)) AS area_km2_calc,\n" +	  
"		   CONCAT(\n" +
"				CAST(CAST(geographic_centroid.Long AS NUMERIC(15,7)) AS VARCHAR(30)),\n" +
"				',',\n" +
"				CAST(CAST(geographic_centroid.Lat AS NUMERIC(15,7)) AS VARCHAR(30))\n" +
"				) AS geographic_centroid,\n" +
"		   CONCAT(\n" +
"				CAST(CAST(geom_" + response.fields["max_zoomlevel"] + ".EnvelopeCenter().Long AS NUMERIC(15,7)) AS VARCHAR(30)),\n" +
"				',',\n" +
"				CAST(CAST(geom_" + response.fields["max_zoomlevel"] + ".EnvelopeCenter().Lat AS NUMERIC(15,7)) AS VARCHAR(30))\n" +
"				) AS geographic_centroid_calc,\n" +
"		   CAST((geom_" + response.fields["max_zoomlevel"] + ".EnvelopeCenter().STDistance(geographic_centroid))/1000 AS VARCHAR(30)) AS centroid_diff_km,\n" +		
"		   ROW_NUMBER() OVER (ORDER BY areaname) as nrow\n" +
"	  FROM " + csvFiles[i].tableName + "\n" +
")\n" +
"SELECT SUBSTRING(a.areaname, 1, 30) AS areaname,\n" +
"       a.area_km2,\n" +
"	   a.area_km2_calc,\n" +
"	   CAST(100*(ABS(a.area_km2 - a.area_km2_calc)/area_km2) AS NUMERIC(15,2)) AS pct_km2_diff,\n" +
"	   a.geographic_centroid,\n" +
"       a.geographic_centroid_calc,\n" +
"	   a.centroid_diff_km\n" +
"  FROM a\n" +
" WHERE nrow <= 100\n" +
" ORDER BY 1"; 
			}
			sql.push(sqlStmt);
			
			// Set default satudy and comparison areas
			if (csvFiles[i].geolevel == 1) {
				defaultcomparea=response.fields[csvFiles[i].file_name_no_ext + "_areaID"]; // E.g. cb_2014_us_nation_5m_areaID
			}
			else if (csvFiles[i].geolevel == (csvFiles.length-1)) {
				defaultstudyarea=response.fields[csvFiles[i].file_name_no_ext + "_areaID"]; // E.g. cb_2014_us_county_500k_areaID
			}
//			else {
//				console.error("No match for geolevel: " + csvFiles[i].geolevel);
//			}			
		} // End of createShapeFileTable()
		
		function Sql(comment, sql) { // Object constructor
			this.comment=comment;
			this.sql=sql;	
			this.nonsql=undefined;	
			this.dbStream=dbType;			
		}
		var sql=[];
		
		beginTransaction();
		
		var defaultcomparea;
		var defaultstudyarea;
	
		for (var i=0; i<csvFiles.length; i++) { // Main file processing loop
			createShapeFileTable();
		} // End of for csvFiles loop
		
		// Check defaultcomparea and defaultstudyarea are defined by createShapeFileTable()
		if (defaultcomparea == undefined) {
			throw new Error("Unable to determine default comparison area");
		}
		if (defaultstudyarea == undefined) {
			throw new Error("Unable to determine default study area");
		}
		
		createGeographyTable();
		createGeolevelsTable();
		createGeolevelsLookupTables();
		createHierarchyTable();
		
		commitTransaction();
		
		analyzeTables();
		
		for (var i=0; i<sql.length; i++) {
			if (sql[i].sql == undefined && sql[i].nonsql == undefined) { // Comment			
				dbStream.write("\n--\n-- " + sql[i].comment + "\n--\n");
			}
			else if (sql[i].sql != undefined && dbType == "PostGres") {				
				dbStream.write("\n-- SQL statement " + i + ": " + sql[i].comment + " >>>\n" + sql[i].sql + ";\n");
			}
			else if (sql[i].sql != undefined && dbType == "MSSQLServer") {				
				dbStream.write("\n-- SQL statement " + i + ": " + sql[i].comment + " >>>\n" + sql[i].sql + ";\nGO\n");
			}
			else if (sql[i].nonsql != undefined && dbType == "PostGres") {				
				dbStream.write("\n-- PSQL statement " + i + ": " + sql[i].comment + " >>>\n" + sql[i].nonsql + "\n");
			}
			else if (sql[i].nonsql != undefined && dbType == "MSSQLServer") {				
				dbStream.write("\n-- SQLCMD statement " + i + ": " + sql[i].comment + " >>>\n" + sql[i].nonsql + "\n");
			}
		}
	} // End of addSQLStatements()

	/*
	 * Function: 	createSqlServerFmtFiles()
	 * Parameters:	Directory to create in, CSV files object
	 * Description:	Create MS SQL Server bulk load format files
	 *				The insistence on quotes excludes the header row
	 *
	 * Exammple file format:
	 
<?xml version="1.0"?>
<!-- MS SQL Server bulk load format files
	 The insistence on quotes excludes the header row -->
<BCPFORMAT xmlns="http://schemas.microsoft.com/sqlserver/2004/bulkload/format"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 <RECORD>
  <FIELD ID="0" xsi:type="CharTerm" TERMINATOR='"' />
   <FIELD ID="1" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="2" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="3" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="4" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="5" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="6" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="7" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="8" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="9" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="10" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="11" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="12" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="13" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="14" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="15" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="16" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="17" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="18" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="19" xsi:type="CharTerm" TERMINATOR='","' />
   <FIELD ID="20" xsi:type="CharTerm" TERMINATOR='"\r\n' />
 </RECORD>
 <ROW>
   <COLUMN SOURCE="1" NAME="statefp" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="2" NAME="countyfp" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="3" NAME="countyns" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="4" NAME="affgeoid" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="5" NAME="geoid" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="6" NAME="name" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="7" NAME="lsad" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="8" NAME="aland" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="9" NAME="awater" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="10" NAME="gid" xsi:type="SQLINT" />
   <COLUMN SOURCE="11" NAME="areaid" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="12" NAME="areaname" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="13" NAME="area_km2" xsi:type="SQLNUMERIC" />
   <COLUMN SOURCE="14" NAME="geographic_centroid_wkt" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="15" NAME="wkt_11" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="16" NAME="wkt_10" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="17" NAME="wkt_9" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="18" NAME="wkt_8" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="19" NAME="wkt_7" xsi:type="SQLVARYCHAR" />
   <COLUMN SOURCE="20" NAME="wkt_6" xsi:type="SQLVARYCHAR" />
 </ROW>
</BCPFORMAT>	 
	 
	 */	 
	var createSqlServerFmtFiles=function createSqlServerFmtFiles(dir, csvFiles) {	
		for (var i=0; i<csvFiles.length; i++) {
			var fmtScriptName="mssql_" + csvFiles[i].tableName + ".fmt";
			var fmtStream = fs.createWriteStream(dir + "/" + fmtScriptName, { flags : 'w' });	
			fmtStream.on('finish', function fmtStreamClose() {
				response.message+="\nstreamClose() MS SQL Server bulk load format file";
			});		
			fmtStream.on('error', function fmtStreamError(e) {
				serverLog.serverLog2(__file, __line, dbType + "StreamError", 
					"WARNING: Exception in MS SQL Server bulk load format file write: " + fmtScriptName, req, e, response);										
			});
			
			var fmtBuf='<?xml version="1.0"?>\n' +
			'<!-- MS SQL Server bulk load format files\n' +
'	 The insistence on quotes excludes the header row -->\n' +
'<BCPFORMAT xmlns="http://schemas.microsoft.com/sqlserver/2004/bulkload/format"\n' +
'  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n' +
' <RECORD>\n' + 
'   <FIELD ID="0" xsi:type="CharTerm" TERMINATOR=' + "'" + '"' + "' />\n";
			var columnList=Object.keys(csvFiles[i].rows[0]);
			
			for (var j=1; j<=columnList.length; j++) {
				if (j<columnList.length) {
					fmtBuf+='   <FIELD ID="' + j + '" xsi:type="CharTerm" TERMINATOR=' + "'" + '","' + "' />\n";
				}
				else {
					fmtBuf+='   <FIELD ID="' + j + '" xsi:type="CharTerm" TERMINATOR=' + "'" + '"\\r\\n' + "' />\n";
				}
			}
			fmtBuf+=' </RECORD>\n'; 
			fmtBuf+=' <ROW>\n'; 
			for (var j=1; j<=columnList.length; j++) {
				var bcpDtype="SQLVARYCHAR";
				var column=columnList[(j-1)].toLowerCase();
				if (column == "gid") {
					bcpDtype="SQLINT"; // Integer
				}
				else if (column == "area_km2") {
					bcpDtype="SQLNUMERIC"; // Numeric
				}
				fmtBuf+='   <COLUMN SOURCE="' + j + '" NAME="' + column + '" xsi:type="' + bcpDtype + '" />\n';
			}			
			fmtBuf+=' </ROW>\n'; 
			fmtBuf+='</BCPFORMAT>\n'; 
			fmtStream.write(fmtBuf);
			fmtStream.end();
		} // End of for csvFiles
	} // End of createSqlServerFmtFiles()
	
	var pgScript="pg_" + response.fields["geographyName"] + ".sql"
	var mssqlScript="mssql_" + response.fields["geographyName"] + ".sql"
	
	var pgStream=createSQLScriptHeader(dir + "/" + pgScript, "PostGres");
	var mssqlStream=createSQLScriptHeader(dir + "/" + mssqlScript, "MSSQLServer");
	
	addSQLStatements(pgStream, csvFiles, response.fields["srid"], "PostGres");
	addSQLStatements(mssqlStream, csvFiles, response.fields["srid"], "MSSQLServer");
	createSqlServerFmtFiles(dir, csvFiles);
	
	var endStr="\n\n--\n-- EOF\n";
	pgStream.write(endStr);
	mssqlStream.write(endStr);
	
	pgStream.end();
	mssqlStream.end();	
	
	var msg="Created database load scripts: " + pgScript + " and " + mssqlScript;
	response.message+="\n" + msg;
									
	addStatus(__file, __line, response, msg,   // Add created WKT zoomlevel topojson status	
		200 /* HTTP OK */, serverLog, undefined /* req */,
		/*
		 * Function: 	createGeoJSONFromTopoJSON()
		 * Parameters:	error object
		 * Description:	Add status callback
		 */												
		function CreateDbLoadScriptsAddStatus(err) {
			if (err) {
				serverLog.serverLog2(__file, __line, "CreateDbLoadScriptsAddStatus", 
					"WARNING: Unable to add dbLoad file processing status", req, err);
			}
			endCallback(err);
		});
} // End of CreateDbLoadScripts()

module.exports.CreateDbLoadScripts = CreateDbLoadScripts;
module.exports.getSqlFromFile = getSqlFromFile;

// Eof