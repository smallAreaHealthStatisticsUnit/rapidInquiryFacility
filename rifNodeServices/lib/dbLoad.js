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
			var sqlStmt=new Sql("Start transaction", "BEGIN TRANSACTION");				
			sqlArray.push(sqlStmt);
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
			sqlArray.push(sqlStmt);
		} // End of commitTransaction()	
		
		/*
		 * Function: 	analyzeTables()
		 * Parameters:	None
		 * Description:	Analze and describe all tables: SQL statements
		 *				Needs to be in a separate transaction (do NOT start one!)
		 */	 	
		function analyzeTables() {
			sqlArray.push(new Sql("Analyze tables"));
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
				sqlArray.push(sqlStmt);
				var sqlStmt=new Sql("Analyze table " + tableList[i], 
					getSqlFromFile("analyze_table.sql", dbType, tableList[i] /* Table name */)); 
				sqlArray.push(sqlStmt);
			} // End of for csvFiles loop			
		} // End of analyzeTables()		 

		/*
		 * Function: 	createGeolevelsLookupTables()
		 * Parameters:	None
		 * Description:	Create geoelvels lookup tables: SQL statements
		 */	 
		function createGeolevelsLookupTables() {
			sqlArray.push(new Sql("Geolevels lookup tables"));
			for (var i=0; i<csvFiles.length; i++) {									
				var sqlStmt=new Sql("Drop table lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("drop_table.sql", dbType, "lookup_" + csvFiles[i].tableName /* Table name */)); 
				sqlArray.push(sqlStmt);	
				
				var sqlStmt=new Sql("Create table lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("create_lookup_table.sql", undefined /* Common */, csvFiles[i].tableName /* Table name */)); 
				sqlArray.push(sqlStmt);				
				
				var sqlStmt=new Sql("Insert table lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("insert_lookup_table.sql", undefined /* Common */, csvFiles[i].tableName /* Table name */)); 
				sqlArray.push(sqlStmt);			
				
				var sqlStmt=new Sql("Add primary key lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
						"lookup_" + csvFiles[i].tableName 	/* Table name */, 
						csvFiles[i].tableName 				/* Primary key */)); 
				sqlArray.push(sqlStmt);

				var sqlStmt=new Sql("Comment table lookup_" + csvFiles[i].tableName,
					getSqlFromFile("comment_table.sql", 
						dbType, 
						"lookup_" + csvFiles[i].tableName,		/* Table name */
						"Lookup table for " + csvFiles[i].geolevelDescription 	/* Comment */)
					);
				sqlArray.push(sqlStmt);			
			}				
		} // End of createGeolevelsLookupTables()
		
		/*
		 * Function: 	analyzeTables()
		 * Parameters:	None
		 * Description:	Create hierarchy table: SQL statements
		 */	 
		function createHierarchyTable() {
			sqlArray.push(new Sql("Hierarchy table"));	
			
			var sqlStmt=new Sql("Drop table hierarchy_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, "hierarchy_" + response.fields["geographyName"].toLowerCase() /* Table name */)); 
			sqlArray.push(sqlStmt);
			
			var sqlStmt=new Sql("Create table hierarchy_" + response.fields["geographyName"].toLowerCase());
			sqlStmt.sql="CREATE TABLE hierarchy_" + response.fields["geographyName"].toLowerCase() + " (\n";
			var pkField=undefined;
			for (var i=0; i<csvFiles.length; i++) {	
				if (i == 0) {
					sqlStmt.sql+="	" + csvFiles[i].tableName + "	VARCHAR(100)  NOT NULL";
				}
				else {
					sqlStmt.sql+=",\n	" + csvFiles[i].tableName + "	VARCHAR(100)  NOT NULL";
				}
			
				if (csvFiles[i].geolevel == csvFiles.length && pkField == undefined) {
					pkField=csvFiles[i].tableName;
					response.message+="\nDetected hierarchy_" + response.fields["geographyName"].toLowerCase() +
						" primary key: " + pkField + "; file: " + i + "; geolevel: " + csvFiles[i].geolevel;
				}
			}
			sqlStmt.sql+=")";
			sqlArray.push(sqlStmt);
			
			var sqlStmt=new Sql("Add primary key hierarchy_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
					"hierarchy_" + response.fields["geographyName"].toLowerCase() 	/* Table name */, 
					pkField 														/* Primary key */)); 
			sqlArray.push(sqlStmt);			
					
			for (var i=0; i<csvFiles.length; i++) {	// Add non unique indexes
				if (csvFiles[i].geolevel != csvFiles.length && csvFiles[i].geolevel != 1) {	
					var sqlStmt=new Sql("Add index key hierarchy_" + 
						response.fields["geographyName"].toLowerCase() + "_" + csvFiles[i].tableName, 
						getSqlFromFile("create_index.sql", undefined /* Common */, 
							"hierarchy_" + response.fields["geographyName"].toLowerCase() + "_" + csvFiles[i].tableName	/* Index name */,
							"hierarchy_" + response.fields["geographyName"].toLowerCase() 								/* Table name */, 
							csvFiles[i].tableName 																		/* Index column(s) */
						)); 
					sqlArray.push(sqlStmt);	
				}
			}
			
			var sqlStmt=new Sql("Comment table: hierarchy_" + response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"hierarchy_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
					"Hierarchy lookup table for " + response.fields["geographyDesc"]	/* Comment */)
				);
			sqlArray.push(sqlStmt);
			
			var sqlStmt=new Sql("Create function check_hierarchy_" + 
					response.fields["geographyName"].toLowerCase(),		
				getSqlFromFile("check_hierarchy_function.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase() 			/* Geography */)
				);	
			sqlArray.push(sqlStmt);
			
			var sqlStmt=new Sql("Comment function check_hierarchy_" + 
					response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("check_hierarchy_function_comment.sql", 
					dbType, 
					"check_hierarchy_" + response.fields["geographyName"].toLowerCase() /* Function name */)
				);			
			sqlArray.push(sqlStmt);			
			
			var sqlStmt=new Sql("Insert into hierarchy_" + response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("insert_hierarchy.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase()			/* Geography */)
				);
			sqlArray.push(sqlStmt);
			
			var sqlStmt=new Sql("Check intersctions  for geograpy: " + 
					response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("check_intersections.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase() 				/* Geography */)
				);
			sqlArray.push(sqlStmt);
			
		} // End of createHierarchyTable()
		
		/*
		 * Function: 	createGeolevelsTable()
		 * Parameters:	None
		 * Description:	Create geolevels meta data table: geolevels_<geographyName> 
		 *				SQL statements
		 */	 
		function createGeolevelsTable() {
			sqlArray.push(new Sql("Geolevels meta data"));	
			
			var sqlStmt=new Sql("Drop table geolevels_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, "geolevels_" + response.fields["geographyName"].toLowerCase() /* Table name */)); 
			sqlArray.push(sqlStmt);
			
			var sqlStmt=new Sql("Create geolevels meta data table",
				getSqlFromFile("create_geolevels_table.sql", undefined /* Common */, 
					"geolevels_" + response.fields["geographyName"].toLowerCase() /* Table name */)); 		
			sqlArray.push(sqlStmt);	
			
			var sqlStmt=new Sql("Comment geolevels meta data table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"geolevels_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
					"Geolevels: hierarchy of level within a geography"					/* Comment */)
				);
			sqlArray.push(sqlStmt);
			
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
				var sqlStmt=new Sql("Comment geolevels meta data column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"geolevels_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */)
					);
				sqlArray.push(sqlStmt);		
			}		
			
			for (var i=0; i<csvFiles.length; i++) { // Main file process loop	
				var sqlStmt=new Sql("Insert geolevels meta data for: " + csvFiles[i].tableName, 
					getSqlFromFile("insert_geolevel.sql", 
						undefined /* Common */, 
						"geolevels_" + response.fields["geographyName"].toLowerCase() 	/* 1: table; e.g. geolevels_cb_2014_us_county_500k */,
						response.fields["geographyName"] 								/* 2: geography; e.g. cb_2014_us_500k */,
						csvFiles[i].tableName 											/* 3: Geolevel name; e.g. cb_2014_us_county_500k */,
						csvFiles[i].geolevel 											/* 4: Geolevel id; e.g. 3 */,
						csvFiles[i].geolevelDescription 								/* 5: Geolevel description; e.g. "The State-County at a scale of 1:500,000" */,
						"lookup_" + csvFiles[i].tableName 								/* 6: lookup table; e.g. lookup_cb_2014_us_county_500k */,
						csvFiles[i].file_name 											/* 7: shapefile; e.g. cb_2014_us_county_500k */,
						csvFiles[i].tableName											/* 8: shapefile table; e.g. cb_2014_us_county_500k */)
						 );
				sqlArray.push(sqlStmt);
			}				
		} // End of createGeolevelsTable()

		/*
		 * Function: 	createGeographyTable()
		 * Parameters:	None
		 * Description:	Create geography meta data table: geography_<geographyName> 
		 *				SQL statements
		 */			
		function createGeographyTable() {
			sqlArray.push(new Sql("Geography meta data"));	
			
			var sqlStmt=new Sql("Drop table geography_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, 
					"geography_" + response.fields["geographyName"].toLowerCase() /* Table name */)); 
			sqlArray.push(sqlStmt);
	
			var sqlStmt=new Sql("Create geography meta data table",
				getSqlFromFile("create_geography_table.sql", undefined /* Common */, 
					"geography_" + response.fields["geographyName"].toLowerCase() /* Table name */)); 		
			sqlArray.push(sqlStmt);		

			var sqlStmt=new Sql("Comment geography meta data table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"geography_" + response.fields["geographyName"].toLowerCase(),	/* Table name */
					"Hierarchial geographies. Usually based on Census geography"	/* Comment */)
				);
			sqlArray.push(sqlStmt);
	
			var sqlStmt=new Sql("Populate geography meta data table",
				getSqlFromFile("insert_geography.sql", 
					undefined /* Common */, 
					"geography_" + response.fields["geographyName"].toLowerCase()	/* table; e.g. geography_cb_2014_us_county_500k */,
					response.fields["geographyName"] 								/* Geography; e.g. cb_2014_us_500k */,
					response.fields["geographyDesc"] 								/* Geography description; e.g. "United states to county level" */,
					"hierarchy_" + response.fields["geographyName"].toLowerCase()	/* Hierarchy table; e.g. hierarchy_cb_2014_us_500k */,
					response.fields["srid"] 										/* SRID; e.g. 4269 */,
					defaultcomparea													/* Default comparision area */,
					defaultstudyarea 												/* Default study area */)
				);
			sqlArray.push(sqlStmt)	
			
			var fieldArray = ['geography', 'description', 'hierarchytable', 'srid', 'defaultcomparea', 'defaultstudyarea'];
			var fieldDescArray = ['Geography name', 
				'Description', 
				'Hierarchy table', 
				'Projection SRID', 
				'Default comparison area: lowest resolution geolevel', 
				'Default study area: highest resolution geolevel'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment geography meta data column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"geography_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */)
					);
				sqlArray.push(sqlStmt);			
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
				getSqlFromFile("drop_table.sql", dbType, 
					csvFiles[i].tableName /* Table name */)); 				
			sqlArray.push(sqlStmt);
			
			var columnList=Object.keys(csvFiles[i].rows[0]);
			var sqlStmt=new Sql("Create table" + csvFiles[i].tableName, "CREATE TABLE " + csvFiles[i].tableName + " (");
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
			sqlArray.push(sqlStmt);

			var sqlStmt=new Sql("Comment geospatial data table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					csvFiles[i].tableName,			/* Table name */
					csvFiles[i].geolevelDescription	/* Comment */)
				);			
			sqlArray.push(sqlStmt);
			
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
			sqlArray.push(sqlStmt);
			
			var sqlStmt=new Sql("Row check: " + csvFiles[i].rows.length,
				getSqlFromFile("csvfile_rowcheck.sql", 
					dbType, 
					csvFiles[i].tableName	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					csvFiles[i].rows.length /* 2: Expected number of rows; e.g. 3233 */,
					"gid"					/* 3: Column to count; e.g. gid */)
				);
			sqlArray.push(sqlStmt);	

			var sqlStmt=new Sql("Add primary key " + csvFiles[i].tableName, 
				getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
					csvFiles[i].tableName	/* Table name */, 
					'gid'					/* Primary key */)); 
			sqlArray.push(sqlStmt);	
			
			var sqlStmt=new Sql("Add unique key " + csvFiles[i].tableName, 
				getSqlFromFile("add_unique_key.sql", undefined /* Common */, 
					csvFiles[i].tableName 		/* 1: table; e.g. cb_2014_us_nation_5m */,
					csvFiles[i].tableName + "_uk" 	/* 2: constraint name; e.g. cb_2014_us_nation_5m_uk */,
					"areaid" 						/* 3: fields; e.g. areaid */)); 
			sqlArray.push(sqlStmt);		

			sqlArray.push(new Sql("Add geometric  data"));		
			
			var sqlStmt=new Sql("Add geometry column: geographic centroid",
				getSqlFromFile("add_geometry_column.sql", dbType, 
					csvFiles[i].tableName 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geographic_centroid' 	/* 2: column name; e.g. geographic_centroid */,
					4326 					/* 3: Column SRID; e.g. 4326 */,
					'POINT' 				/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */));				
			sqlArray.push(sqlStmt);
				
			var sqlStmt=new Sql("Add geometry column for original SRID geometry",
				getSqlFromFile("add_geometry_column.sql", dbType, 
					csvFiles[i].tableName 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geom_orig' 			/* 2: column name; e.g. geographic_centroid */,
					response.fields["srid"]	/* 3: Column SRID; e.g. 4326 */,
					'MULTIPOLYGON' 			/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */));	
			sqlArray.push(sqlStmt);
	
			for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
				var sqlStmt=new Sql("Add geometry column for zoomlevel: " + k,
					getSqlFromFile("add_geometry_column.sql", dbType, 
						csvFiles[i].tableName 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
						"geom_" + k 			/* 2: column name; e.g. geographic_centroid */,
						4326	/* 3: Column SRID; e.g. 4326 */,
						'MULTIPOLYGON' 			/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */));
				sqlArray.push(sqlStmt);
			}
				
			if (dbType == "PostGres") {				

				var sqlStmt=new Sql("Update geographic centroid, geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" + 
"   SET geographic_centroid = ST_GeomFromText(geographic_centroid_wkt, 4326),\n";
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					sqlStmt.sql+="" +
"       geom_" + k + " = \n" +
"       \t\tCASE ST_IsCollection(ST_GeomFromText(wkt_" + k + ", 4326)) /* Convert to Multipolygon */\n" +
"       \t\t\tWHEN true THEN 	ST_GeomFromText(wkt_" + k + ", 4326)\n" +
"       \t\t\tELSE 			ST_Multi(ST_GeomFromText(wkt_" + k + ", 4326))\n" +
"       \t\tEND,\n";
				}
				sqlStmt.sql+="" +
"       geom_orig = ST_Transform(\n" +
"       \t\tCASE ST_IsCollection(ST_GeomFromText(wkt_" + response.fields["max_zoomlevel"] + ", 4326)) /* Convert to Multipolygon */\n" +
"       \t\t\tWHEN true THEN 	ST_GeomFromText(wkt_" + response.fields["max_zoomlevel"] + ", 4326)\n" +
"       \t\t\tELSE 			ST_Multi(ST_GeomFromText(wkt_" + response.fields["max_zoomlevel"] + ", 4326))\n" +
"       \t\tEND, " + response.fields["srid"] + ")";
				sqlArray.push(sqlStmt);
				
				var sqlStmt=new Sql("Make geometry columns valid");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" +
"   SET\n";
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					sqlStmt.sql+="" +		
"       geom_" + k + " = CASE ST_IsValid(geom_" + k + ")\n" + 
"		\t\tWHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_" + k + "), 3 /* Remove non polygons */)\n" +
"		\t\tELSE geom_" + k + "\n" +
"		\tEND,\n";	
				}
				sqlStmt.sql+="" +
