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
//		for (var i = 2; i < arguments.length; i++) {
		for (var i = (arguments.length-1); i > 1 ; i--) { // Do it backwards to handle %10 etc
			var regex='%' + (i-1);
			sqlText=sqlText.replace(new RegExp(regex, 'g'), arguments[i]); 
				// No negative lookbehind in javascript so cannot ignore %%1
//			console.error("getSqlFromFile(): replace: " + regex + " with: " + arguments[i] /* + "; new SQL: " + sqlText */);
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
			var sqlStmt=new Sql("Start transaction", "BEGIN TRANSACTION", sqlArray);
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
			tableList.push("geometry_" + response.fields["geographyName"].toLowerCase());
			tableList.push("tile_intersects_" + response.fields["geographyName"].toLowerCase());
			tableList.push("tile_limits_" + response.fields["geographyName"].toLowerCase());
			tableList.push("t_tiles_" + response.fields["geographyName"].toLowerCase());
			
			for (var i=0; i<tableList.length; i++) {												
				var sqlStmt=new Sql("Describe table " + tableList[i], 
					getSqlFromFile("describe_table.sql", dbType, tableList[i] /* Table name */), 
				sqlArray); 
				
				var sqlStmt=new Sql("Analyze table " + tableList[i], 
					getSqlFromFile("vacuum_analyze_table.sql", dbType, tableList[i] /* Table name */), 
				sqlArray); 
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
					getSqlFromFile("drop_table.sql", dbType, "lookup_" + csvFiles[i].tableName /* Table name */), 
					sqlArray); 
				
				var sqlStmt=new Sql("Create table lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("create_lookup_table.sql", undefined /* Common */, csvFiles[i].tableName /* Table name */), 
					sqlArray); 			
				
				var sqlStmt=new Sql("Insert table lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("insert_lookup_table.sql", undefined /* Common */, csvFiles[i].tableName /* Table name */), 
					sqlArray); 	
				
				var sqlStmt=new Sql("Add primary key lookup_" + csvFiles[i].tableName, 
					getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
						"lookup_" + csvFiles[i].tableName 	/* Table name */, 
						csvFiles[i].tableName 				/* Primary key */), 
					sqlArray); 

				var sqlStmt=new Sql("Comment table lookup_" + csvFiles[i].tableName,
					getSqlFromFile("comment_table.sql", 
						dbType, 
						"lookup_" + csvFiles[i].tableName,		/* Table name */
						"Lookup table for " + csvFiles[i].geolevelDescription 	/* Comment */), 
					sqlArray);		
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
				getSqlFromFile("drop_table.sql", dbType, "hierarchy_" + response.fields["geographyName"].toLowerCase() /* Table name */), 
				sqlArray); 
			
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
					pkField 														/* Primary key */), 
				sqlArray); 	
					
			for (var i=0; i<csvFiles.length; i++) {	// Add non unique indexes
				if (csvFiles[i].geolevel != csvFiles.length && csvFiles[i].geolevel != 1) {	
					var sqlStmt=new Sql("Add index key hierarchy_" + 
						response.fields["geographyName"].toLowerCase() + "_" + csvFiles[i].tableName, 
						getSqlFromFile("create_index.sql", undefined /* Common */, 
							"hierarchy_" + response.fields["geographyName"].toLowerCase() + "_" + csvFiles[i].tableName	/* Index name */,
							"hierarchy_" + response.fields["geographyName"].toLowerCase() 								/* Table name */, 
							csvFiles[i].tableName 																		/* Index column(s) */
						), 
						sqlArray); 
				}
			}
			
			var sqlStmt=new Sql("Comment table: hierarchy_" + response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"hierarchy_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
					"Hierarchy lookup table for " + response.fields["geographyDesc"]	/* Comment */), 
				sqlArray);
			
			var sqlStmt=new Sql("Create function check_hierarchy_" + 
					response.fields["geographyName"].toLowerCase(),		
				getSqlFromFile("check_hierarchy_function.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase() 			/* Geography */), 
				sqlArray);	
			
			var sqlStmt=new Sql("Comment function check_hierarchy_" + 
					response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("check_hierarchy_function_comment.sql", 
					dbType, 
					"check_hierarchy_" + response.fields["geographyName"].toLowerCase() /* Function name */), 
				sqlArray);			
			
			var sqlStmt=new Sql("Insert into hierarchy_" + response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("insert_hierarchy.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase()			/* 1: Geography */,
					response.fields["max_zoomlevel"] 						/* 2: Max zoomlevel */), 
				sqlArray);
			
			var sqlStmt=new Sql("Check intersctions  for geograpy: " + 
					response.fields["geographyName"].toLowerCase(),
				getSqlFromFile("check_intersections.sql", 
					dbType, 
					response.fields["geographyName"].toLowerCase() 				/* Geography */), 
				sqlArray);
			
		} // End of createHierarchyTable()
		
		/*
		 * Function: 	createGeolevelsTable()
		 * Parameters:	None
		 * Description:	Create geolevels meta data table: geolevels_<geographyName> 
		 *				SQL statements
		 */	 
		function createGeolevelsTable() {
			sqlArray.push(new Sql("Geolevels meta data"));			

			sqlArray.push(new Sql("Drop depedent objects: tiles view and generate_series() [MS SQL Server only]"));			
			if (dbType == "MSSQLServer") { 
				var sqlStmt=new Sql("Drop generate_series() function", 
					getSqlFromFile("drop_generate_series.sql", dbType), sqlArray); 
			}	
			var sqlStmt=new Sql("Drop view " + "tiles_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_view.sql", dbType, 
					"tiles_" + response.fields["geographyName"].toLowerCase() /* View name */), sqlArray); 
					
			var sqlStmt=new Sql("Drop table geolevels_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, "geolevels_" + response.fields["geographyName"].toLowerCase() /* Table name */), 
				sqlArray); 
			
			var sqlStmt=new Sql("Create geolevels meta data table",
				getSqlFromFile("create_geolevels_table.sql", undefined /* Common */, 
					"geolevels_" + response.fields["geographyName"].toLowerCase() /* Table name */), sqlArray); 
			
			var sqlStmt=new Sql("Comment geolevels meta data table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"geolevels_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
					"Geolevels: hierarchy of level within a geography"					/* Comment */), sqlArray);
			
			var fieldArray = ['geography', 'geolevel_name', 'geolevel_id', 'description', 'lookup_table',
							  'lookup_desc_column', 'shapefile', 'shapefile_table', 'shapefile_area_id_column', 'shapefile_desc_column',
							  'resolution', 'comparea', 'listing', 'areaid_count'];
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
				'Able to be used in a disease map listing (0/1)',
				'Total number of area IDs within the geolevel'];
			for (var l=0; l< fieldArray.length; l++) {			
				var sqlStmt=new Sql("Comment geolevels meta data column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"geolevels_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray);
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
						csvFiles[i].tableName											/* 8: shapefile table; e.g. cb_2014_us_county_500k */), 
					sqlArray);
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
					"geography_" + response.fields["geographyName"].toLowerCase() /* Table name */), sqlArray); 
	
			var sqlStmt=new Sql("Create geography meta data table",
				getSqlFromFile("create_geography_table.sql", undefined /* Common */, 
					"geography_" + response.fields["geographyName"].toLowerCase() /* Table name */), sqlArray);

			var sqlStmt=new Sql("Comment geography meta data table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"geography_" + response.fields["geographyName"].toLowerCase(),	/* Table name */
					"Hierarchial geographies. Usually based on Census geography"	/* Comment */), sqlArray);
	
			var sqlStmt=new Sql("Populate geography meta data table",
				getSqlFromFile("insert_geography.sql", 
					undefined /* Common */, 
					"geography_" + response.fields["geographyName"].toLowerCase()	/* table; e.g. geography_cb_2014_us_county_500k */,
					response.fields["geographyName"] 								/* Geography; e.g. cb_2014_us_500k */,
					response.fields["geographyDesc"] 								/* Geography description; e.g. "United states to county level" */,
					"hierarchy_" + response.fields["geographyName"].toLowerCase()	/* Hierarchy table; e.g. hierarchy_cb_2014_us_500k */,
					"geometry_" + response.fields["geographyName"].toLowerCase()	/* Geometry table; e.g. geometry_cb_2014_us_500k */,
					"tiles_" + response.fields["geographyName"].toLowerCase()	    /* Tile table; e.g. tiles_cb_2014_us_500k */,
					response.fields["srid"] 										/* SRID; e.g. 4269 */,
					defaultcomparea													/* Default comparision area */,
					defaultstudyarea												/* Default study area */,
					response.fields["min_zoomlevel"] 								/* Min zoomlevel */,
					response.fields["max_zoomlevel"] 								/* Max zoomlevel */
					), 
				sqlArray);
			
			var fieldArray = ['geography', 'description', 'hierarchytable', 'geometrytable', 'tiletable', 
					'srid', 'defaultcomparea', 'defaultstudyarea', 'minzoomlevel', 'maxzoomlevel'];
			var fieldDescArray = ['Geography name', 
				'Description', 
				'Hierarchy table', 
				'Geometry table', 
				'Tile table', 
				'Projection SRID', 
				'Default comparison area: lowest resolution geolevel', 
				'Default study area: highest resolution geolevel',
				'Min zoomlevel',
				'Max zoomlevel'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment geography meta data column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"geography_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray);
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
					csvFiles[i].tableName /* Table name */), sqlArray); 
			
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
					csvFiles[i].geolevelDescription	/* Comment */),
				sqlArray);			
			
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
					"gid"					/* 3: Column to count; e.g. gid */), 
				sqlArray);

			var sqlStmt=new Sql("Add primary key " + csvFiles[i].tableName, 
				getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
					csvFiles[i].tableName	/* Table name */, 
					'gid'					/* Primary key */), sqlArray); 
			
			var sqlStmt=new Sql("Add unique key " + csvFiles[i].tableName, 
				getSqlFromFile("add_unique_key.sql", undefined /* Common */, 
					csvFiles[i].tableName 		/* 1: table; e.g. cb_2014_us_nation_5m */,
					csvFiles[i].tableName + "_uk" 	/* 2: constraint name; e.g. cb_2014_us_nation_5m_uk */,
					"areaid" 						/* 3: fields; e.g. areaid */), sqlArray); 

			sqlArray.push(new Sql("Add geometric  data"));		
			
			var sqlStmt=new Sql("Add geometry column: geographic centroid",
				getSqlFromFile("add_geometry_column.sql", dbType, 
					csvFiles[i].tableName 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geographic_centroid' 	/* 2: column name; e.g. geographic_centroid */,
					4326 					/* 3: Column SRID; e.g. 4326 */,
					'POINT' 				/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), sqlArray);	
				
			var sqlStmt=new Sql("Add geometry column for original SRID geometry",
				getSqlFromFile("add_geometry_column.sql", dbType, 
					csvFiles[i].tableName 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geom_orig' 			/* 2: column name; e.g. geographic_centroid */,
					response.fields["srid"]	/* 3: Column SRID; e.g. 4326 */,
					'MULTIPOLYGON' 			/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), sqlArray);
	
			for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
				var sqlStmt=new Sql("Add geometry column for zoomlevel: " + k,
					getSqlFromFile("add_geometry_column.sql", dbType, 
						csvFiles[i].tableName 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
						"geom_" + k 			/* 2: column name; e.g. geographic_centroid */,
						4326	/* 3: Column SRID; e.g. 4326 */,
						'MULTIPOLYGON' 			/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), sqlArray);
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
					csvFiles[i].tableName 						/* 2: table name; e.g. cb_2014_us_county_500k */), sqlArray);
		
			sqlArray.push(new Sql("Create spatial indexes"));
			for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
				var sqlStmt=new Sql("Index geometry column for zoomlevel: " + k,
					getSqlFromFile("create_spatial_index.sql", dbType, 
						csvFiles[i].tableName + "_geom_" + k + "_gix"	/* Index name */,
						csvFiles[i].tableName  							/* Table name */, 
						"geom_" + k 									/* Index column(s) */), sqlArray); 
			}				
			var sqlStmt=new Sql("Index geometry column for original SRID geometry",
				getSqlFromFile("create_spatial_index.sql", dbType, 
					csvFiles[i].tableName + "_geom_orig_gix"	/* Index name */,
					csvFiles[i].tableName  						/* Table name */, 
					"geom_orig" 								/* Index column(s) */), sqlArray);

			sqlArray.push(new Sql("Reports"));	
			
			var sqlStmt=new Sql("Areas and centroids report",
				getSqlFromFile("area_centroid_report.sql", dbType, 
					"geom_" + response.fields["max_zoomlevel"]	/* 1: geometry column; e.g. geom_11 */,
					csvFiles[i].tableName  						/* Table name */), sqlArray);
			
			// Set default study and comparison areas
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

		/*
		 * Function: 	createTilesTables()
		 * Parameters:	None
		 * Description:	Create tiles tables 
		 *				SQL statements
		 */			
		function createTilesTables() {
			var sqlStmt;	
			
			sqlArray.push(new Sql("Create tiles functions"));

			var sqlStmt=new Sql("Create function: longitude2tile.sql", 
				getSqlFromFile("longitude2tile.sql", dbType), sqlArray); 
			var sqlStmt=new Sql("Create function: latitude2tile.sql", 
				getSqlFromFile("latitude2tile.sql", dbType), sqlArray); 
			var sqlStmt=new Sql("Create function: tile2longitude.sql", 
				getSqlFromFile("tile2longitude.sql", dbType), sqlArray); 
			var sqlStmt=new Sql("Create function: tile2latitude.sql", 
				getSqlFromFile("tile2latitude.sql", dbType), sqlArray); 			
			
			var singleBoundaryGeolevelTable;
			for (var i=0; i<csvFiles.length; i++) {	
				if (csvFiles[i].geolevel == 1) {
					singleBoundaryGeolevelTable=csvFiles[i].tableName;
				}
			}
/*
SAME:

Postgres:

    geography    | min_geolevel_id | max_geolevel_id | zoomlevel | area_xmin  | area_xmax | area_ymin  | area_ymax | y_mintile | y_maxtile | x_mintile | x_maxtile
-----------------+-----------------+-----------------+-----------+------------+-----------+------------+-----------+-----------+-----------+-----------+-----------
 cb_2014_us_500k |               1 |               3 |        11 | -179.14734 | 179.77847 | -14.552549 | 71.352561 |      1107 |       435 |         4 |      2046

SQL Server
 
geography          min_geolevel_id max_geolevel_id zoomlevel   Xmin       Xmax       Ymin       Ymax       Y_mintile   Y_maxtile   X_mintile   X_maxtile
tile
------------------ --------------- --------------- ----------- ---------- ---------- ---------- ---------- ----------- ----------- ----------- ---------
cb_2014_us_500k                  1               3          11 -179.14734  179.77847  -14.55255   71.35256        1107         435           4      2046
 */
			var sqlStmt=new Sql("Tile check", 
				getSqlFromFile("tile_check.sql", dbType,
					"geolevels_" + response.fields["geographyName"].toLowerCase() 	/* 1: Lowest resolution geolevels table */,
					response.fields["geographyName"] 								/* 2: Geography */,
					response.fields["min_zoomlevel"]							 	/* 3: min_zoomlevel */,
					response.fields["max_zoomlevel"] 								/* 4: max_zoomlevel */,
					singleBoundaryGeolevelTable										/* 5: Geolevel id = 1 geometry table */
				), sqlArray); 

			sqlArray.push(new Sql("Create tiles tables"));
			
			var sqlStmt=new Sql("Drop table " + "t_tiles_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, 
					"t_tiles_" + response.fields["geographyName"].toLowerCase() /* Table name */), sqlArray); 
					
			if (dbType == "MSSQLServer") { 
				var sqlStmt=new Sql("Create tiles table", 
					getSqlFromFile("create_tiles_table.sql", 
						undefined /* Common */,	
						"t_tiles_" + response.fields["geographyName"].toLowerCase() 	/* 1: Tiles table name */,
						"Text"															/* 2: JSON datatype (Postgres JSON, SQL server Text) */
						), sqlArray); 
			}
			else if (dbType == "PostGres") { // No JSON in SQL Server
				var sqlStmt=new Sql("Create tiles table", 
					getSqlFromFile("create_tiles_table.sql", 
						undefined /* Common */,	
						"t_tiles_" + response.fields["geographyName"].toLowerCase() 	/* 1: Tiles table name */,
						"JSON"														/* 2: JSON datatype (Postgres JSON, SQL server Text) */
						), sqlArray); 
			}			
			
			var sqlStmt=new Sql("Comment tiles table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"t_tiles_" + response.fields["geographyName"].toLowerCase(),	/* Table name */
					"Maptiles for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11"	/* Comment */), sqlArray);
					
			var fieldArray = ['geolevel_id', 'zoomlevel', 'x', 'y', 'optimised_geojson', 'optimised_topojson', 'tile_id'];
			var fieldDescArray = ['ID for ordering (1=lowest resolution). Up to 99 supported.',
				'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11',
				'X tile number. From 0 to (2**<zoomlevel>)-1',
				'Y tile number. From 0 to (2**<zoomlevel>)-1',
				'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.',
				'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.',
				'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment tiles table column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"t_tiles_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray);
			}				

			if (dbType == "MSSQLServer") { 			
				var sqlStmt=new Sql("Create generate_series() function", 
					getSqlFromFile("generate_series.sql", dbType), sqlArray); 
			}

			var sqlStmt=new Sql("Create tiles view", 
				getSqlFromFile("create_tiles_view.sql", 
					dbType,	
					"tiles_" + response.fields["geographyName"].toLowerCase() 		/* 1: Tiles view name */,
					"geolevels_" + response.fields["geographyName"].toLowerCase()   /* 2: geolevel table; e.g. geolevels_cb_2014_us_county_500k */,
					"NOT_USED"															/* 3: JSON datatype (Postgres JSON, SQL server VARCHAR) */,
					"t_tiles_" + response.fields["geographyName"].toLowerCase() 	/* 4: tiles table; e.g. t_tiles_cb_2014_us_500k */,
					response.fields["max_zoomlevel"]								/* 5: Max zoomlevel; e.g. 11 */
					), sqlArray); 		

			var sqlStmt=new Sql("Comment tiles view",
				getSqlFromFile("comment_view.sql", 
					dbType, 
					"tiles_" + response.fields["geographyName"].toLowerCase(),	/* Table name */
					"Maptiles view for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11. This view is efficent!"	/* Comment */), sqlArray);
					
			var fieldArray = ['geography', 'geolevel_id', 'zoomlevel', 'x', 'y', 'optimised_geojson', 'optimised_topojson', 'tile_id', 'geolevel_name', 'no_area_ids'];
			var fieldDescArray = ['Geography',
				'ID for ordering (1=lowest resolution). Up to 99 supported.',
				'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11',
				'X tile number. From 0 to (2**<zoomlevel>)-1',
				'Y tile number. From 0 to (2**<zoomlevel>)-1',
				'Tile multipolygon in GeoJSON format, optimised for zoomlevel N.',
				'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.',
				'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>',
				'Name of geolevel. This will be a column name in the numerator/denominator tables',
				'Tile contains no area_ids flag: 0/1'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment tiles view column",
					getSqlFromFile("comment_view_column.sql", 
						dbType, 
						"tiles_" + response.fields["geographyName"].toLowerCase(),		/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray);
			}				

			sqlArray.push(new Sql("Create tile limits table"));
		
			if (dbType == "MSSQLServer") { 	
				var sqlStmt=new Sql("Create tileMaker_STMakeEnvelope()", 
					getSqlFromFile("tileMaker_STMakeEnvelope.sql", dbType), 
					sqlArray); 
			}
				
			var sqlStmt=new Sql("Drop table " + "tile_limits_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, 
					"tile_limits_" + response.fields["geographyName"].toLowerCase() /* Table name */), sqlArray); 
		
			var sqlStmt=new Sql("Create table " + "tile_limits_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("create_tile_limits_table.sql", dbType, 
					"tile_limits_" + response.fields["geographyName"].toLowerCase() /* 1: Tile limits table */,
					"geometry_" + response.fields["geographyName"].toLowerCase() 	/* 2: Geometry table */,
					response.fields["max_zoomlevel"] 								/* 3: max_zoomlevel */), 
				sqlArray); 

			var sqlStmt=new Sql("Comment tile limits table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"tile_limits_" + response.fields["geographyName"].toLowerCase(),	/* Table name */
					"Tile limits"	/* Comment */), sqlArray);
					
			var fieldArray = ['zoomlevel', 'x_min', 'x_max', 'y_min', 'y_max', 'y_mintile', 'y_maxtile',
				'x_mintile', 'x_maxtile', 'bbox'];
			var fieldDescArray = ['Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at max zooomlevel (11)',
				'Min X (longitude)',
				'Max X (longitude)',
				'Min Y (latitude)',
				'Max Y (latitude)',
				'Min Y tile number (latitude)',
				'Max Y tile number (latitude)',
				'Min X tile number (longitude)',
				'Max X tile number (longitude)',
				'Bounding box polygon for geolevel_id 1 area'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment tile limits table column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"tile_limits_" + response.fields["geographyName"].toLowerCase(),	/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray);
			}	

			if (dbType == "MSSQLServer") { 	
				var sqlStmt=new Sql("Make primary key not null",
					getSqlFromFile("not_null.sql", 
						dbType, 
						"tile_limits_" + response.fields["geographyName"].toLowerCase()		/* Table name */,
						"zoomlevel"															/* Primary key */), 
					sqlArray);
			}			
			var sqlStmt=new Sql("Add primary key",
				getSqlFromFile("add_primary_key.sql", 
					undefined /* Common */, 
					"tile_limits_" + response.fields["geographyName"].toLowerCase()		/* Table name */,
					"zoomlevel"									/* Primary key */), 
				sqlArray);	
			var sqlStmt=new Sql("Analyze table",
				getSqlFromFile("analyze_table.sql", 
					dbType, 
					"tile_limits_" + response.fields["geographyName"].toLowerCase()		/* Table name */), 
				sqlArray);	
			var sqlStmt=new Sql("Analyze table",
				"SELECT zoomlevel, x_min, x_max, y_min, y_max, y_mintile, y_maxtile, x_mintile, x_maxtile FROM tile_limits_" + 
					response.fields["geographyName"].toLowerCase(), 
				sqlArray);	
					
			if (dbType == "MSSQLServer") { 
				var sqlStmt=new Sql("Drop table " + "tile_intersects_" + response.fields["geographyName"].toLowerCase(), 
					getSqlFromFile("drop_table.sql", dbType, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase() /* Table name */), sqlArray);
			}
			else if (dbType == "PostGres") { 
				var sqlStmt=new Sql("Drop table " + "tile_intersects_" + response.fields["geographyName"].toLowerCase(), 
					getSqlFromFile("drop_table_cascade.sql", dbType, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase() /* Table name */), sqlArray);						
			}
			
			if (dbType == "MSSQLServer") { // No JSON in SQL Server
				var sqlStmt=new Sql("Create tile intersects table",
					getSqlFromFile("create_tile_intersects_table.sql", 
						undefined /* Common */, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()		/* Table name */,
						"Text"									/* JSON datatype (Postgres: JSON, MS SQL Server: Text) */,
						"bit"									/* STWithin() return datatype: bit (0/1) */), 
					sqlArray);					
			}					
			else if (dbType == "PostGres") { // No JSON in SQL Server					
				var sqlStmt=new Sql("Create tile intersects table",
					getSqlFromFile("create_tile_intersects_table.sql", 
						undefined /* Common */, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()		/* Table name */,
						"JSON"									/* JSON datatype (Postgres: JSON, MS SQL Server: Text) */,
						"BOOLEAN"								/* ST_Within() return datatype: bit (0/1) */), 
					sqlArray);						
			}

			var sqlStmt=new Sql("Add geometry column: bbox",
				getSqlFromFile("add_geometry_column2.sql", dbType, 
					"tile_intersects_" + response.fields["geographyName"].toLowerCase()
											/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'bbox' 					/* 2: column name; e.g. geographic_centroid */,
					4326 					/* 3: Column SRID; e.g. 4326 */,
					'POLYGON' 				/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), sqlArray);
			var sqlStmt=new Sql("Add geometry column: geom",
				getSqlFromFile("add_geometry_column2.sql", dbType, 
					"tile_intersects_" + response.fields["geographyName"].toLowerCase()
											/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geom' 					/* 2: column name; e.g. geographic_centroid */,
					4326 					/* 3: Column SRID; e.g. 4326 */,
					'MULTIPOLYGON' 			/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), sqlArray);
					
			var sqlStmt=new Sql("Comment tile intersects table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"tile_intersects_" + response.fields["geographyName"].toLowerCase(),	/* Table name */
					"Tile area id intersects"	/* Comment */), sqlArray);
					
			var fieldArray = ['geolevel_id', 'zoomlevel', 'areaid', 'x', 'y', 'optimised_geojson',
				'within', 'optimised_wkt', 'bbox', 'geom'];
			var fieldDescArray = ['ID for ordering (1=lowest resolution). Up to 99 supported.',
				'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11',
				'Area ID',
				'X tile number. From 0 to (2**<zoomlevel>)-1',
				'Y tile number. From 0 to (2**<zoomlevel>)-1',
				'Tile areaid intersect multipolygon in GeoJSON format, optimised for zoomlevel N.',
				'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.',
				'Tile areaid intersect multipolygon in WKT format, optimised for zoomlevel N.',
				'Bounding box of tile as a polygon.',
				'Geometry of area.'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment tile intersects table column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase(),/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray);
			}			

			if (dbType == "PostGres") { // Partition Postgres
				var sqlStmt=new Sql("Create partitioned tables and insert function for tile intersects table; comment partitioned tables and columns",
					getSqlFromFile("partition_tile_intersects_table.sql", 
						dbType, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()	/* 1: Tile iontersects table name */,
						response.fields["max_zoomlevel"]									/* 2: Max zoomlevel; e.g. 11 */,
						"geolevels_" + response.fields["geographyName"].toLowerCase()		/* 3: Geolevels table; 
																								e.g. geolevels_cb_2014_us_500k */), 
					sqlArray);			

				var sqlStmt=new Sql("Partition tile intersects table: insert trigger",
					getSqlFromFile("partition_trigger.sql", 
						dbType, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()		/* Table name */), 
					sqlArray);						
			}

			var sqlStmt=new Sql("INSERT into tile intersects table",
				getSqlFromFile("tile_intersects_insert.sql", 
					dbType, 
					"tile_intersects_" + response.fields["geographyName"].toLowerCase(),	/* Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
					"tile_limits_" + response.fields["geographyName"].toLowerCase(),		/* Tile limits table name; e.g. tile_limits_cb_2014_us_500k */
					"geometry_" + response.fields["geographyName"].toLowerCase()			/* Geometry table name; e.g. geometry_cb_2014_us_500k */
					), sqlArray);

					
			if (dbType == "PostGres") { 
				var sqlStmt=new Sql("Add primary key",
					getSqlFromFile("add_primary_key.sql", 
						undefined /* Common */, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()		/* Tile intersects table name */,
						"geolevel_id, zoomlevel, areaid, x, y"									/* Primary key */), 
					sqlArray);	
			}
			else if (dbType == "MSSQLServer") { // Force PK to be non clustered so inserts are fast
				var sqlStmt=new Sql("Add non clustered primary key",
					getSqlFromFile("add_primary_key.sql", 
						dbType, 
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()		/* Tile intersects table name */,
						"geolevel_id, zoomlevel, areaid, x, y"									/* Primary key */), 
					sqlArray);	
			}	
			
			var sqlStmt=new Sql("Analyze table",
				getSqlFromFile("analyze_table.sql", 
					dbType, 
					"tile_intersects_" + response.fields["geographyName"].toLowerCase()		/* Table name */), 
				sqlArray);	
				
			var sqlStmt=new Sql("SELECT from tile intersects table",
				getSqlFromFile("tile_intersects_select.sql", 
					dbType, 
					"tile_intersects_" + response.fields["geographyName"].toLowerCase()	/* Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
					), sqlArray);

			if (dbType == "PostGres") { // Postgres tile manufacture
				var sqlStmt=new Sql("Create tile intersects table INSERT function",
					getSqlFromFile("tileMaker_intersector_function.sql", 
						dbType, 
						"tileMaker_intersector_" + response.fields["geographyName"].toLowerCase()	
																							/* 1: function name; e.g. tileMaker_intersector_cb_2014_us_500k */,
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()	/* 2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k */,
						"tile_limits_" + response.fields["geographyName"].toLowerCase()		/* 3: tile limits table; e.g. tile_limits_cb_2014_us_500k */,
						"geometry_" + response.fields["geographyName"].toLowerCase()		/* 4: geometry table; e.g. geometry_cb_2014_us_500k */), 
					sqlArray);

				var sqlStmt=new Sql("Create second tile intersects table INSERT function (simplification errors)",
					getSqlFromFile("tileMaker_intersector_function2.sql", 
						dbType, 
						"tileMaker_intersector2_" + response.fields["geographyName"].toLowerCase()	
																							/* 1: function name; e.g. tileMaker_intersector2_cb_2014_us_500k */,
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()	/* 2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k */,
						"geometry_" + response.fields["geographyName"].toLowerCase()		/* 3: geometry table; e.g. geometry_cb_2014_us_500k */), 
					sqlArray);		

				var sqlStmt=new Sql("Create tiles table INSERT function (tile aggregator)",
					getSqlFromFile("tileMaker_aggregator_function.sql", 
						dbType, 
						"tileMaker_aggregator_" + response.fields["geographyName"].toLowerCase()	
																							/* 1: function name; e.g. tileMaker_aggregator_cb_2014_us_500k */,
						"tile_intersects_" + response.fields["geographyName"].toLowerCase()	/* 2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k */,
						"t_tiles_" + response.fields["geographyName"].toLowerCase()			/* 3: tiles table; e.g. t_tiles_cb_2014_us_500k */,
						"geolevels_" + response.fields["geographyName"].toLowerCase()		/* 4: geolevels table; e.g. geolevels_cb_2014_us_500k */), 
					sqlArray);		

				var sqlStmt=new Sql("Create tiles table INSERT function (tile aggregator)",
					getSqlFromFile("tileMaker_main_function.sql", 
						dbType, 
						response.fields["geographyName"].toLowerCase()						/* 1: geography; e.g. cb_2014_us_500k */,
 						"geometry_" + response.fields["geographyName"].toLowerCase()		/* 2: geometry table; e.g. geometry_cb_2014_us_500k */,
 						"geolevels_" + response.fields["geographyName"].toLowerCase()		/* 3: geolevels table; e.g. geolevels_cb_2014_us_500k */), 
					sqlArray);				
					
			}	
			else if (dbType == "MSSQLServer") { // MSSQLServer tile manufacture
				var sqlStmt=new Sql("INSERT into tile intersects table (MSSQLServer tile manufacture)",
					getSqlFromFile("tile_intersects_insert2.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase()	/* 1: Geometry table name; e.g. geometry_cb_2014_us_500k */,
 						"geolevels_" + response.fields["geographyName"].toLowerCase()	/* 2: Geolevels table name; e.g. geolevels_cb_2014_us_500k */,
 						"tile_intersects_" + response.fields["geographyName"].toLowerCase()	/* 3: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
						), sqlArray);
			} 
			
			var sqlStmt=new Sql("Tile intersects table % savings",
				getSqlFromFile("tile_intersects_select2.sql", 
					dbType, 
					"tile_intersects_" + response.fields["geographyName"].toLowerCase()	/* Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
					), sqlArray);			
	
			var sqlStmt=new Sql("Tile intersects table % WKT update",
				getSqlFromFile("tile_intersects_wkt_update.sql", 
					dbType, 
					"tile_intersects_" + response.fields["geographyName"].toLowerCase()	/* Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
					), sqlArray);		
					
		} // End of createTilesTables()
		
		/*
		 * Function: 	createGeometryTables()
		 * Parameters:	None
		 * Description:	Create geometry tables 
		 *				SQL statements
		 */			
		function createGeometryTables() {
			var sqlStmt;	
			
			sqlArray.push(new Sql("Create geometry tables"));		
			
			if (dbType == "PostGres") { // Partition Postgres
				var sqlStmt=new Sql("Drop geometry table " + "geometry_" + response.fields["geographyName"].toLowerCase(), 
					getSqlFromFile("drop_table_cascade.sql", dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase() 		/* Table name */
						), sqlArray); 
			}
			else if (dbType == "MSSQLServer") {// MS SQL Server
				var sqlStmt=new Sql("Drop geometry table " + "geometry_" + response.fields["geographyName"].toLowerCase(), 
					getSqlFromFile("drop_table.sql", dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase() 		/* Table name */
						), sqlArray); 
			}
			
			var sqlStmt=new Sql("Create geometry table " + "geometry_" + response.fields["geographyName"].toLowerCase(), 
				getSqlFromFile("create_geometry_table.sql", 
					undefined /* Common */, 
					"geometry_" + response.fields["geographyName"].toLowerCase() /* Table name */), 
				sqlArray); 
					
			var sqlStmt=new Sql("Add geom geometry column",
				getSqlFromFile("add_geometry_column2.sql", dbType, 
					"geometry_" + response.fields["geographyName"].toLowerCase() 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geom' 															/* 2: column name; e.g. geographic_centroid */,
					4326															/* 3: Column SRID; e.g. 4326 */,
					'MULTIPOLYGON' 													/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), 
					sqlArray);
			if (dbType == "MSSQLServer") { // Add bounding box for implement PostGIS && operator
 				var sqlStmt=new Sql("Add bbox geometry column",
				getSqlFromFile("add_geometry_column2.sql", dbType, 
					"geometry_" + response.fields["geographyName"].toLowerCase() 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'bbox' 															/* 2: column name; e.g. geographic_centroid */,
					4326															/* 3: Column SRID; e.g. 4326 */,
					'POLYGON' 														/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), 
					sqlArray);
				var sqlStmt=new Sql("Comment geometry table column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase(), /* Geometry table name */
						'bbox'														/* Column name */,
						'Bounding box'												/* Comment */), 
					sqlArray);
			}
			
			var sqlStmt=new Sql("Comment geometry table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"geometry_" + response.fields["geographyName"].toLowerCase(),	/* Table name */
					"All geolevels geometry combined into a single table for a single geography"	/* Comment */), sqlArray);
					
			var fieldArray = ['geolevel_id', 'zoomlevel', 'areaid', 'geom'];
			var fieldDescArray = ['ID for ordering (1=lowest resolution). Up to 99 supported.',
				'Zoom level: 0 to maxoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11',
				'Area ID.',
				'Geometry data in SRID 4326 (WGS84).'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment geometry table column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase(),		/* Geometry table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray);
			}	

			if (dbType == "PostGres") { // Partition Postgres
				var sqlStmt=new Sql("Create partitioned tables and insert function for geometry table; comment partitioned tables and columns",
					getSqlFromFile("partition_geometry_table1.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase()		/* 1: Geometry table name */,
						response.fields["max_zoomlevel"]									/* 2: Max zoomlevel; e.g. 11 */), 
					sqlArray);			

				var sqlStmt=new Sql("Partition geometry table: insert trigger",
					getSqlFromFile("partition_trigger.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase()		/* Table name */), 
					sqlArray);						
			}
			
			var sqlFrag=undefined;
			for (var i=0; i<csvFiles.length; i++) { // Main file process loop				
				for (var k=response.fields["min_zoomlevel"]; k <= response.fields["max_zoomlevel"]; k++) {
					sqlFrag="INSERT INTO geometry_cb_2014_us_500k(geolevel_id, areaid, zoomlevel, geom)\n";
					if (dbType == "PostGres") {
						sqlFrag+="SELECT " + csvFiles[i].geolevel + " geolevel_id,\n" +
"       areaid,\n" + 
"        " + k + " AS zoomlevel,\n" +
"       geom_" + k + " AS geom\n" +
"  FROM " + csvFiles[i].tableName + "\n";		
					}
					else if (dbType == "MSSQLServer") { 	
						sqlFrag+="SELECT " + csvFiles[i].geolevel + " geolevel_id,\n" +
"       areaid,\n" + 
"        " + k + " AS zoomlevel,\n" +
"       geometry::STGeomFromWKB(geom_" + k + ".STAsBinary(), 4326).MakeValid() AS geom\n" +
"  FROM " + csvFiles[i].tableName + "\n";	
					}
					sqlFrag+="ORDER BY 1, 3, 2";
					var sqlStmt=new Sql("Insert into geometry table",
						sqlFrag, 
						sqlArray);
				} // End of for zoomlevels loop
			} // End of main file process loop			

			if (dbType == "MSSQLServer") { // Update bounding box for implement PostGIS && operator
				var sqlStmt=new Sql("Update bounding box for implement PostGIS && operator",
					getSqlFromFile("geometry_bbox_update.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase()		/* 1: Geometry table name */), 
					sqlArray);
			
			}
			
			if (dbType == "PostGres") { // Partition Postgres
				var sqlStmt=new Sql("Add primary key, index and cluster (convert to index organized table)",
					getSqlFromFile("partition_geometry_table2.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase()		/* 1: Geometry table name */,
						response.fields["max_zoomlevel"]									/* 2: Max zoomlevel; e.g. 11 */), 
					sqlArray);	
			}		
			else if (dbType == "MSSQLServer") { 
			
				sqlArray.push(new Sql("No partitioning on SQL Server as it requires an Enterprise license; which"));
				sqlArray.push(new Sql("means you have to do it yourself using the generated scripts as a start.")); // Comment
					
				// Add primary key, index and cluster (convert to index organized table)
				var sqlStmt=new Sql("Add primary key",
					getSqlFromFile("add_primary_key.sql", 
						undefined /* Common */, 
						"geometry_" + response.fields["geographyName"].toLowerCase()		/* Table name */,
						"geolevel_id, areaid, zoomlevel"									/* Primary key */), 
					sqlArray);	
				var sqlStmt=new Sql("Create spatial index on geom",
					getSqlFromFile("create_spatial_geometry_index.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase() + "_gix"	/* Index name */, 
						"geometry_" + response.fields["geographyName"].toLowerCase()			/* Table name */, 
						"geom"																	/* Geometry field name */,
						csvFiles[0].bbox[0]														/* 4: Xmin (4326); e.g. -179.13729006727 */,
						csvFiles[0].bbox[1]														/* 5: Ymin (4326); e.g. -14.3737802873213 */, 
						csvFiles[0].bbox[2]														/* 6: Xmax (4326); e.g.  179.773803959804 */,
						csvFiles[0].bbox[3]														/* 7: Ymax (4326); e.g. 71.352561 */), 
					sqlArray);	
					
				var sqlStmt=new Sql("Create spatial index on bbox",
					getSqlFromFile("create_spatial_geometry_index.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase() + "_gix2"	/* Index name */, 
						"geometry_" + response.fields["geographyName"].toLowerCase()			/* Table name */, 
						"bbox"																	/* Geometry field name */,
						csvFiles[0].bbox[0]														/* 4: Xmin (4326); e.g. -179.13729006727 */,
						csvFiles[0].bbox[1]														/* 5: Ymin (4326); e.g. -14.3737802873213 */, 
						csvFiles[0].bbox[2]														/* 6: Xmax (4326); e.g.  179.773803959804 */,
						csvFiles[0].bbox[3]														/* 7: Ymax (4326); e.g. 71.352561 */), 
					sqlArray);	
					
				var sqlStmt=new Sql("Analyze table",
					getSqlFromFile("analyze_table.sql", 
						dbType, 
						"geometry_" + response.fields["geographyName"].toLowerCase()		/* Table name */), 
					sqlArray);	
			}
					
			var sqlStmt=new Sql("Update areaid_count column in geolevels table using geometry table", 
				getSqlFromFile("geolevels_areaid_update.sql", 
					dbType, 
					"geolevels_" + response.fields["geographyName"].toLowerCase() /* Geolevels table */,
					"geometry_" + response.fields["geographyName"].toLowerCase() /* Geometry table */), 
				sqlArray);
				
		} // End of createGeometryTables()
/*
psql -d sahsuland_dev -U peter -w -e -f pg_cb_2014_us_500k.sql
sqlcmd -E -b -m-1 -e -r1 -i mssql_cb_2014_us_500k.sql -v pwd="%cd%"
*/	
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
		createGeometryTables();
		createTilesTables();
		
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