"       geom_orig = CASE ST_IsValid(geom_orig)\n" +
"			WHEN false THEN ST_CollectionExtract(ST_MakeValid(geom_orig), 3 /* Remove non polygons */)\n" +
"			ELSE geom_orig\n" +
"		END";	
				sqlArray.push(sqlStmt);
						
			}
			else if (dbType == "MSSQLServer") {					

				var sqlStmt=new Sql("Update geographic centroid, geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" + 
						    "   SET geographic_centroid = geography::STGeomFromText(geographic_centroid_wkt, 4326),\n";							
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					sqlStmt.sql+="       geom_" + k + " = geography::STGeomFromText(wkt_" + k + ", 4326).MakeValid(),\n";
				}	
// Needs codeplex SQL Server Spatial Tools:  http://sqlspatialtools.codeplex.com/wikipage?title=Current%20Contents&referringTitle=Home				
				sqlStmt.sql+="" +
"       geom_orig = /* geography::STTransform(geography::STGeomFromText(wkt_" + response.fields["max_zoomlevel"] + ", 4326).MakeValid(), " + 
					response.fields["srid"] + ") NOT POSSIBLE */ NULL"; 
				sqlArray.push(sqlStmt);
			}

			sqlArray.push(new Sql("Test geometry and make valid if required"));
			
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
			sqlArray.push(sqlStmt);		

//
// In SQL server, all polygons must have right hand orientation or bad things happen - like the area ~ one hemisphere
// as used to detect the problem
//
			sqlArray.push(new Sql("Make all polygons right handed"));
			for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
				var sqlStmt=new Sql("Make all polygons right handed for zoomlevel: " + k);
				if (dbType == "MSSQLServer") {	
					sqlStmt.sql=getSqlFromFile("force_rhr.sql", dbType, 
						"geom_" + k 			/* 1: geometry column; e.g. geom_6 */,
						csvFiles[i].tableName 	/* 2: table name; e.g. cb_2014_us_county_500k	*/);
					sqlArray.push(sqlStmt);
				}
			}
			if (dbType == "PostGres") { // No geom_orig in SQL Server
				var sqlStmt=new Sql("Make all polygons right handed for original geometry", 
"UPDATE " + csvFiles[i].tableName + "\n" +
"   SET");
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					sqlStmt.sql+="" +
"       geom_" + k + " = ST_ForceRHR(geom_" + k + "),\n";
				}
				sqlStmt.sql+="" +
"       geom_orig = ST_ForceRHR(geom_orig)";
				sqlArray.push(sqlStmt);
			}
			
			sqlArray.push(new Sql("Test Turf and DB areas agree to within 1%"));
			
			var sqlStmt=new Sql("Test Turf and DB areas agree to within 1% (Postgres)/5% (SQL server)",
				getSqlFromFile("area_check.sql", dbType, 
					"geom_" + response.fields["max_zoomlevel"] 	/* 1: geometry column; e.g. geom_11 */,
					csvFiles[i].tableName 						/* 2: table name; e.g. cb_2014_us_county_500k */));
			sqlArray.push(sqlStmt);
		
			sqlArray.push(new Sql("Create spatial indexes"));
			for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
				var sqlStmt=new Sql("Index geometry column for zoomlevel: " + k,
					getSqlFromFile("create_spatial_index.sql", dbType, 
						csvFiles[i].tableName + "_geom_" + k + "_gix"	/* Index name */,
						csvFiles[i].tableName  							/* Table name */, 
						"geom_" + k 									/* Index column(s) */)); 
				sqlArray.push(sqlStmt);
			}				
			var sqlStmt=new Sql("Index geometry column for original SRID geometry",
				getSqlFromFile("create_spatial_index.sql", dbType, 
					csvFiles[i].tableName + "_geom_orig_gix"	/* Index name */,
					csvFiles[i].tableName  						/* Table name */, 
					"geom_orig" 								/* Index column(s) */));
			sqlArray.push(sqlStmt);

			sqlArray.push(new Sql("Reports"));	
			
			var sqlStmt=new Sql("Areas and centroids report",
				getSqlFromFile("area_centroid_report.sql", dbType, 
					"geom_" + response.fields["max_zoomlevel"]	/* 1: geometry column; e.g. geom_11 */,
					csvFiles[i].tableName  						/* Table name */));
			sqlArray.push(sqlStmt);
			
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
		
		function Sql(comment, sql, sqlArray) { // Object constructor
			this.comment=comment;
			this.sql=sql;	
			this.nonsql=undefined;	
			this.dbStream=dbType;	

			if (sqlArray) {
				sqlArray.push(this);	
			}			
		}
		var sqlArray=[];
		
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
		
		for (var i=0; i<sqlArray.length; i++) {
			if (sqlArray[i].sql == undefined && sqlArray[i].nonsql == undefined) { // Comment			
				dbStream.write("\n--\n-- " + sqlArray[i].comment + "\n--\n");
			}
			else if (sqlArray[i].sql != undefined && dbType == "PostGres") {				
				dbStream.write("\n-- SQL statement " + i + ": " + sqlArray[i].comment + " >>>\n" + sqlArray[i].sql + ";\n");
			}
			else if (sqlArray[i].sql != undefined && dbType == "MSSQLServer") {				
				dbStream.write("\n-- SQL statement " + i + ": " + sqlArray[i].comment + " >>>\n" + sqlArray[i].sql + ";\nGO\n");
			}
			else if (sqlArray[i].nonsql != undefined && dbType == "PostGres") {				
				dbStream.write("\n-- PSQL statement " + i + ": " + sqlArray[i].comment + " >>>\n" + sqlArray[i].nonsql + "\n");
			}
			else if (sqlArray[i].nonsql != undefined && dbType == "MSSQLServer") {				
				dbStream.write("\n-- SQLCMD statement " + i + ": " + sqlArray[i].comment + " >>>\n" + sqlArray[i].nonsql + "\n");
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