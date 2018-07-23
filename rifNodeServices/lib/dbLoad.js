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
	  async = require('async'),
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
 * Parameters:	Internal response object, xmlConfig, HTTP request object, HTTP response object, dir, csvFiles object, callback to call at end of processing
 * Description:	Convert geoJSON to CSV; save as CSV files; create load scripts for Postgres and MS SQL server
 */		
var CreateDbLoadScripts = function CreateDbLoadScripts(response, xmlConfig, req, res, dir,  csvFiles, endCallback) {
	
	scopeChecker(__file, __line, {
		response: response,
		message: response.message,
		xmlConfig: xmlConfig,
		dataLoader: xmlConfig.dataLoader,
		fields: response.fields,
		geographyName: xmlConfig.dataLoader.geographyName,
		min_zoomlevel: xmlConfig.dataLoader.minZoomlevel,
		max_zoomlevel: xmlConfig.dataLoader.maxZoomlevel,
		srid: xmlConfig.dataLoader.srid,
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
	 * Parameters:	Directory to create in, CSV files object, callback
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
	var createSqlServerFmtFiles=function createSqlServerFmtFiles(dir, csvFiles, createSqlServerFmtFilesCallback) {	
			async.forEachOfSeries(csvFiles, 
				function csvFilesFmtProcessing(value, i, fmtCallback) {
					createSqlServerFmtFile(dir, csvFiles[i].tableName, csvFiles[i].rows, fmtCallback);
				},
				function csvFilesFmtError(err) {
					createSqlServerFmtFilesCallback(err);
				}
			); // End of async.forEachOfSeries csvFiles
	} // End of createSqlServerFmtFiles()
	
	/*
	 * Function: 	createSQLScriptHeader()
	 * Parameters:	Script file name (full path), dbbase type as a string ("PostGres" or "MSSQLServer"), 
	 *				header script name (dbType specific)
	 * Description:	Create header for SQL script
	 */		
	var createSQLScriptHeader=function createSQLScriptHeader(scriptName, dbType, headerScriptName) {
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
			
			baseScriptName=path.basename(scriptName);
			var header=getSqlFromFile("header.sql", undefined /* Common header */);
			newStream.write(header);
			header=getSqlFromFile(headerScriptName, dbType, 
				baseScriptName	/* file name */);
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
	 * Function: 	Sql()
	 * Parameters:	Comment, SQL statement, sqlArray, dbType (PostGres or MSSQLServer)
	 * Description:	Begin transaction SQL statements
	 */
	function Sql(comment, sql, sqlArray, dbType) { // Object constructor
		this.comment=comment;
		this.sql=sql;	
		this.nonsql=undefined;	
		this.dbStream=dbType;	

		if (sqlArray) {
			sqlArray.push(this);	
		}			
	}

//
// Start of common SQL
//
	
	/*
	 * Function: 	beginTransaction()
	 * Parameters:	sqlArray, dbType (PostGres or MSSQLServer)
	 * Description:	Begin transaction SQL statements
	 */	 
	function beginTransaction(sqlArray, dbType) {
		var sqlStmt=new Sql("Start transaction", "BEGIN TRANSACTION", sqlArray, dbType);
	} // End of beginTransaction()
	
	/*
	 * Function: 	commitTransaction()
	 * Parameters:	sqlArray, dbType (PostGres or MSSQLServer)
	 * Description:	Commit transaction SQL statements
	 */	 
	function commitTransaction(sqlArray, dbType) {
		var sqlStmt=new Sql("Commit transaction");
		if (dbType == "PostGres") {		
			sqlStmt.sql="END";	
		}
		else if (dbType == "MSSQLServer") {	
			sqlStmt.sql="COMMIT";	
		}				
		sqlStmt.dbType=dbType;
		sqlArray.push(sqlStmt);
	} // End of commitTransaction()		

	/*
	 * Function: 	createAdjacencyTable(sqlArray, dbType, schema)
	 * Parameters:	sqlArray, dbType
	 * Description:	Create hierarchy table: SQL statements
	 */	 
	function createAdjacencyTable(sqlArray, dbType, schema) {
		sqlArray.push(new Sql("Adjacency table"));

		if (schema && dbType == "MSSQLServer") {		
			var sqlStmt=new Sql("Drop table adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, schema + "adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), 
				sqlArray, dbType); 
		}
		else {		
			var sqlStmt=new Sql("Drop table adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, "adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), 
				sqlArray, dbType); 
		}	
		
		var sqlStmt=new Sql("Create table adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
			getSqlFromFile("create_adjacency_table.sql", undefined /* Common */, 
				"adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */,
				(schema||"")), 
			sqlArray, dbType); 

		var sqlStmt=new Sql("Comment table: adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase(),
			getSqlFromFile("comment_table.sql", 
				dbType, 
				"adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase(),		/* Table name */
				"Adjacency lookup table for " + response.fields["geographyDesc"]		/* Comment */), 
			sqlArray, dbType);	
		
		var fieldArray = ['geolevel_id', 'areaid', 'num_adjacencies', 'adjacency_list'];
		var fieldDescArray = ['ID for ordering (1=lowest resolution). Up to 99 supported.', 'Area Id', 'Number of adjacencies', 'Adjacent area Ids'];
		for (var l=0; l< fieldArray.length; l++) {		
			var sqlStmt=new Sql("Comment column: adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "." + fieldArray[l],
				getSqlFromFile("comment_column.sql", 
					dbType, 
					"adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
					fieldArray[l]														/* Column name */,
					fieldDescArray[l]													/* Comment */), 
				sqlArray, dbType);
		}
		
		if (schema && dbType == "MSSQLServer") {
			var sqlStmt=new Sql("Drop function " + xmlConfig.dataLoader.geographyName.toLowerCase() + "_GetAdjacencyMatrix()", 
				getSqlFromFile("drop_GetAdjacencyMatrix.sql", dbType, 
					xmlConfig.dataLoader.geographyName.toLowerCase() 				/* 1: Geography */), 
				sqlArray, dbType);	
			var sqlStmt=new Sql("Create function " + xmlConfig.dataLoader.geographyName.toLowerCase() + "_GetAdjacencyMatrix()", 
				getSqlFromFile("create_GetAdjacencyMatrix.sql", dbType, 
					xmlConfig.dataLoader.geographyName.toLowerCase() 				/* 1: Geography */, 
					"adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* 2: Adjacency Table name */), 
				sqlArray, dbType);	
			var sqlStmt=new Sql("Grant function " + xmlConfig.dataLoader.geographyName.toLowerCase() + "_GetAdjacencyMatrix()", 
				getSqlFromFile("grant_function.sql", dbType, 
					xmlConfig.dataLoader.geographyName.toLowerCase() + "_GetAdjacencyMatrix" /* 1: Function name */), 
				sqlArray, dbType);		
		}
	} // End of createAdjacencyTable()
	
	/*
	 * Function: 	createHierarchyTable(sqlArray, dbType, schema)
	 * Parameters:	sqlArray, dbType
	 * Description:	Create hierarchy table: SQL statements
	 */	 
	function createHierarchyTable(sqlArray, dbType, schema) {
		sqlArray.push(new Sql("Hierarchy table"));	

		if (schema && dbType == "MSSQLServer") {		
			var sqlStmt=new Sql("Drop table hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, schema + "hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), 
				sqlArray, dbType); 
		}
		else {		
			var sqlStmt=new Sql("Drop table hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, "hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), 
				sqlArray, dbType); 
		}
		
		var sqlStmt=new Sql("Create table hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase());
		sqlStmt.sql="CREATE TABLE " + (schema||"") + // Schema; e.g.rif_data. or "" 
			"hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() + " (\n";
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
				response.message+="\nDetected hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() +
					" primary key: " + pkField + "; file: " + i + "; geolevel: " + csvFiles[i].geolevel;
			}
		}
		sqlStmt.sql+=")";
		sqlArray.push(sqlStmt);
		
		var sqlStmt=new Sql("Add primary key hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
			getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
				(schema||"") + // Schema; e.g.rif_data. or "" 
				"hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 1: Table name */, 
				pkField 															/* 2: Primary key */), 
			sqlArray, dbType); 	
				
		for (var i=0; i<csvFiles.length; i++) {	// Add non unique indexes
			if (csvFiles[i].geolevel != csvFiles.length && csvFiles[i].geolevel != 1) {	
				var sqlStmt=new Sql("Add index key hierarchy_" + 
					xmlConfig.dataLoader.geographyName.toLowerCase() + "_" + csvFiles[i].tableName, 
					getSqlFromFile("create_index.sql", undefined /* Common */, 
						"hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "_" + csvFiles[i].tableName	/* Index name */,
						(schema||"") + // Schema; e.g.rif_data. or "" 
						"hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() 								/* Table name */, 
						csvFiles[i].tableName 																			/* Index column(s) */
					), 
					sqlArray, dbType); 
			}
		}
		
		var sqlStmt=new Sql("Comment table: hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(),
			getSqlFromFile("comment_table.sql", 
				dbType, 
				"hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(),		/* Table name */
				"Hierarchy lookup table for " + response.fields["geographyDesc"]		/* Comment */), 
			sqlArray, dbType);	
		
		for (var i=0; i<csvFiles.length; i++) {	
			var sqlStmt=new Sql("Comment column: hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
				"." + csvFiles[i].tableName,
				getSqlFromFile("comment_column.sql", 
					dbType, 
					"hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
					csvFiles[i].tableName,												/* Column name */
					"Hierarchy lookup for " + csvFiles[i].geolevelDescription			/* Comment */), 
				sqlArray, dbType);	
		}
		
	} // End of createHierarchyTable()

	/*
	 * Function: 	createGeometryTable(sqlArray, dbType, schema)
	 * Parameters:	sqlArray, dbType, schema
	 * Description:	Create geometry tables 
	 *				SQL statements
	 */			
	function createGeometryTable(sqlArray, dbType, schema) {
		var sqlStmt;	
		
		sqlArray.push(new Sql("Create geometry table"));		
		
		if (dbType == "PostGres") { // Partition Postgres
			var sqlStmt=new Sql("Drop geometry table " + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_table_cascade.sql", dbType, 
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() 		/* Table name */
					), sqlArray, dbType); 
		}
		else if (dbType == "MSSQLServer") {// MS SQL Server
			var sqlStmt=new Sql("Drop geometry table " + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, 
					(schema||"") + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() 		/* Table name */
					), sqlArray, dbType); 
		}
		
		var sqlStmt=new Sql("Create geometry table " + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
			getSqlFromFile("create_geometry_table.sql", 
				undefined /* Common */, 
				"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 1: Table name */,
				(schema||"")													/* 2: Schema; e.g.rif_data. or "" */), 
			sqlArray, dbType); 
				
		var sqlStmt=new Sql("Add geom geometry column",
			getSqlFromFile("add_geometry_column2.sql", dbType, 
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
				'geom' 															/* 2: column name; e.g. geographic_centroid */,
				4326															/* 3: Column SRID; e.g. 4326 */,
				'MULTIPOLYGON' 													/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */,
				(schema||"")													/* 5: Schema (rif_data. or "") [NEVER USED IN POSTGRES] */), 
				sqlArray, dbType);
		if (dbType == "MSSQLServer") { // Add bounding box for implement PostGIS && operator
			var sqlStmt=new Sql("Add bbox geometry column",
			getSqlFromFile("add_geometry_column2.sql", dbType, 
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
				'bbox' 															/* 2: column name; e.g. geographic_centroid */,
				4326															/* 3: Column SRID; e.g. 4326 */,
				'POLYGON' 														/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */,
				(schema||"")													/* 5: Schema (rif_data. or "") [NEVER USED IN POSTGRES] */), 
				sqlArray, dbType);
			var sqlStmt=new Sql("Comment geometry table column",
				getSqlFromFile("comment_column.sql", 
					dbType, 
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase(), /* Geometry table name */
					'bbox'														/* Column name */,
					'Bounding box'												/* Comment */), 
				sqlArray, dbType);
		}
		
		var sqlStmt=new Sql("Comment geometry table",
			getSqlFromFile("comment_table.sql", 
				dbType, 
				"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
				"All geolevels geometry combined into a single table for a single geography"	/* Comment */), sqlArray, dbType);
				
		var fieldArray = ['geolevel_id', 'zoomlevel', 'areaid', 'geom'];
		var fieldDescArray = ['ID for ordering (1=lowest resolution). Up to 99 supported.',
			'Zoom level: 0 to maxoomlevel (11). Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11',
			'Area ID.',
			'Geometry data in SRID 4326 (WGS84).'];
		for (var l=0; l< fieldArray.length; l++) {		
			var sqlStmt=new Sql("Comment geometry table column",
				getSqlFromFile("comment_column.sql", 
					dbType, 
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase(),		/* Geometry table name */
					fieldArray[l]														/* Column name */,
					fieldDescArray[l]													/* Comment */), 
				sqlArray, dbType);
		}	

		if (dbType == "PostGres") { // Partition Postgres
			var sqlStmt=new Sql("Create partitioned tables and insert function for geometry table; comment partitioned tables and columns",
				getSqlFromFile("partition_geometry_table1.sql", 
					dbType, 
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */,
					xmlConfig.dataLoader.maxZoomlevel									/* 2: Max zoomlevel; e.g. 11 */,
					csvFiles.length														/* 3: Number of geolevels (e.g. 3) */), 
				sqlArray, dbType);			

			var sqlStmt=new Sql("Partition geometry table: insert trigger",
				getSqlFromFile("partition_trigger.sql", 
					dbType, 
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */), 
				sqlArray, dbType);	
				
			var sqlStmt=new Sql("Comment partition geometry table: insert trigger",		
				getSqlFromFile("comment_partition_trigger.sql", 
					dbType, 
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */), 
				sqlArray, dbType);					
		}
		
	} // End of createGeometryTable()

	/*
	 * Function: 	createTilesTables()
	 * Parameters:	sqlArray, dbType, geoLevelsTable, schema
	 * Description:	Create tiles tables 
	 *				SQL statements
	 */			
	function createTilesTables(sqlArray, dbType, geoLevelsTable, schema) {
		var sqlStmt;			

		sqlArray.push(new Sql("Create tiles functions"));

		var sqlStmt=new Sql("Create function: longitude2tile.sql", 
			getSqlFromFile("longitude2tile.sql", dbType), sqlArray, dbType); 
		var sqlStmt=new Sql("Create function: latitude2tile.sql", 
			getSqlFromFile("latitude2tile.sql", dbType), sqlArray, dbType); 
		var sqlStmt=new Sql("Create function: tile2longitude.sql", 
			getSqlFromFile("tile2longitude.sql", dbType), sqlArray, dbType); 
		var sqlStmt=new Sql("Create function: tile2latitude.sql", 
			getSqlFromFile("tile2latitude.sql", dbType), sqlArray, dbType); 
				
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
		if (geoLevelsTable != "t_rif40_geolevels") {
			var sqlStmt=new Sql("Tile check", 
				getSqlFromFile("tile_check.sql", dbType,
					geoLevelsTable													/* 1: Lowest resolution geolevels table */,
					xmlConfig.dataLoader.geographyName 								/* 2: Geography */,
					xmlConfig.dataLoader.minZoomlevel							 	/* 3: min_zoomlevel */,
					xmlConfig.dataLoader.maxZoomlevel 								/* 4: max_zoomlevel */,
					singleBoundaryGeolevelTable										/* 5: Geolevel id = 1 geometry table */
				), sqlArray, dbType); 
		}

		sqlArray.push(new Sql("Create tiles tables"));
		
		var sqlStmt=new Sql("Drop table " + "t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
			getSqlFromFile("drop_table.sql", dbType, 
				(schema||"") + "t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), sqlArray, dbType); 
				
		if (dbType == "MSSQLServer") { 
			var sqlStmt=new Sql("Create tiles table", 
				getSqlFromFile("create_tiles_table.sql", 
					undefined /* Common */,	
					"t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 1: Tiles table name */,
					"NVARCHAR(MAX)"													/* 2: JSON datatype (Postgres JSON, SQL server NVARCHAR(MAX)) */,
					(schema||"")													/* 3: Schema; e.g.rif_data. or "" */
					), sqlArray, dbType); 
		}
		else if (dbType == "PostGres") { // No JSON in SQL Server
			var sqlStmt=new Sql("Create tiles table", 
				getSqlFromFile("create_tiles_table.sql", 
					undefined /* Common */,	
					"t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 1: Tiles table name */,
					"JSON"															/* 2: JSON datatype (Postgres JSON, SQL server Text) */,
					(schema||"")													/* 3: Schema; e.g.rif_data. or "" */
					), sqlArray, dbType); 
		}			
		
		var sqlStmt=new Sql("Comment tiles table",
			getSqlFromFile("comment_table.sql", 
				dbType, 
				"t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
				"Maptiles for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11"	/* Comment */), 
			sqlArray, dbType);
				
		var fieldArray = ['geolevel_id', 'zoomlevel', 'x', 'y', 'optimised_topojson', 'tile_id', 'areaid_count'];
		var fieldDescArray = ['ID for ordering (1=lowest resolution). Up to 99 supported.',
			'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11',
			'X tile number. From 0 to (2**<zoomlevel>)-1',
			'Y tile number. From 0 to (2**<zoomlevel>)-1',
			'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.',
			'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>',
			'Total number of areaIDs (geoJSON features)'];
		for (var l=0; l< fieldArray.length; l++) {		
			var sqlStmt=new Sql("Comment tiles table column",
				getSqlFromFile("comment_column.sql", 
					dbType, 
					"t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(),		/* Table name */
					fieldArray[l]														/* Column name */,
					fieldDescArray[l]													/* Comment */), 
				sqlArray, dbType);
		}			

		// Add indexes to tiles table
		var tilesIndexes = {
			x_tile: "geolevel_id, zoomlevel, x",
			y_tile: "geolevel_id, zoomlevel, x",
			xy_tile: "geolevel_id, zoomlevel, x, y",
			areaid_count: "areaid_count"
		}
		for (var key in tilesIndexes) {
			var sqlStmt=new Sql("Add tiles index: t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "_" + key, 
				getSqlFromFile("create_index.sql", undefined /* Common */, 
					"t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "_" + key		/* 1: Index name */,
					(schema||"") + // Schema; e.g.rif_data. or "" 
					"t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() 			/* 2: Table name */, 
					tilesIndexes[key] 														/* 3: Index column(s) */
				), 
				sqlArray, dbType); 
		}
					
		var dataSchema=schema;
		var appSchema;
		
		if (dbType == "MSSQLServer") { 
			appSchema='$(SQLCMDUSER).';
			if (schema) {
				appSchema='rif40.';
			}
		}
		var sqlStmt=new Sql("Create tiles view", 
			getSqlFromFile("create_tiles_view.sql", 
				dbType,	
				"tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 1: Tiles view name */,
				geoLevelsTable												   	/* 2: geolevel table; e.g. geolevels_cb_2014_us_county_500k */,
				"NOT_USED"														/* 3: JSON datatype (Postgres JSON, SQL server VARCHAR) */,
				"t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 4: tiles table; e.g. t_tiles_cb_2014_us_500k */,
				xmlConfig.dataLoader.maxZoomlevel								/* 5: Max zoomlevel; e.g. 11 */,
				(schema||"")													/* 6: Schema; e.g.rif_data. or "" */,
				(appSchema||"")													/* 7: RIF or user schema; e.g. $(SQLCMDUSER) or rif40 */,
				xmlConfig.dataLoader.geographyName.toUpperCase()				/* 8: Geography; e.g. USA_2014 */
				), sqlArray, dbType); 		

		var sqlStmt=new Sql("Comment tiles view",
			getSqlFromFile("comment_view.sql", 
				dbType, 
				"tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
				"Maptiles view for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11. This view is efficent!"	/* Comment */), 
			sqlArray, dbType);
				
		var fieldArray = ['geography', 'geolevel_id', 'zoomlevel', 'x', 'y', 'optimised_topojson', 'tile_id', 'geolevel_name', 'no_area_ids'];
		var fieldDescArray = ['Geography',
			'ID for ordering (1=lowest resolution). Up to 99 supported.',
			'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11',
			'X tile number. From 0 to (2**<zoomlevel>)-1',
			'Y tile number. From 0 to (2**<zoomlevel>)-1',
			'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.',
			'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>',
			'Name of geolevel. This will be a column name in the numerator/denominator tables',
			'Tile contains no area_ids flag: 0/1'];
		for (var l=0; l< fieldArray.length; l++) {		
			var sqlStmt=new Sql("Comment tiles view column",
				getSqlFromFile("comment_view_column.sql", 
					dbType, 
					"tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(),		/* Table name */
					fieldArray[l]														/* Column name */,
					fieldDescArray[l]													/* Comment */), 
				sqlArray, dbType);
		}				

	} // End of createTilesTables()	


	/*
	 * Function: 	createGeolevelsLookupTables()
	 * Parameters:	sqlArray, dbType
	 * Description:	Create geoelvels lookup tables: SQL statements
	 */	 
	function createGeolevelsLookupTables(sqlArray, dbType, schema) {
		sqlArray.push(new Sql("Create Geolevels lookup tables"));
		for (var i=0; i<csvFiles.length; i++) {		
			if (schema && dbType == "MSSQLServer") {
				var sqlStmt=new Sql("Drop table " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(), 
					getSqlFromFile("drop_table.sql", dbType, 
						schema + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase() /* Table name */), 
					sqlArray, dbType); 
			}
			else {
				var sqlStmt=new Sql("Drop table " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(), 
					getSqlFromFile("drop_table.sql", dbType, 
						(xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase() /* Table name */), 
					sqlArray, dbType); 
			}
			
			var sqlStmt=new Sql("Create table " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(), 
				getSqlFromFile("create_lookup_table.sql", dbType, (
					xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase().toLowerCase() /* 1: Table name */,
					xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toLowerCase()	/* 2: shapefile table name */,
					(schema||"")													/* 3: Schema; e.g.rif_data. or "" */
				), 
				sqlArray, dbType); 			 	

			var sqlStmt=new Sql("Comment table " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(),
				getSqlFromFile("comment_table.sql", 
					dbType, 
					(xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(),		/* Table name */
					"Lookup table for " + csvFiles[i].geolevelDescription 			/* Comment */), 
				sqlArray, dbType);		
	
			var sqlStmt=new Sql("Comment " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase() + " columns",
				getSqlFromFile("comment_column.sql", 
					dbType, 
					(xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(),		/* Table name */
					xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toLowerCase()	/* Column name */,
					"Area ID field"													/* Comment */), 
				sqlArray, dbType);	
	
			var sqlStmt=new Sql("Comment " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase() + " columns",
				getSqlFromFile("comment_column.sql", 
					dbType, 
					(xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(),		/* Table name */
					"gid"														/* Column name */,
					"GID field"													/* Comment */), 
				sqlArray, dbType);					
			var sqlStmt=new Sql("Comment " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase() + " columns",
				getSqlFromFile("comment_column.sql", 
					dbType, 
					(xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(),		/* Table name */
					"areaname"														/* Column name */,
					"Area Name field"												/* Comment */), 
				sqlArray, dbType);	
			var sqlStmt=new Sql("Comment " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase() + " columns",
				getSqlFromFile("comment_column.sql", 
					dbType, 
					(xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(),		/* Table name */
					"geographic_centroid"											/* Column name */,
					"Geographic centroid"											/* Comment */), 
				sqlArray, dbType);	
			var sqlStmt=new Sql("Comment " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase() + " columns",
				getSqlFromFile("comment_column.sql", 
					dbType, 
					(xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(),		/* Table name */
					"population_weighted_centroid"											/* Column name */,
					"Population weighted centroid"											/* Comment */),  
				sqlArray, dbType);						
		}				
	} // End of createGeolevelsLookupTables()
		
//
// End of common SQL
//
// Load specific SQL
//
		
	/*
	 * Function: 	addSQLStatements()
	 * Parameters:	Database stream, format file stream, CSV files object, srid (spatial reference identifier), 
	 *				dbbase type as a string ("PostGres" or "MSSQLServer")
	 * Description:	Add SQL statements
	 */		
	var addSQLStatements=function addSQLStatements(dbStream, csvFiles, srid, dbType) {
		
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
				tableList.push((xmlConfig.dataLoader.geoLevel[i].lookupTable.toLowerCase() || "lookup_" + csvFiles[i].tableName));
			}
			tableList.push("geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("geography_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			
			for (var i=0; i<tableList.length; i++) {												
				var sqlStmt=new Sql("Describe table " + tableList[i], 
					getSqlFromFile("describe_table.sql", dbType, tableList[i] /* Table name */), 
					sqlArray, dbType); 
				
				var sqlStmt=new Sql("Analyze table " + tableList[i], 
					getSqlFromFile("vacuum_analyze_table.sql", dbType, tableList[i] /* Table name */), 
					sqlArray, dbType); 
			} // End of for csvFiles loop			
		} // End of analyzeTables()		 

		/*
		 * Function: 	insertGeolevelsLookupTables()
		 * Parameters:	None
		 * Description:	Insert geoelvels lookup tables: SQL statements
		 */	 
		function insertGeolevelsLookupTables() {
			sqlArray.push(new Sql("Insert Geolevels lookup tables"));
			for (var i=0; i<csvFiles.length; i++) {			
				var sqlStmt=new Sql("Insert table " + (xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase(), 
					getSqlFromFile("insert_lookup_table.sql", dbType, 
						(xmlConfig.dataLoader.geoLevel[i].lookupTable || "lookup_" + csvFiles[i].tableName).toLowerCase() /* Table name */,
						xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toLowerCase()		/* shapefile table name */ ), 
					sqlArray, dbType);					
			}				
		} // End of insertGeolevelsLookupTables()

		/*
		 * Function: 	insertAdjacencyTable()
		 * Parameters:	None
		 * Description:	Insert adjacency table: SQL statements
		 */	 
		function insertAdjacencyTable() {				
			var sqlStmt=new Sql("Insert into adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase(),
				getSqlFromFile("insert_adjacency.sql", 
					dbType, 
					"adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* 1: adjacency table; e.g. adjacency_cb_2014_us_500k */,
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* 2: geometry table; e.g. geometry_cb_2014_us_500k */,
					xmlConfig.dataLoader.maxZoomlevel 								/* 3: Max zoomlevel */), 					
				sqlArray, dbType);		
		}
		
		/*
		 * Function: 	insertHierarchyTable()
		 * Parameters:	None
		 * Description:	Insert hierarchy table: SQL statements
		 */	 
		function insertHierarchyTable() {	
			var sqlStmt=new Sql("Create function check_hierarchy_" + 
					xmlConfig.dataLoader.geographyName.toLowerCase(),		
				getSqlFromFile("check_hierarchy_function.sql", 
					dbType, 
					xmlConfig.dataLoader.geographyName.toLowerCase() 			/* Geography */), 
				sqlArray, dbType);	
			
			var sqlStmt=new Sql("Comment function check_hierarchy_" + 
					xmlConfig.dataLoader.geographyName.toLowerCase(),
				getSqlFromFile("check_hierarchy_function_comment.sql", 
					dbType, 
					"check_hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Function name */), 
				sqlArray, dbType);		
				
			if (dbType == "PostGres") { 					
				var sqlStmt=new Sql("Insert into hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(),
					getSqlFromFile("insert_hierarchy.sql", 
						dbType, 
						xmlConfig.dataLoader.geographyName.toUpperCase()		/* 1: Geography */,
						xmlConfig.dataLoader.maxZoomlevel 						/* 2: Max zoomlevel */), 
					sqlArray, dbType);
			}
			if (dbType == "MSSQLServer") { 					
				var sqlStmt=new Sql("Insert into hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(),
					getSqlFromFile("insert_hierarchy.sql", 
						dbType, 
						xmlConfig.dataLoader.geographyName.toUpperCase()		/* 1: Geography */,
						"orig"							 						/* 2: Use geom_orig */), 
					sqlArray, dbType);
			}
			
			/* 
			 * Add SQL from <hierarchy_post_processing_sql> element in geoDataLoader.xml
			 */
			if (xmlConfig.hierarchy_post_processing_sql) {				
				var sqlStmt=new Sql("Post processing hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase(),
					xmlConfig.hierarchy_post_processing_sql, 
					sqlArray, dbType);
			}
			else {
				sqlArray.push(new Sql("No SQL from <hierarchy_post_processing_sql> element in geoDataLoader.xml"));
			}
			
			var sqlStmt=new Sql("Check intersctions  for geograpy: " + 
					xmlConfig.dataLoader.geographyName.toLowerCase(),
				getSqlFromFile("check_intersections.sql", 
					dbType, 
					xmlConfig.dataLoader.geographyName.toUpperCase() 		/* 1: Geography */), 
				sqlArray, dbType);
			
		} // End of insertHierarchyTable()
		
		/*
		 * Function: 	createGeolevelsTable()
		 * Parameters:	None
		 * Description:	Create geolevels meta data table: geolevels_<geographyName> 
		 *				SQL statements
		 */	 
		function createGeolevelsTable() {
			sqlArray.push(new Sql("Geolevels meta data"));			
								
			var sqlStmt=new Sql("Create geolevels meta data table",
				getSqlFromFile("create_geolevels_table.sql", undefined /* Common */, 
					"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* 1: geolevels table name */,
					"geography_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* 2: geography table name */
					), sqlArray, dbType); 
			
			var sqlStmt=new Sql("Comment geolevels meta data table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase(),		/* Table name */
					"Geolevels: hierarchy of level within a geography"					/* Comment */), sqlArray, dbType);
			
			var fieldArray = ['geography', 'geolevel_name', 'geolevel_id', 'description', 'lookup_table',
							  'lookup_desc_column', 'shapefile', 'shapefile_table', 'shapefile_area_id_column', 'shapefile_desc_column',
							  'resolution', 'comparea', 'listing', 'areaid_count',
							  'centroids_table', 'centroids_area_id_column', 'covariate_table'];
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
				'Total number of area IDs within the geolevel',
				'Centroids table',
				'Centroids area id column',
				'Covariate table'];
			for (var l=0; l< fieldArray.length; l++) {			
				var sqlStmt=new Sql("Comment geolevels meta data column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase(),		/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray, dbType);
			}		
			
			for (var i=0; i<csvFiles.length; i++) { // Main file process loop	
				var covariateTable='NULL';
				if (xmlConfig.dataLoader.geoLevel[i].covariateTable) {
					covariateTable="'" + xmlConfig.dataLoader.geoLevel[i].covariateTable.toUpperCase() +  "'";
				}
				var shapeFileAreaIdColumn='NULL';
				if (xmlConfig.dataLoader.geoLevel[i].shapeFileAreaIdColumn) {
					shapeFileAreaIdColumn="'" + xmlConfig.dataLoader.geoLevel[i].shapeFileAreaIdColumn.toUpperCase() +  "'";
				}
				var shapefileDescColumn='NULL';
				if (xmlConfig.dataLoader.geoLevel[i].shapefileDescColumn) {
					shapefileDescColumn="'" + xmlConfig.dataLoader.geoLevel[i].shapefileDescColumn.toUpperCase() +  "'";
				}
				var lookupDescColumn='NULL';
				if (xmlConfig.dataLoader.geoLevel[i].lookupDescColumn) {
					lookupDescColumn="'" + xmlConfig.dataLoader.geoLevel[i].lookupDescColumn.toUpperCase() +  "'";
				}
/*
 * Example data from config:
 *	 
 *	   <geolevelId>1</geolevelId>
 *     <geolevelName>cb_2014_us_nation_5m</geolevelName>
 *     <covariateTable/>
 *     <geolevelDescription>Theat a scale of 1:5,000,000</geolevelDescription>
 *     <lookupTable>lookup_cb_2014_us_nation_5m</lookupTable>
 *     <lookupDescColumn>AREANAME</lookupDescColumn>
 *     <shapeFileName>cb_2014_us_nation_5m.shp</shapeFileName>
 *     <shapeFileTable>CB_2014_US_NATION_5M</shapeFileTable>
 *     <shapeFileAreaIdColumn>GEOID</shapeFileAreaIdColumn>
 *     <shapefileDescColumn>NAME</shapefileDescColumn>
 *     <resolution>1</resolution>
 *     <comparea>1</comparea>
 *     <listing>0</listing>
 */
				var sqlStmt=new Sql("Insert geolevels meta data for: " + xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toLowerCase(), 
					getSqlFromFile("insert_geolevel.sql", 
						undefined /* Common */, 
						"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* 1: table; e.g. geolevels_cb_2014_us_county_500k */,
						xmlConfig.dataLoader.geographyName.toUpperCase() 				/* 2: geography; e.g. CB_2014_US_500K */,
						xmlConfig.dataLoader.geoLevel[i].geolevelName.toUpperCase()     /* 3: Geolevel name; e.g. CB_2014_US_COUNTY_500K */,
						xmlConfig.dataLoader.geoLevel[i].geolevelId 					/* 4: Geolevel id; e.g. 3 */,
						xmlConfig.dataLoader.geoLevel[i].geolevelDescription 			/* 5: Geolevel description; e.g. "The State-County at a scale of 1:500,000" */,
						(xmlConfig.dataLoader.geoLevel[i].lookupTable ||
						 "LOOKUP_" +  xmlConfig.dataLoader.geographyName).toUpperCase()	/* 6: lookup table; e.g. LOOKUP_CB_2014_US_COUNTY_500K */,
						xmlConfig.dataLoader.geoLevel[i].shapeFileName					/* 7: shapefile; e.g. cb_2014_us_county_500k */,
						xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toUpperCase()	/* 8: shapefile table; e.g. CB_2014_US_COUNTY_500K */,
						covariateTable													/* 9: covariate_table; e.g. CB_2014_US_500K_COVARIATES_CB_2014_US_COUNTY_500K */,
						shapeFileAreaIdColumn											/* 10: shapefile_area_id_column; e.g. COUNTYNS */,
						shapefileDescColumn												/* 11: shapefile_desc_column; e.g. NAME */,
						lookupDescColumn												/* 12: lookup_desc_column; e.g. AREANAME */,	
						xmlConfig.dataLoader.geoLevel[i].resolution 					/* 13: resolution: Can use a map for selection at this resolution (0/1) */,
						xmlConfig.dataLoader.geoLevel[i].comparea 						/* 14: comparea: Able to be used as a comparison area (0/1) */,
 						xmlConfig.dataLoader.geoLevel[i].listing						/* 15: listing: Able to be used in a disease map listing (0/1) */), 
					sqlArray, dbType);
			}				
		} // End of createGeolevelsTable()

		/*
		 * Function: 	checkAreas()
		 * Parameters:	Schema (rif_data.)
		 * Description:	check Turf and DB areas agree to within 1% (Postgres)/5% (SQL server)
		 *				Needs to be in a separate transaction (do NOT start one!)
		 */	 	
		function checkAreas(schema) {
			sqlArray.push(new Sql("Check areas"));		
			for (var i=0; i<csvFiles.length; i++) {	
					var sqlStmt=new Sql("Test Turf and DB areas agree to within 1% (Postgres)/5% (SQL server)",
				getSqlFromFile("area_check.sql", dbType, 
					"geom_" + xmlConfig.dataLoader.maxZoomlevel /* 1: geometry column; e.g. geom_11 */,
					csvFiles[i].tableName 						/* 2: table name; e.g. cb_2014_us_county_500k */), sqlArray, dbType);
			} // End of for csvFiles loop	
		
		} // End of checkAreas()	
		
		/*
		 * Function: 	createGeographyTable()
		 * Parameters:	None
		 * Description:	Create geography meta data table: geography_<geographyName> 
		 *				SQL statements
		 */			
		function createGeographyTable() {
			sqlArray.push(new Sql("Geography meta data"));	
		
			sqlArray.push(new Sql("Drop dependent objects: tiles view and generate_series() [MS SQL Server only]"));			
			if (dbType == "MSSQLServer") { 
				var sqlStmt=new Sql("Drop generate_series() function", 
					getSqlFromFile("drop_generate_series.sql", dbType), sqlArray, dbType); 
			}	
			var sqlStmt=new Sql("Drop dependent object - view " + "tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_view.sql", dbType, 
					"tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* View name */), sqlArray, dbType); 
					
			var sqlStmt=new Sql("Drop dependent object - FK table geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase(), // Drop first - FK
				getSqlFromFile("drop_table.sql", dbType, "geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), 
				sqlArray, dbType); 
				
			var sqlStmt=new Sql("Drop table geography_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, 
					"geography_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), sqlArray, dbType); 
	
			if (dbType == "MSSQLServer") { 			
				var sqlStmt=new Sql("Create generate_series() function", 
					getSqlFromFile("generate_series.sql", dbType), sqlArray, dbType); 
			}	
			
			var sqlStmt=new Sql("Create geography meta data table",
				getSqlFromFile("create_geography_table.sql", undefined /* Common */, 
					"geography_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), sqlArray, dbType);

			var sqlStmt=new Sql("Comment geography meta data table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"geography_" + xmlConfig.dataLoader.geographyName.toLowerCase(),/* Table name */
					"Hierarchial geographies. Usually based on Census geography"	/* Comment */), sqlArray, dbType);
			var partition=0;	
			if (dbType == "PostGres") {			// Only Postgres is partitioned
				partition=1;
			}
			var postalPopulationTable='NULL'; 	// Handle NULLs
			var postalPointColumn='NULL';
			if (xmlConfig.dataLoader.postalPopulationTable) {
				postalPopulationTable="'" + xmlConfig.dataLoader.postalPopulationTable.toUpperCase() + "'";
			}
			if (xmlConfig.dataLoader.postalPointColumn) {
				postalPointColumn="'" + xmlConfig.dataLoader.postalPointColumn.toUpperCase() + "'";
			}
			var sqlStmt=new Sql("Populate geography meta data table",
				getSqlFromFile("insert_geography.sql", 
					undefined /* Common */, 
					"geography_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* table; e.g. geography_cb_2014_us_county_500k */,
					xmlConfig.dataLoader.geographyName.toUpperCase() 				/* Geography; e.g. CB_2014_US_500K */,
					xmlConfig.dataLoader.geographyDesc 								/* Geography description; e.g. "United states to county level" */,
					"HIERARCHY_" + xmlConfig.dataLoader.geographyName.toUpperCase()	/* Hierarchy table; e.g. HIERARCHY_CB_2014_US_500K */,
					"GEOMETRY_" + xmlConfig.dataLoader.geographyName.toUpperCase()	/* Geometry table; e.g. GEOMETRY_CB_2014_US_500K */,
					"TILES_" + xmlConfig.dataLoader.geographyName.toUpperCase()	    /* Tile table; e.g. TILES_CB_2014_US_500K */,
					xmlConfig.dataLoader.srid 										/* SRID; e.g. 4269 */,
					defaultcomparea.toUpperCase()									/* Default comparision area, e.g. GEOID */,
					defaultstudyarea.toUpperCase()									/* Default study area, e.g. STATENS */,
					xmlConfig.dataLoader.minZoomlevel 								/* Min zoomlevel */,
					xmlConfig.dataLoader.maxZoomlevel 								/* Max zoomlevel */,
					postalPopulationTable											/* Postal population table */,
					postalPointColumn												/* Postal point column */,
					partition														/* partition (0/1) */,
					xmlConfig.dataLoader.maxGeojsonDigits							/* Max geojson digits */,
					"ADJACENCY_" + xmlConfig.dataLoader.geographyName.toUpperCase()	/* Adjacency table; e.g. ADJACENCY_CB_2014_US_500K */
					), 
				sqlArray, dbType);
			
			var fieldArray = ['geography', 'description', 'hierarchytable', 'geometrytable', 'tiletable', 
					'srid', 'defaultcomparea', 'defaultstudyarea', 'minzoomlevel', 'maxzoomlevel',
					'postal_population_table', 'postal_point_column', 'partition', 'max_geojson_digits', 'adjacencytable'];
			var fieldDescArray = ['Geography name', 
				'Description', 
				'Hierarchy table', 
				'Geometry table', 
				'Tile table', 
				'Projection SRID', 
				'Default comparison area: lowest resolution geolevel', 
				'Default study area: highest resolution geolevel',
				'Min zoomlevel',
				'Max zoomlevel',
				'Postal_population_table', 
				'Postal_point_column', 
				'Partition geometry and tile tables (0/1)', 
				'Maximum digits in geojson (topojson quantisation)',
				'Adjacency table'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment geography meta data column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"geography_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray, dbType);
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
					csvFiles[i].tableName /* Table name */), sqlArray, dbType); 

			var areaID=response.fields[csvFiles[i].tableName + "_areaID"];
			var areaID_desc=response.fields[csvFiles[i].tableName+ "_areaID_desc"];
			var areaName=response.fields[csvFiles[i].tableName + "_areaName"];
			var areaName_desc=response.fields[csvFiles[i].tableName+ "_areaName_desc"];
			var fieldComments = {
				gid: "Unique geographic index",
				areaid: "Area ID (" + areaID + "): " + areaID_desc,
				areaname: "Area name (" + areaName + "): " + areaName_desc,
				area_km2: "Area in square km",
				geographic_centroid_wkt: "Wellknown text for geographic centroid"
			};
			
			for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
				fieldComments["wkt_" + k]="Wellknown text for zoomlevel " + k;
			}
				
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
							columnList[j].toLowerCase(), false) + "\tNVARCHAR(1000)	NOT NULL";
					}
				}
				else if (columnList[j].match(/wkt/i)) {
					sqlStmt.sql+="\t" + pad("                               ", columnList[j].toLowerCase(), false) + "\ttext";	
				}
				else {
					if (dbType == "PostGres") {	
						sqlStmt.sql+="\t" + pad("                               ", columnList[j].toLowerCase(), false) + "\ttext";
					}
					else if (dbType == "MSSQLServer") {	
						sqlStmt.sql+="\t" + pad("                               ", columnList[j].toLowerCase(), false) + "\tNVARCHAR(1000)";
					}
				}
				
				var fieldKey=csvFiles[i].tableName + "_" + columnList[j].toUpperCase();
				var desc=response.fields[fieldKey];

				if (desc) {
					sqlStmt.sql+=" /* " + desc.replace(/'/g, "''") + " */";
					fieldComments[columnList[j].toLowerCase()]=desc.replace(/'/g, "''");
				}
				else {
					sqlStmt.sql+=" /* " + (fieldComments[columnList[j].toLowerCase()] || "No comment for: " + columnList[j].toLowerCase()) + " */";
				}
				
			} // End of for columnList loop
			
			sqlStmt.sql+=")";
			sqlStmt.dbType=dbType;
			sqlArray.push(sqlStmt);

			var sqlStmt=new Sql("Comment geospatial data table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					csvFiles[i].tableName,			/* Table name */
					csvFiles[i].geolevelDescription	/* Comment */),
				sqlArray, dbType);			
			
			for (var key in fieldComments) {
				var sqlStmt=new Sql("Comment geospatial data column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						csvFiles[i].tableName,			/* Table name */
						key,							/* Column name */
						fieldComments[key]				/* Comment */),
					sqlArray, dbType);					
			}
			
			// Needs to be SQL to psql command (i.e. COPY FROM stdin)
			var sqlStmt=new Sql("Load table from CSV file"); // CSV file comes from Tilemaker so is not DB dependent
			if (dbType == "PostGres") {	
				sqlStmt.sql="\\copy " + csvFiles[i].tableName + " FROM '" + csvFiles[i].tableName + 
					".csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8'";
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
			sqlStmt.dbType=dbType;
			sqlArray.push(sqlStmt);
			
			var sqlStmt=new Sql("Row check: " + csvFiles[i].rows.length,
				getSqlFromFile("csvfile_rowcheck.sql", 
					dbType, 
					csvFiles[i].tableName	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					csvFiles[i].rows.length /* 2: Expected number of rows; e.g. 3233 */,
					"gid"					/* 3: Column to count; e.g. gid */), 
				sqlArray, dbType);

			var sqlStmt=new Sql("Add primary key " + csvFiles[i].tableName, 
				getSqlFromFile("add_primary_key.sql", undefined /* Common */, 
					csvFiles[i].tableName	/* Table name */, 
					'gid'					/* Primary key */), sqlArray, dbType); 
			
			var sqlStmt=new Sql("Add unique key " + csvFiles[i].tableName, 
				getSqlFromFile("add_unique_key.sql", undefined /* Common */, 
					csvFiles[i].tableName 		/* 1: table; e.g. cb_2014_us_nation_5m */,
					csvFiles[i].tableName + "_uk" 	/* 2: constraint name; e.g. cb_2014_us_nation_5m_uk */,
					"areaid" 						/* 3: fields; e.g. areaid */), sqlArray, dbType); 

			sqlArray.push(new Sql("Add geometric  data"));		
			
			var sqlStmt=new Sql("Add geometry column: geographic centroid",
				getSqlFromFile("add_geometry_column.sql", dbType, 
					csvFiles[i].tableName 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geographic_centroid' 	/* 2: column name; e.g. geographic_centroid */,
					4326 					/* 3: Column SRID; e.g. 4326 */,
					'POINT' 				/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), sqlArray, dbType);	
				
			var sqlStmt=new Sql("Add geometry column for original SRID geometry",
				getSqlFromFile("add_geometry_column2.sql", dbType, 
					csvFiles[i].tableName 		/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geom_orig' 				/* 2: column name; e.g. geographic_centroid */,
					xmlConfig.dataLoader.srid	/* 3: Column SRID; e.g. 4326 */,
					'MULTIPOLYGON' 				/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */,
					""							/* 5: Schema (rif_data. or "") [NEVER USED IN POSTGRES] */), sqlArray, dbType);
	
			for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
				var sqlStmt=new Sql("Add geometry column for zoomlevel: " + k,
					getSqlFromFile("add_geometry_column.sql", dbType, 
						csvFiles[i].tableName 	/* 1: Table name; e.g. cb_2014_us_county_500k */,
						"geom_" + k 			/* 2: column name; e.g. geographic_centroid */,
						4326	/* 3: Column SRID; e.g. 4326 */,
						'MULTIPOLYGON' 			/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */), sqlArray, dbType);
			}
				
			if (dbType == "PostGres") {				

				var sqlStmt=new Sql("Update geographic centroid, geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" + 
"   SET geographic_centroid = ST_GeomFromText(geographic_centroid_wkt, 4326),\n";
				for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
					sqlStmt.sql+="" +
"       geom_" + k + " = \n" +
"       \t\tCASE ST_IsCollection(ST_GeomFromText(wkt_" + k + ", 4326)) /* Convert to Multipolygon */\n" +
"       \t\t\tWHEN true THEN 	ST_GeomFromText(wkt_" + k + ", 4326)\n" +
"       \t\t\tELSE 			ST_Multi(ST_GeomFromText(wkt_" + k + ", 4326))\n" +
"       \t\tEND,\n";
				}
				sqlStmt.sql+="" +
"       geom_orig = ST_Transform(\n" +
"       \t\tCASE ST_IsCollection(ST_GeomFromText(wkt_" + xmlConfig.dataLoader.maxZoomlevel + ", 4326)) /* Convert to Multipolygon */\n" +
"       \t\t\tWHEN true THEN 	ST_GeomFromText(wkt_" + xmlConfig.dataLoader.maxZoomlevel + ", 4326)\n" +
"       \t\t\tELSE 			ST_Multi(ST_GeomFromText(wkt_" + xmlConfig.dataLoader.maxZoomlevel + ", 4326))\n" +
"       \t\tEND, " + xmlConfig.dataLoader.srid + ")";
				sqlStmt.dbType=dbType;
				sqlArray.push(sqlStmt);
				
				var sqlStmt=new Sql("Make geometry columns valid");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" +
"   SET\n";
				for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
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
				sqlStmt.dbType=dbType;
				sqlArray.push(sqlStmt);
						
			}
			else if (dbType == "MSSQLServer") {					
/*
UPDATE sahsu_grd_level1
   SET geographic_centroid = geography::STGeomFromText(geographic_centroid_wkt, 4326),
       geom_6 = geography::STGeomFromText(wkt_6, 4326).MakeValid(),
       geom_7 = geography::STGeomFromText(wkt_7, 4326).MakeValid(),
       geom_8 = geography::STGeomFromText(wkt_8, 4326).MakeValid(),
       geom_9 = geography::STGeomFromText(wkt_9, 4326).MakeValid(),
       geom_10 = geography::STGeomFromText(wkt_10, 4326).MakeValid(),
       geom_11 = geography::STGeomFromText(wkt_11, 4326).MakeValid(),
       geom_orig = geometry::STGeomFromText(geometry::STGeomFromText(wkt_11, 4326).MakeValid().STAsText(), 27700);

 */
				var sqlStmt=new Sql("Update geographic centroid, geometry columns, handle polygons and mutlipolygons, convert highest zoomlevel to original SRID");
				sqlStmt.sql="UPDATE " + csvFiles[i].tableName + "\n" + 
						    "   SET geographic_centroid = geography::STGeomFromText(geographic_centroid_wkt, 4326),\n";							
				for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
					sqlStmt.sql+="       geom_" + k + " = geography::STGeomFromText(wkt_" + k + ", 4326).MakeValid(),\n";
				}	
				sqlStmt.sql+="" +
"       geom_orig = geometry::STGeomFromText(geometry::STGeomFromText(wkt_" + xmlConfig.dataLoader.maxZoomlevel+ ", 4326).MakeValid().STAsText(), " + 
					xmlConfig.dataLoader.srid + ")"; 
				sqlStmt.dbType=dbType;
				sqlArray.push(sqlStmt);
			}

			sqlArray.push(new Sql("Test geometry and make valid if required"));
			
			var sqlStmt=new Sql("Check validity of geometry columns");
			var selectFrag=undefined;
			for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
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
			sqlStmt.dbType=dbType;
			sqlArray.push(sqlStmt);		

//
// In SQL server, all polygons must have right hand orientation or bad things happen - like the area ~ one hemisphere
// as used to detect the problem
//
			sqlArray.push(new Sql("Make all polygons right handed"));
			for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
				var sqlStmt=new Sql("Make all polygons right handed for zoomlevel: " + k);
				if (dbType == "MSSQLServer") {	
					sqlStmt.sql=getSqlFromFile("force_rhr.sql", dbType, 
						"geom_" + k 			/* 1: geometry column; e.g. geom_6 */,
						csvFiles[i].tableName 	/* 2: table name; e.g. cb_2014_us_county_500k	*/);
					sqlStmt.dbType=dbType;
					sqlArray.push(sqlStmt);
				}
			}
			if (dbType == "PostGres") { // No geom_orig in SQL Server
				var sqlStmt=new Sql("Make all polygons right handed for original geometry", 
"UPDATE " + csvFiles[i].tableName + "\n" +
"   SET");
				for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
					sqlStmt.sql+="" +
"       geom_" + k + " = ST_ForceRHR(geom_" + k + "),\n";
				}
				sqlStmt.sql+="" +
"       geom_orig = ST_ForceRHR(geom_orig)";
				sqlStmt.dbType=dbType;
				sqlArray.push(sqlStmt);
			}
			
			sqlArray.push(new Sql("Test Turf and DB areas agree to within 1%"));
				
			sqlArray.push(new Sql("Create spatial indexes"));
			for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
				var sqlStmt=new Sql("Index geometry column for zoomlevel: " + k,
					getSqlFromFile("create_spatial_index.sql", dbType, 
						csvFiles[i].tableName + "_geom_" + k + "_gix"	/* Index name */,
						csvFiles[i].tableName  							/* Table name */, 
						"geom_" + k 									/* Index column(s) */), sqlArray, dbType); 
			}	

			if (dbType == "PostGres") { 
				var sqlStmt=new Sql("Index geometry column for original SRID geometry",
					getSqlFromFile("create_spatial_index.sql", dbType, 
						csvFiles[i].tableName + "_geom_orig_gix"	/* Index name */,
						csvFiles[i].tableName  						/* Table name */, 
						"geom_orig" 								/* Index column(s) */), sqlArray, dbType);			
			}
			else if (dbType == "MSSQLServer") {	
				var sqlStmt=new Sql("Index geometry column for original SRID geometry",
					getSqlFromFile("create_spatial_geometry_index.sql", 
						dbType, 
						csvFiles[i].tableName + "_geom_orig_gix"	/* 1: Index name */,
						csvFiles[i].tableName  						/* 2: Table name */,  
						"geom_orig"									/* 2: Geometry field name */,
						csvFiles[0].bbox[0]							/* 4: Xmin (4326); e.g. -179.13729006727 */,
						csvFiles[0].bbox[1]							/* 5: Ymin (4326); e.g. -14.3737802873213 */, 
						csvFiles[0].bbox[2]							/* 6: Xmax (4326); e.g.  179.773803959804 */,
						csvFiles[0].bbox[3]							/* 7: Ymax (4326); e.g. 71.352561 */), 
					sqlArray, dbType);				
			}
					
			sqlArray.push(new Sql("Reports"));	
			
			var sqlStmt=new Sql("Areas and centroids report",
				getSqlFromFile("area_centroid_report.sql", dbType, 
					"geom_" + xmlConfig.dataLoader.maxZoomlevel	/* 1: geometry column; e.g. geom_11 */,
					csvFiles[i].tableName  						/* Table name */), sqlArray, dbType);
			
			// Get default study and comparison areas
			defaultcomparea=xmlConfig.dataLoader.defaultcomparea; // E.g. cb_2014_us_nation_5m_areaID
			defaultstudyarea=xmlConfig.dataLoader.defaultstudyarea; // E.g. cb_2014_us_county_500k_areaID
		
		} // End of createShapeFileTable()

		/*
		 * Function: 	insertTilesTables()
		 * Parameters:	None
		 * Description:	Create tiles tables 
		 *				SQL statements
		 */			
		function insertTilesTables() {
			var sqlStmt;		
			
			sqlArray.push(new Sql("Create tile limits table"));
		
			if (dbType == "MSSQLServer") { 	
				var sqlStmt=new Sql("Create tileMaker_STMakeEnvelope()", 
					getSqlFromFile("tileMaker_STMakeEnvelope.sql", dbType), 
					sqlArray, dbType); 
			}
				
			var sqlStmt=new Sql("Drop table " + "tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_table.sql", dbType, 
					"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), sqlArray, dbType); 
		
			var sqlStmt=new Sql("Create table " + "tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("create_tile_limits_table.sql", dbType, 
					"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* 1: Tile limits table */,
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 2: Geometry table */,
					xmlConfig.dataLoader.maxZoomlevel 								/* 3: max_zoomlevel */), 
				sqlArray, dbType); 

			var sqlStmt=new Sql("Comment tile limits table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
					"Tile limits"	/* Comment */), sqlArray, dbType);
					
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
						"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray, dbType);
			}	

			if (dbType == "MSSQLServer") { 	
				var sqlStmt=new Sql("Make primary key not null",
					getSqlFromFile("not_null.sql", 
						dbType, 
						"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */,
						"zoomlevel"															/* Primary key */), 
					sqlArray, dbType);
			}			
			var sqlStmt=new Sql("Add primary key",
				getSqlFromFile("add_primary_key.sql", 
					undefined /* Common */, 
					"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */,
					"zoomlevel"									/* Primary key */), 
				sqlArray, dbType);	
			var sqlStmt=new Sql("Analyze table",
				getSqlFromFile("analyze_table.sql", 
					dbType, 
					"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */), 
				sqlArray, dbType);	
			var sqlStmt=new Sql("Analyze table",
				"SELECT zoomlevel, x_min, x_max, y_min, y_max, y_mintile, y_maxtile, x_mintile, x_maxtile FROM tile_limits_" + 
					xmlConfig.dataLoader.geographyName.toLowerCase(), 
				sqlArray, dbType);	
					
			if (dbType == "MSSQLServer") { 
				var sqlStmt=new Sql("Drop table " + "tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
					getSqlFromFile("drop_table.sql", dbType, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), sqlArray, dbType);
			}
			else if (dbType == "PostGres") { 
				var sqlStmt=new Sql("Drop table " + "tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
					getSqlFromFile("drop_table_cascade.sql", dbType, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* Table name */), sqlArray, dbType);						
			}
			
			if (dbType == "MSSQLServer") { // No JSON in SQL Server
				var sqlStmt=new Sql("Create tile intersects table",
					getSqlFromFile("create_tile_intersects_table.sql", 
						undefined /* Common */, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */,
						"Text"									/* JSON datatype (Postgres: JSON, MS SQL Server: Text) */,
						"bit"									/* STWithin() return datatype: bit (0/1) */), 
					sqlArray, dbType);					
			}					
			else if (dbType == "PostGres") { // No JSON in SQL Server					
				var sqlStmt=new Sql("Create tile intersects table",
					getSqlFromFile("create_tile_intersects_table.sql", 
						undefined /* Common */, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */,
						"JSON"									/* JSON datatype (Postgres: JSON, MS SQL Server: Text) */,
						"BOOLEAN"								/* ST_Within() return datatype: bit (0/1) */), 
					sqlArray, dbType);						
			}

			var sqlStmt=new Sql("Add geometry column: bbox",
				getSqlFromFile("add_geometry_column2.sql", dbType, 
					"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()
											/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'bbox' 					/* 2: column name; e.g. geographic_centroid */,
					4326 					/* 3: Column SRID; e.g. 4326 */,
					'POLYGON' 				/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */,
					""						/* 5: Schema (rif_data. or "") [NEVER USED IN POSTGRES] */), sqlArray, dbType);
			var sqlStmt=new Sql("Add geometry column: geom",
				getSqlFromFile("add_geometry_column2.sql", dbType, 
					"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()
											/* 1: Table name; e.g. cb_2014_us_county_500k */,
					'geom' 					/* 2: column name; e.g. geographic_centroid */,
					4326 					/* 3: Column SRID; e.g. 4326 */,
					'MULTIPOLYGON' 			/* 4: Spatial geometry type: e.g. POINT, MULTIPOLYGON */,
					""						/* 5: Schema (rif_data. or "") [NEVER USED IN POSTGRES] */), sqlArray, dbType);
					
			var sqlStmt=new Sql("Comment tile intersects table",
				getSqlFromFile("comment_table.sql", 
					dbType, 
					"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Table name */
					"Tile area id intersects"	/* Comment */), sqlArray, dbType);
					
			var fieldArray = ['geolevel_id', 'zoomlevel', 'areaid', 'x', 'y', 'optimised_geojson',
				'within', 'bbox', 'geom'];
			var fieldDescArray = ['ID for ordering (1=lowest resolution). Up to 99 supported.',
				'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11',
				'Area ID',
				'X tile number. From 0 to (2**<zoomlevel>)-1',
				'Y tile number. From 0 to (2**<zoomlevel>)-1',
				'Tile areaid intersect multipolygon in GeoJSON format, optimised for zoomlevel N.',
				'Defined as: ST_Within(bbox, geom). Used to exclude any tile bounding completely within the area.',
				'Bounding box of tile as a polygon.',
				'Geometry of area.'];
			for (var l=0; l< fieldArray.length; l++) {		
				var sqlStmt=new Sql("Comment tile intersects table column",
					getSqlFromFile("comment_column.sql", 
						dbType, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase(),/* Table name */
						fieldArray[l]														/* Column name */,
						fieldDescArray[l]													/* Comment */), 
					sqlArray, dbType);
			}			

			if (dbType == "PostGres") { // Partition Postgres
				var sqlStmt=new Sql("Create partitioned tables and insert function for tile intersects table; comment partitioned tables and columns",
					getSqlFromFile("partition_tile_intersects_table.sql", 
						dbType, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* 1: Tile iontersects table name */,
						xmlConfig.dataLoader.maxZoomlevel									/* 2: Max zoomlevel; e.g. 11 */,
						"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 3: Geolevels table; 
																								e.g. geolevels_cb_2014_us_500k */,
						csvFiles.length 													/* 4: Number of geolevels (e.g. 3) */), 
					sqlArray, dbType);			

				var sqlStmt=new Sql("Partition tile intersects table: insert trigger",
					getSqlFromFile("partition_trigger.sql", 
						dbType, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */), 
					sqlArray, dbType);	
					
				var sqlStmt=new Sql("Comment partition tile intersects table: insert trigger",
					getSqlFromFile("comment_partition_trigger.sql", 
						dbType, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */), 
					sqlArray, dbType);						
			}

			var sqlStmt=new Sql("INSERT into tile intersects table",
				getSqlFromFile("tile_intersects_insert.sql", 
					dbType, 
					"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase(),	/* Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
					"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase(),		/* Tile limits table name; e.g. tile_limits_cb_2014_us_500k */
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()			/* Geometry table name; e.g. geometry_cb_2014_us_500k */
					), sqlArray, dbType);

					
			if (dbType == "PostGres") { 
				var sqlStmt=new Sql("Add primary key",
					getSqlFromFile("add_primary_key.sql", 
						undefined /* Common */, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Tile intersects table name */,
						"geolevel_id, zoomlevel, areaid, x, y"									/* Primary key */), 
					sqlArray, dbType);	
			}
			else if (dbType == "MSSQLServer") { // Force PK to be non clustered so inserts are fast
				var sqlStmt=new Sql("Add non clustered primary key",
					getSqlFromFile("add_primary_key.sql", 
						dbType, 
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Tile intersects table name */,
						"geolevel_id, zoomlevel, areaid, x, y"									/* Primary key */), 
					sqlArray, dbType);	
			}	
			
			var sqlStmt=new Sql("Analyze table",
				getSqlFromFile("analyze_table.sql", 
					dbType, 
					"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */), 
				sqlArray, dbType);	
				
			var sqlStmt=new Sql("SELECT from tile intersects table",
				getSqlFromFile("tile_intersects_select.sql", 
					dbType, 
					"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
					), sqlArray, dbType);

			if (dbType == "PostGres") { // Postgres tile manufacture
				var sqlStmt=new Sql("Create tile intersects table INSERT function",
					getSqlFromFile("tileMaker_intersector_function.sql", 
						dbType, 
						"tileMaker_intersector_" + xmlConfig.dataLoader.geographyName.toLowerCase()	
																							/* 1: function name; e.g. tileMaker_intersector_cb_2014_us_500k */,
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* 2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k */,
						"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 3: tile limits table; e.g. tile_limits_cb_2014_us_500k */,
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 4: geometry table; e.g. geometry_cb_2014_us_500k */), 
					sqlArray, dbType);

				var sqlStmt=new Sql("Create second tile intersects table INSERT function (simplification errors)",
					getSqlFromFile("tileMaker_intersector_function2.sql", 
						dbType, 
						"tileMaker_intersector2_" + xmlConfig.dataLoader.geographyName.toLowerCase()	
																							/* 1: function name; e.g. tileMaker_intersector2_cb_2014_us_500k */,
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* 2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k */,
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 3: geometry table; e.g. geometry_cb_2014_us_500k */), 
					sqlArray, dbType);		

				var sqlStmt=new Sql("Create tiles table INSERT function (tile aggregator)",
					getSqlFromFile("tileMaker_aggregator_function.sql", 
						dbType, 
						"tileMaker_aggregator_" + xmlConfig.dataLoader.geographyName.toLowerCase()	
																							/* 1: function name; e.g. tileMaker_aggregator_cb_2014_us_500k */,
						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* 2: tile intersects table; e.g. tile_intersects_cb_2014_us_500k */,
						"t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase()			/* 3: tiles table; e.g. t_tiles_cb_2014_us_500k */,
						"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 4: geolevels table; e.g. geolevels_cb_2014_us_500k */), 
					sqlArray, dbType);		

				var sqlStmt=new Sql("Create tiles table INSERT function (tile aggregator)",
					getSqlFromFile("tileMaker_main_function.sql", 
						dbType, 
						xmlConfig.dataLoader.geographyName.toLowerCase()						/* 1: geography; e.g. cb_2014_us_500k */,
 						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 2: geometry table; e.g. geometry_cb_2014_us_500k */,
 						"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 3: geolevels table; e.g. geolevels_cb_2014_us_500k */), 
					sqlArray, dbType);				
					
			}	
			else if (dbType == "MSSQLServer") { // MSSQLServer tile manufacture
				var sqlStmt=new Sql("INSERT into tile intersects table (MSSQLServer tile manufacture)",
					getSqlFromFile("tile_intersects_insert2.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()			/* 1: Geometry table name; e.g. geometry_cb_2014_us_500k */,
 						"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase()			/* 2: Geolevels table name; e.g. geolevels_cb_2014_us_500k */,
 						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* 3: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */,
						"tile_limits_" + xmlConfig.dataLoader.geographyName.toLowerCase() 		/* 4: Tile limits table name; e.g. tile_limits_cb_2014_us_500k */
						), sqlArray, dbType);
						
				var sqlStmt=new Sql("Special index on tile intersects table for MS SQL tuning",
					getSqlFromFile("tile_intersects_usa_2014_tlidx.sql", 
						dbType, 
 						"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* 1: Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
						), sqlArray, dbType);	
			} 
			
			var sqlStmt=new Sql("Tile intersects table % savings",
				getSqlFromFile("tile_intersects_select2.sql", 
					dbType, 
					"tile_intersects_" + xmlConfig.dataLoader.geographyName.toLowerCase()	/* Tile intersects table name; e.g. tile_intersects_cb_2014_us_500k */
					), sqlArray, dbType);				
					
		} // End of createTilesTables()

		/*
		 * Function: 	insertGeometryTable()
		 * Parameters:	None
		 * Description:	Insert geometry table 
		 *				SQL statements
		 */			
		function insertGeometryTable() {		
		
			sqlArray.push(new Sql("Insert geometry table"));
			var sqlFrag=undefined;
			for (var i=0; i<csvFiles.length; i++) { // Main file process loop				
				for (var k=xmlConfig.dataLoader.minZoomlevel; k <= xmlConfig.dataLoader.maxZoomlevel; k++) {
					sqlFrag="INSERT INTO geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
						"(geolevel_id, areaid, zoomlevel, geom)\n";
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
						sqlArray, dbType);
				} // End of for zoomlevels loop
			} // End of main file process loop			

			if (dbType == "MSSQLServer") { // Update bounding box for implement PostGIS && operator
				var sqlStmt=new Sql("Update bounding box for implement PostGIS && operator",
					getSqlFromFile("geometry_bbox_update.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */), 
					sqlArray, dbType);
			
			}
			
			if (dbType == "PostGres") { // Partition Postgres
				var sqlStmt=new Sql("Add primary key, index and cluster (convert to index organized table)",
					getSqlFromFile("partition_geometry_table2.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */,
						xmlConfig.dataLoader.maxZoomlevel									/* 2: Max zoomlevel; e.g. 11 */,
						csvFiles.length														/* 3: Number of geolevels (e.g. 3) */), 
					sqlArray, dbType);	
			}		
			else if (dbType == "MSSQLServer") { 
			
				sqlArray.push(new Sql("No partitioning on SQL Server as it requires an Enterprise license; which"));
				sqlArray.push(new Sql("means you have to do it yourself using the generated scripts as a start.")); // Comment
					
				// Add primary key, index and cluster (convert to index organized table)
				var sqlStmt=new Sql("Add primary key",
					getSqlFromFile("add_primary_key.sql", 
						undefined /* Common */, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */,
						"geolevel_id, areaid, zoomlevel"									/* Primary key */), 
					sqlArray, dbType);	
				var sqlStmt=new Sql("Create spatial index on geom",
					getSqlFromFile("create_spatial_geometry_index.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "_gix"	/* Index name */, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()			/* Table name */, 
						"geom"																	/* Geometry field name */,
						csvFiles[0].bbox[0]														/* 4: Xmin (4326); e.g. -179.13729006727 */,
						csvFiles[0].bbox[1]														/* 5: Ymin (4326); e.g. -14.3737802873213 */, 
						csvFiles[0].bbox[2]														/* 6: Xmax (4326); e.g.  179.773803959804 */,
						csvFiles[0].bbox[3]														/* 7: Ymax (4326); e.g. 71.352561 */), 
					sqlArray, dbType);	
					
				var sqlStmt=new Sql("Create spatial index on bbox",
					getSqlFromFile("create_spatial_geometry_index.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "_gix2"	/* Index name */, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()			/* Table name */, 
						"bbox"																	/* Geometry field name */,
						csvFiles[0].bbox[0]														/* 4: Xmin (4326); e.g. -179.13729006727 */,
						csvFiles[0].bbox[1]														/* 5: Ymin (4326); e.g. -14.3737802873213 */, 
						csvFiles[0].bbox[2]														/* 6: Xmax (4326); e.g.  179.773803959804 */,
						csvFiles[0].bbox[3]														/* 7: Ymax (4326); e.g. 71.352561 */), 
					sqlArray, dbType);	
					
				var sqlStmt=new Sql("Analyze table",
					getSqlFromFile("analyze_table.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */), 
					sqlArray, dbType);	
			}
					
			var sqlStmt=new Sql("Update areaid_count column in geolevels table using geometry table", 
				getSqlFromFile("geolevels_areaid_update.sql", 
					dbType, 
					"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* 1: Geolevels table */,
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 2: Geometry table */,
					""																/* 3: Schema; e.g. rif_data. or "" */,
					xmlConfig.dataLoader.geographyName.toUpperCase() 				/* 4: Geography */), 
				sqlArray, dbType);
					
			var sqlStmt=new Sql("Check areaid_count column in geolevels table using geometry table", 
				getSqlFromFile("geolevels_areaid_check.sql", 
					dbType, 
					"geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* 1: Geolevels table */,
					""																/* 2: Schema; e.g. rif_data. or "" */), 
				sqlArray, dbType);
				
		} // End of insertGeometryTable()
/*
psql -d sahsuland_dev -U peter -w -e -f pg_cb_2014_us_500k.sql
sqlcmd -E -b -m-1 -e -r1 -i mssql_cb_2014_us_500k.sql -v pwd="%cd%"
*/	
		var sqlArray=[];
		
		beginTransaction(sqlArray, dbType);

		var sqlStmt=new Sql("NON RIF initialisation", 
			getSqlFromFile("startup.sql", dbType), 
			sqlArray, dbType);
			
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
		createGeolevelsLookupTables(sqlArray, dbType, undefined /* No schema - use default */);
		insertGeolevelsLookupTables();
		createHierarchyTable(sqlArray, dbType, undefined /* No schema - use default */);
		insertHierarchyTable();
		createGeometryTable(sqlArray, dbType, undefined /* No schema - use default */);
		insertGeometryTable();
		createAdjacencyTable(sqlArray, dbType, undefined /* No schema - use default */);
		insertAdjacencyTable();
		
		var geoLevelsTable="geolevels_" + xmlConfig.dataLoader.geographyName.toLowerCase();
		createTilesTables(sqlArray, dbType, geoLevelsTable, undefined /* No schema - use default */);
		insertTilesTables();
		
		commitTransaction(sqlArray, dbType);
		
		analyzeTables();
		checkAreas();

//
// Write SQL statements to file
//		
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
	
//
// End of load specific SQL
// Production specific SQL
//	
	/*
	 * Function: 	addSQLLoadStatements()
	 * Parameters:	Database stream, format file stream, CSV files object, srid (spatial reference identifier), 
	 *				dbbase type as a string ("PostGres" or "MSSQLServer")
	 * Description:	Add SQL statements for RIF database load
	 */		
	var addSQLLoadStatements=function addSQLLoadStatements(dbStream, csvFiles, srid, dbType) {

		/*
		 * Function: 	grantTables()
		 * Parameters:	Schema (rif_data.)
		 * Description:	Grant SELECT to rif_user on all tables: SQL statements
		 *				Needs to be in a separate transaction (do NOT start one!)
		 */	
		function grantTables(schema) {
			sqlArray.push(new Sql("Analyze tables"));
			var tableList=[];
			for (var i=0; i<csvFiles.length; i++) {
				tableList.push((xmlConfig.dataLoader.geoLevel[i].lookupTable.toLowerCase() || "lookup_" + csvFiles[i].tableName));
			}
			tableList.push("hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase());
						
			for (var i=0; i<tableList.length; i++) {												
				var sqlStmt=new Sql("Grant table/view " + tableList[i], 
					getSqlFromFile("grant_table.sql", undefined /* Common */, 
						(schema||"") + tableList[i] /* 1. Table name */,
						'SELECT'					/* 2: Privileges */,
						'rif_user, rif_manager'		/* 3: Roles */), 
					sqlArray, dbType); 
			}
			
		} // End of grantTables
		
		/*
		 * Function: 	analyzeTables()
		 * Parameters:	Schema (rif_data.)
		 * Description:	Analyze and describe all tables: SQL statements
		 *				Needs to be in a separate transaction (do NOT start one!)
		 */	 	
		function analyzeTables(schema) {
			sqlArray.push(new Sql("Analyze tables"));
			var tableList=[];
			for (var i=0; i<csvFiles.length; i++) {
				tableList.push((xmlConfig.dataLoader.geoLevel[i].lookupTable.toLowerCase() || "lookup_" + csvFiles[i].tableName));
			}
			tableList.push("hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			tableList.push("t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase());
			
			for (var i=0; i<tableList.length; i++) {												
				var sqlStmt=new Sql("Describe table " + tableList[i], 
					getSqlFromFile("describe_table.sql", dbType, (schema||"") + tableList[i] /* Table name */), 
					sqlArray, dbType); 
				
				var sqlStmt=new Sql("Analyze table " + tableList[i], 
					getSqlFromFile("vacuum_analyze_table.sql", dbType, (schema||"") + tableList[i] /* Table name */), 
					sqlArray, dbType); 
			} // End of for csvFiles loop			
		} // End of analyzeTables()		 	 
		
		/*
		 * Function: 	setupGeography()
		 * Parameters:	Schema (rif_data.)
		 * Description:	Setup t_rif40_geolevels and rif40_geographies
		 */	
		function setupGeography(schema) {
			
			if (dbType == "MSSQLServer") { 			
				var sqlStmt=new Sql("Set comment schema path to rif_data")
				sqlStmt.nonsql=':SETVAR SchemaName @CurrentUser';
				sqlStmt.dbType=dbType;
				sqlArray.push(sqlStmt);
			}
	
			var fkList = {
				rif40_covariates: ['rif40_covariates_geolevel_fk', 'rif40_covariates_geog_fk'],
				t_rif40_studies: ['t_rif40_std_study_geolevel_fk', 't_rif40_std_comp_geolevel_fk', 
					't_rif40_studies_geography_fk'],
				t_rif40_inv_covariates: ['t_rif40_inv_cov_geography_fk', 't_rif40_inv_cov_geolevel_fk']					
			};
			var tableList=Object.keys(fkList);	
				
			if (dbType == "MSSQLServer") { 			
				for (var i=0; i<tableList.length; i++) {	
					var fkArray=fkList[tableList[i]];		
					for (var j=0; j<fkArray.length; j++) {
						var sqlStmt=new Sql("Disable constraints: " + fkArray[j] + " on: " + tableList[i],
							getSqlFromFile("enable_disable_constraint.sql", 
								dbType,
								"rif40."						/* 1: Schema */,
								tableList[i]					/* 2: Table */,
								"NOCHECK"						/* 3: DISABLE/ENABLE */,
								fkArray[j]						/* 4: Constraint */),
							sqlArray, dbType);									
					}
				}								
			}
			else if (dbType == "PostGres") { 
		
				for (var i=0; i<tableList.length; i++) {	
					var fkArray=fkList[tableList[i]];		
					for (var j=0; j<fkArray.length; j++) {
						var sqlStmt=new Sql("Disable constraints: " + fkArray[j] + " on: " + tableList[i],
							getSqlFromFile("drop_constraint.sql", 
								dbType,
								tableList[i]	/* 1: Table */,
								fkArray[j]		/* 2: Constraint */),
							sqlArray, dbType);		
					}
				}									
			}
			
			var newColumnList=['geometrytable', 'tiletable', 'minzoomlevel', 'maxzoomlevel', 'adjacencytable'];
			var newColumnDataType=['VARCHAR(30)', 'VARCHAR(30)', 'INTEGER', 'INTEGER', 'VARCHAR(30)'];
			var newColumnComment=['Geometry table name', 'Tile table name', 'Minimum zoomlevel', 'Maximum zoomlevel', 'Adjacency table'];			
			new Sql("Remove old geolevels meta data table",
					"DELETE FROM t_rif40_geolevels WHERE geography = '" + 
						xmlConfig.dataLoader.geographyName.toUpperCase() + "'", 
					sqlArray, dbType);			
			new Sql("Remove old geography meta data table",
					"DELETE FROM rif40_geographies WHERE geography = '" + 
						xmlConfig.dataLoader.geographyName.toUpperCase() + "'", 
					sqlArray, dbType);		
					
			for (var i=0; i<newColumnList.length; i++) {
				var sqlStmt=new Sql("Setup geography meta data table column: " + newColumnList[i],
					getSqlFromFile("add_column.sql", 
						dbType,
						"rif40_geographies"			/* 1: Table */, 
						newColumnList[i]			/* 2: Column */, 
						newColumnDataType[i]		/* 3: Datatype */), 
					sqlArray, dbType);	
				var sqlStmt=new Sql("Comment geography meta data table column" + newColumnList[i],
					getSqlFromFile("comment_column.sql", 
						dbType,
						"rif40_geographies"			/* 1: Table */, 
						newColumnList[i]			/* 2: Column */, 
						newColumnComment[i]			/* 3: Comment */), 
					sqlArray, dbType);	
			}


			var newColumnList=['areaid_count'];
			var newColumnDataType=['INTEGER'];
			var newColumnComment=['Area ID count'];
			for (var i=0; i<newColumnList.length; i++) {
				var sqlStmt=new Sql("Setup geolevels meta data table column: " + newColumnList[i],
					getSqlFromFile("add_column.sql", 
						dbType,
						"t_rif40_geolevels"			/* 1: Table */, 
						newColumnList[i]			/* 2: Column */, 
						newColumnDataType[i]		/* 3: Datatype */), 
					sqlArray, dbType);	
				var sqlStmt=new Sql("Comment geolevels meta data table column" + newColumnList[i],
					getSqlFromFile("comment_column.sql", 
						dbType,
						"t_rif40_geolevels"			/* 1: Table */, 
						newColumnList[i]			/* 2: Column */, 
						newColumnComment[i]			/* 3: Comment */), 
					sqlArray, dbType);	
			}
			var sqlStmt=new Sql("Recreate rif40_geolevels view with new columns", // Add if needed
				getSqlFromFile("rif40_geolevels_view.sql", 
					dbType), 
				sqlArray, dbType);	 
					
			if (dbType == "MSSQLServer") { 
				var sqlStmt=new Sql("Set comment schema path to rif_data")
				sqlStmt.nonsql=':SETVAR SchemaName "rif_data"';
				sqlStmt.dbType=dbType;
				sqlArray.push(sqlStmt);
			}
			
			var partition=0;	
			if (dbType == "PostGres") {			// Only Postgres is partitioned
				partition=1;
			}
			var postalPopulationTable='NULL'; 	// Handle NULLs
			var postalPointColumn='NULL';
			if (xmlConfig.dataLoader.postalPopulationTable) {
				postalPopulationTable="'" + xmlConfig.dataLoader.postalPopulationTable.toUpperCase() + "'";
			}
			if (xmlConfig.dataLoader.postalPointColumn) {
				postalPointColumn="'" + xmlConfig.dataLoader.postalPointColumn.toUpperCase() + "'";
			}
					
			var sqlStmt=new Sql("Populate geography meta data table",
				getSqlFromFile("insert_geography.sql", 
					undefined /* Common */, 
					"rif40_geographies"												/* table; e.g. rif40_geographies */,
					xmlConfig.dataLoader.geographyName.toUpperCase() 				/* Geography; e.g. CB_2014_US_500K */,
					xmlConfig.dataLoader.geographyDesc 								/* Geography description; e.g. "United states to county level" */,
					"HIERARCHY_" + xmlConfig.dataLoader.geographyName.toUpperCase()	/* Hierarchy table; e.g. HIERARCHY_CB_2014_US_500K */,
					"GEOMETRY_" + xmlConfig.dataLoader.geographyName.toUpperCase()	/* Geometry table; e.g. GEOMETRY_CB_2014_US_500K */,
					"TILES_" + xmlConfig.dataLoader.geographyName.toUpperCase()	    /* Tile table; e.g. TILES_CB_2014_US_500K */,
					xmlConfig.dataLoader.srid 										/* SRID; e.g. 4269 */,
					xmlConfig.dataLoader.defaultcomparea.toUpperCase()				/* Default comparision area, e.g. GEOID */,
					xmlConfig.dataLoader.defaultstudyarea.toUpperCase()				/* Default study area, e.g. STATENS */,
					xmlConfig.dataLoader.minZoomlevel 								/* Min zoomlevel */,
					xmlConfig.dataLoader.maxZoomlevel 								/* Max zoomlevel */,
					postalPopulationTable											/* Postal population table */,
					postalPointColumn												/* Postal point column */,
					partition														/* partition (0/1) */,
					xmlConfig.dataLoader.maxGeojsonDigits							/* Max geojson digits */,
					"ADJACENCY_" + xmlConfig.dataLoader.geographyName.toUpperCase()	/* Adjacency table; e.g. ADJACENCY_CB_2014_US_500K */
					), 
				sqlArray, dbType);	
				
			for (var i=0; i<csvFiles.length; i++) { // Main file process loop	
				var covariateTable='NULL';
				if (xmlConfig.dataLoader.geoLevel[i].covariateTable) {
					covariateTable="'" + xmlConfig.dataLoader.geoLevel[i].covariateTable.toUpperCase() +  "'";
				}
				var shapeFileAreaIdColumn='NULL';
				if (xmlConfig.dataLoader.geoLevel[i].shapeFileAreaIdColumn) {
					shapeFileAreaIdColumn="'" + xmlConfig.dataLoader.geoLevel[i].shapeFileAreaIdColumn.toUpperCase() +  "'";
				}
				var shapefileDescColumn='NULL';
				if (xmlConfig.dataLoader.geoLevel[i].shapefileDescColumn) {
					shapefileDescColumn="'" + xmlConfig.dataLoader.geoLevel[i].shapefileDescColumn.toUpperCase() +  "'";
				}
				var lookupDescColumn='NULL';
				if (xmlConfig.dataLoader.geoLevel[i].lookupDescColumn) {
					lookupDescColumn="'" + xmlConfig.dataLoader.geoLevel[i].lookupDescColumn.toUpperCase() +  "'";
				}
/*
 * Example data from config:
 *	 
 *	   <geolevelId>1</geolevelId>
 *     <geolevelName>cb_2014_us_nation_5m</geolevelName>
 *     <covariateTable/>
 *     <geolevelDescription>Theat a scale of 1:5,000,000</geolevelDescription>
 *     <lookupTable>lookup_cb_2014_us_nation_5m</lookupTable>
 *     <lookupDescColumn>AREANAME</lookupDescColumn>
 *     <shapeFileName>cb_2014_us_nation_5m.shp</shapeFileName>
 *     <shapeFileTable>CB_2014_US_NATION_5M</shapeFileTable>
 *     <shapeFileAreaIdColumn>GEOID</shapeFileAreaIdColumn>
 *     <shapefileDescColumn>NAME</shapefileDescColumn>
 *     <resolution>1</resolution>
 *     <comparea>1</comparea>
 *     <listing>0</listing>
 */
				if (xmlConfig.dataLoader.geoLevel[i].covariateTable) {
					var sqlStmt=new Sql("Create (if required) geolevels covariate table for: " + 
							xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toLowerCase(), 
						getSqlFromFile("create_covariate_table.sql", 
							dbType, 
							xmlConfig.dataLoader.geoLevel[i].covariateTable					/* 1: covariate_table; e.g. COV_CB_2014_US_STATE_500K */,
							xmlConfig.dataLoader.geoLevel[i].geolevelName.toLowerCase()		/* 2: Geolevel name: CB_2014_US_STATE_500K */,
							(schema||"")													/* 3: Schema; e.g. rif_data. or "" */),
						sqlArray, dbType);
		
					var sqlStmt=new Sql("Comment covariate table",
						getSqlFromFile("comment_table.sql", 
							dbType,
							xmlConfig.dataLoader.geoLevel[i].covariateTable					/* 1: covariate table name */,
							"Example covariate table for: "	+
								xmlConfig.dataLoader.geoLevel[i].geolevelDescription		/* 2: Comment */), 
						sqlArray, dbType);		
					var sqlStmt=new Sql("Comment covariate year column",
						getSqlFromFile("comment_column.sql", 
							dbType,
							xmlConfig.dataLoader.geoLevel[i].covariateTable					/* 1: covariate table name */,
							"year"															/* 2: Column */, 
							"Year"															/* 3: Comment */), 
						sqlArray, dbType);	
					var sqlStmt=new Sql("Comment covariate year column",
						getSqlFromFile("comment_column.sql", 
							dbType,
							xmlConfig.dataLoader.geoLevel[i].covariateTable					/* 1: covariate table name */,
							xmlConfig.dataLoader.geoLevel[i].geolevelName.toLowerCase()		/* 2: Column */, 
							"Geolevel name"													/* 3: Comment */), 
						sqlArray, dbType);	
				}
				
				var sqlStmt=new Sql("Insert geolevels meta data for: " + xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toLowerCase(), 
					getSqlFromFile("insert_geolevel.sql", 
						undefined /* Common */, 
						"t_rif40_geolevels" 											/* 1: covariate table; e.g. rif40_geolevels */,
						xmlConfig.dataLoader.geographyName.toUpperCase() 				/* 2: geography; e.g. CB_2014_US_500K */,
						xmlConfig.dataLoader.geoLevel[i].geolevelName.toUpperCase()     /* 3: Geolevel name; e.g. CB_2014_US_COUNTY_500K */,
						xmlConfig.dataLoader.geoLevel[i].geolevelId 					/* 4: Geolevel id; e.g. 3 */,
						xmlConfig.dataLoader.geoLevel[i].geolevelDescription 			/* 5: Geolevel description; e.g. "The State-County at a scale of 1:500,000" */,
						(xmlConfig.dataLoader.geoLevel[i].lookupTable ||
						 "LOOKUP_" +  xmlConfig.dataLoader.geographyName).toUpperCase()	/* 6: lookup table; e.g. LOOKUP_CB_2014_US_COUNTY_500K */,
						xmlConfig.dataLoader.geoLevel[i].shapeFileName					/* 7: shapefile; e.g. cb_2014_us_county_500k */,
						xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toUpperCase()	/* 8: shapefile table; e.g. CB_2014_US_COUNTY_500K */,
						covariateTable													/* 9: covariate_table; e.g. CB_2014_US_500K_COVARIATES_CB_2014_US_COUNTY_500K */,
						shapeFileAreaIdColumn											/* 10: shapefile_area_id_column; e.g. COUNTYNS */,
						shapefileDescColumn												/* 11: shapefile_desc_column; e.g. NAME */,
						lookupDescColumn												/* 12: lookup_desc_column; e.g. AREANAME */,	
						xmlConfig.dataLoader.geoLevel[i].resolution 					/* 13: resolution: Can use a map for selection at this resolution (0/1) */,
						xmlConfig.dataLoader.geoLevel[i].comparea 						/* 14: comparea: Able to be used as a comparison area (0/1) */,
 						xmlConfig.dataLoader.geoLevel[i].listing						/* 15: listing: Able to be used in a disease map listing (0/1) */), 
					sqlArray, dbType);

			} // End of for loop

			sqlArray.push(new Sql("Re-enable foreign key constraints"));			
			
			if (dbType == "MSSQLServer") { 		
	
				var fkList = {
					rif40_covariates: ['rif40_covariates_geolevel_fk', 'rif40_covariates_geog_fk'],
					t_rif40_studies: ['t_rif40_std_study_geolevel_fk', 't_rif40_std_comp_geolevel_fk', 
						't_rif40_studies_geography_fk'],
					t_rif40_inv_covariates: ['t_rif40_inv_cov_geography_fk', 't_rif40_inv_cov_geolevel_fk']					
				};
				var tableList=Object.keys(fkList);	
			
				for (var i=0; i<tableList.length; i++) {	
					var fkArray=fkList[tableList[i]];		
					for (var j=0; j<fkArray.length; j++) {
						var sqlStmt=new Sql("Enable constraints: " + fkArray[j] + " on: " + tableList[i],
							getSqlFromFile("enable_disable_constraint.sql", 
								dbType,
								"rif40."						/* 1: Schema */,
								tableList[i]					/* 2: Table */,
								"CHECK"							/* 3: DISABLE/ENABLE */,
								fkArray[j]						/* 4: Constraint */),
							sqlArray, dbType);									
					}
				}								
			}
			else if (dbType == "PostGres") { 
			
				var fkList = {
					rif40_covariates: {
							constraints: [{
									constraintName: 		'rif40_covariates_geolevel_fk',
									foreignKey: 			'geography, geolevel_name',
									referencedTableColumns: 't_rif40_geolevels (geography, geolevel_name)'
								}, {
									constraintName: 		'rif40_covariates_geog_fk',
									foreignKey: 			'geography',
									referencedTableColumns: 'rif40_geographies (geography)'
								}]
						},
					t_rif40_studies: {
							constraints: [{
									constraintName: 		't_rif40_std_study_geolevel_fk',
									foreignKey: 			'geography, study_geolevel_name',
									referencedTableColumns: 't_rif40_geolevels (geography, geolevel_name)'
								}, {
									constraintName: 		't_rif40_std_comp_geolevel_fk',
									foreignKey: 			'geography, comparison_geolevel_name',
									referencedTableColumns: 't_rif40_geolevels (geography, geolevel_name)'
								}, {
									constraintName:			't_rif40_studies_geography_fk',
									foreignKey: 			'geography',
									referencedTableColumns: 'rif40_geographies (geography)'
								}]
						},
					t_rif40_inv_covariates: {
							constraints: [{
									constraintName: 		't_rif40_inv_cov_geography_fk',
									foreignKey: 			'geography',
									referencedTableColumns: 'rif40_geographies (geography)'
								}, {
									constraintName: 		't_rif40_inv_cov_geolevel_fk',
									foreignKey: 			'geography, study_geolevel_name',
									referencedTableColumns: 't_rif40_geolevels (geography, geolevel_name)'
								}]
						}						
				};
				var tableList=Object.keys(fkList);	

				for (var i=0; i<tableList.length; i++) {	
					var fkConstraintsArray=fkList[tableList[i]].constraints;		
					for (var j=0; j<fkConstraintsArray.length; j++) {
						var constraint=fkConstraintsArray[j];
						var sqlStmt=new Sql("Enable constraints: " + constraint["constraintName"] + " on: " + tableList[i],
							getSqlFromFile("create_constraint.sql", 
								dbType,
								tableList[i] 							/* 1: Table; e.g. t_rif40_studies */,
								constraint["constraintName"] 			/* 2: Constraint; e.g. t_rif40_std_comp_geolevel_fk */,
								constraint["foreignKey"]				/* 3: Foreign key fields; e.g. geography, comparison_geolevel_name */,
								constraint["referencedTableColumns"]	/* 4: Referenced table and columns; e.g. t_rif40_geolevels (geography, geolevel_name) */),
							sqlArray, dbType);									
					}
				}			
			}			
			
			var sqlStmt=new Sql("Populate geography meta data table",
				getSqlFromFile("update_geography.sql", 
					undefined /* Common */,
					"rif40_geographies"												/* table; e.g. rif40_geographies */,
					xmlConfig.dataLoader.geographyName.toUpperCase() 				/* Geography; e.g. CB_2014_US_500K */,
					xmlConfig.dataLoader.defaultcomparea.toUpperCase()				/* Default comparision area, e.g. GEOID */,
					xmlConfig.dataLoader.defaultstudyarea.toUpperCase()				/* Default study area, e.g. STATENS */
					), 
				sqlArray, dbType);		
				
			var sqlStmt=new Sql("Update areaid_count column in geolevels table using geometry table", 
				getSqlFromFile("geolevels_areaid_update.sql", 
					dbType, 
					"t_rif40_geolevels" 											/* 1: Geolevels table */,
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() 	/* 2: Geometry table */,
					schema															/* 3: Schema; e.g. rif_data. or "" */,
					xmlConfig.dataLoader.geographyName.toUpperCase() 				/* 4: Geography */), 
				sqlArray, dbType);		

			var sqlStmt=new Sql("Check areaid_count column in geolevels table using geometry table", 
				getSqlFromFile("geolevels_areaid_check.sql", 
					dbType, 
					"t_rif40_geolevels" 											/* 1: Geolevels table */,
					"rif40."														/* 2: Schema; e.g. rif_data. or "" */), 
				sqlArray, dbType);		

//
// Drop dependent view to allow tiles table to be re-created
//	
			var sqlStmt=new Sql("Drop dependent object - view " + "tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
				getSqlFromFile("drop_view.sql", dbType, 
					(schema||"") + "tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() /* View name */), sqlArray, dbType); 

			if (dbType == "MSSQLServer") { 
				sqlArray.push(new Sql("Drop and recreate dependent objects required by tiles view: generate_series() [MS SQL Server only]"));			

				var sqlStmt=new Sql("Drop generate_series() function", 
					getSqlFromFile("drop_generate_series.sql", dbType), sqlArray, dbType); 
				var sqlStmt=new Sql("Create generate_series() function", 
					getSqlFromFile("generate_series.sql", dbType), sqlArray, dbType); 
			}						
		} // End of setupGeography()
		
		/*
		 * Function: 	loadGeolevelsLookupTables()
		 * Parameters:	Schema (rif_data.)
		 * Description:	Load geolevel lookup tables SQL statements
		 */			
		var loadGeolevelsLookupTables=function loadGeolevelsLookupTables(schema) {
			sqlArray.push(new Sql("Load geolevel lookup tables"));
		// Tables: lookup_<geometry>.csv
		
			for (var i=0; i<csvFiles.length; i++) { // Main file process loop	
				var lookupTable=(xmlConfig.dataLoader.geoLevel[i].lookupTable ||
						 "LOOKUP_" +  xmlConfig.dataLoader.geographyName).toLowerCase();
				var shapefileTable=xmlConfig.dataLoader.geoLevel[i].shapeFileTable.toLowerCase();
				var sqlStmt=new Sql("Load DB specific geolevel lookup table: (mssql_/pg_)" + lookupTable);
				if (dbType == "PostGres") {	
					sqlStmt.sql="\\copy " + lookupTable + "(" + shapefileTable + ", areaname, gid, geographic_centroid)" +
						" FROM 'pg_" + lookupTable +
						".csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8'";
				}
				else if (dbType == "MSSQLServer") {	
					sqlStmt.sql="BULK INSERT " + (schema||"") + lookupTable + "\n" + 
"FROM '$(pwd)/mssql_" + lookupTable + ".csv'" + '	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line\n' + 
"WITH\n" + 
"(\n" + 
"	FORMATFILE = '$(pwd)/mssql_" + lookupTable + ".fmt',		-- Use a format file\n" +
"	TABLOCK					-- Table lock\n" + 
")";
				}
				sqlStmt.dbType=dbType;
				sqlArray.push(sqlStmt);		
			} // End of for loop		
		} // End of loadGeolevelsLookupTables()
		
		/*
		 * Function: 	loadHierarchyTable()
		 * Parameters:	Schema (rif_data.)
		 * Description:	Load hierarchy table SQL statements
		 */	
		var loadHierarchyTable=function loadHierarchyTable(schema) {
			sqlArray.push(new Sql("Load hierarchy table"));
			var sqlStmt=new Sql("Load DB dependent hierarchy table from CSV file");
			if (dbType == "PostGres") {	
				sqlStmt.sql="\\copy " + "hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
					" FROM 'pg_hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
					".csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8'";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="BULK INSERT " + (schema||"") + "hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "\n" + 
"FROM '$(pwd)/mssql_hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() + ".csv'" + '	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line\n' + 
"WITH\n" + 
"(\n" + 
"	FORMATFILE = '$(pwd)/mssql_hierarchy_" + xmlConfig.dataLoader.geographyName.toLowerCase() + ".fmt',		-- Use a format file\n" +
"	TABLOCK					-- Table lock\n" + 
")";
			}
			sqlStmt.dbType=dbType;
			sqlArray.push(sqlStmt);		
			
		} // End of loadHierarchyTable()

		/*
		 * Function: 	loadAdjacencyTable()
		 * Parameters:	Schema (rif_data.)
		 * Description:	Load hierarchy table SQL statements
		 */	
		var loadAdjacencyTable=function loadAdjacencyTable(schema) {
			sqlArray.push(new Sql("Load adjacency table"));
			var sqlStmt=new Sql("Load DB dependent adjacency table from CSV file");
			if (dbType == "PostGres") {	
				sqlStmt.sql="\\copy " + "adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
					" FROM 'pg_adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
					".csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8'";
			}
			else if (dbType == "MSSQLServer") {	
				sqlStmt.sql="BULK INSERT " + (schema||"") + "adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "\n" + 
"FROM '$(pwd)/mssql_adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() + ".csv'" + '	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line\n' + 
"WITH\n" + 
"(\n" + 
"	FORMATFILE = '$(pwd)/mssql_adjacency_" + xmlConfig.dataLoader.geographyName.toLowerCase() + ".fmt',		-- Use a format file\n" +
"	TABLOCK					-- Table lock\n" + 
")";
			}
			sqlStmt.dbType=dbType;
			sqlArray.push(sqlStmt);		
			
		} // End of loadAdjacencyTable()
		
		/*
		 * Function: 	loadGeometryTable()
		 * Parameters:	Schema (rif_data.)
		 * Description:	Load geometry table SQL statements
		 */			
		var loadGeometryTable=function loadGeometryTable(schema) {
			sqlArray.push(new Sql("Load geometry table"));
			
			// Table: pg_geometry_USA_2014.csv
			// Needs to be SQL to psql command (i.e. COPY FROM stdin)
			
			if (dbType == "PostGres") {	
				var sqlStmt=new Sql("Add WKT column",
					getSqlFromFile("add_column.sql", 
						undefined /* Common */, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */,
						"WKT"																/* 2: WKT */,
						"Text"																/* 3: Data type */), 
					sqlArray, dbType);	
			}
			else if (dbType == "MSSQLServer") {		
				var sqlStmt=new Sql("Add WKT column",
					getSqlFromFile("add_column.sql", 
						undefined /* Common */, 
						(schema||"") + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */,
						"WKT"																/* 2: WKT */,
						"VARCHAR(MAX)"														/* 3: Data type */), 
					sqlArray, dbType);	
			}	
			var sqlStmt=new Sql("Comment geometry WKT column",
				getSqlFromFile("comment_column.sql", 
					dbType,
					"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */,
					"wkt"																/* 2: Column */, 
					"Well known text"													/* 3: Comment */), 
				sqlArray, dbType);	
					
			if (dbType == "PostGres") {		
				var sqlStmt=new Sql("Load DB dependent geometry table from CSV file", 
					"\\copy " + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
					"(geolevel_id, areaid, zoomlevel, wkt)" +
					" FROM '" + "pg_geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
					".csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8'", 
					sqlArray, dbType);
			}
			else if (dbType == "MSSQLServer") {	// Restrict columns using a view	
				var sqlStmt=new Sql("Create load geometry view", 
					"CREATE VIEW " + (schema||"") + "v_geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "\n" +
					"AS\n" + 
					"SELECT geolevel_id,areaid,zoomlevel,wkt\n" +
					"  FROM " + (schema||"") + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
					sqlArray, dbType);			
				var sqlStmt=new Sql("Load DB dependent geometry table from CSV file", 
					"BULK INSERT " + (schema||"") + "v_geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "\n" + 
"FROM '$(pwd)/" + "mssql_geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + ".csv'" + '	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line\n' + 
"WITH\n" + 
"(\n" + 
"	FORMATFILE = '$(pwd)/mssql_geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + ".fmt',	-- Use a format file\n" +
"	TABLOCK					-- Table lock\n" + 
")", 
					sqlArray, dbType);
				var sqlStmt=new Sql("Drop load geometry view", 
					"DROP VIEW " + (schema||"") + "v_geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
					sqlArray, dbType);
			}

			if (dbType == "PostGres") {	
				var sqlStmt=new Sql("Add WKT column",
					getSqlFromFile("update_geometry.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */,
						4326																/* 2: SRID */), 
					sqlArray, dbType);	
			}
			else if (dbType == "MSSQLServer") {		
				var sqlStmt=new Sql("Add WKT column",
					getSqlFromFile("update_geometry.sql", 
						dbType, 
						(schema||"") + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */,
						4326																/* 2: SRID */), 
					sqlArray, dbType);	
			}
			
			if (dbType == "PostGres") { // Partition Postgres
				var sqlStmt=new Sql("Add primary key, index and cluster (convert to index organized table)",
					getSqlFromFile("partition_geometry_table2.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* 1: Geometry table name */,
						xmlConfig.dataLoader.maxZoomlevel									/* 2: Max zoomlevel; e.g. 11 */,
						csvFiles.length														/* 3: Number of geolevels (e.g. 3) */), 
					sqlArray, dbType);	
			}		
			else if (dbType == "MSSQLServer") { 
			
				sqlArray.push(new Sql("No partitioning on SQL Server as it requires an Enterprise license; which"));
				sqlArray.push(new Sql("means you have to do it yourself using the generated scripts as a start.")); // Comment
					
				// Add primary key, index and cluster (convert to index organized table)
				var sqlStmt=new Sql("Add primary key",
					getSqlFromFile("add_primary_key.sql", 
						undefined /* Common */, 
						(schema||"") + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */,
						"geolevel_id, areaid, zoomlevel"									/* Primary key */), 
					sqlArray, dbType);	
				var sqlStmt=new Sql("Create spatial index on geom",
					getSqlFromFile("create_spatial_geometry_index.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "_gix"	/* Index name */, 
						(schema||"") + 
							"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */, 
						"geom"																	/* Geometry field name */,
						csvFiles[0].bbox[0]														/* 4: Xmin (4326); e.g. -179.13729006727 */,
						csvFiles[0].bbox[1]														/* 5: Ymin (4326); e.g. -14.3737802873213 */, 
						csvFiles[0].bbox[2]														/* 6: Xmax (4326); e.g.  179.773803959804 */,
						csvFiles[0].bbox[3]														/* 7: Ymax (4326); e.g. 71.352561 */), 
					sqlArray, dbType);	
					
				var sqlStmt=new Sql("Create spatial index on bbox",
					getSqlFromFile("create_spatial_geometry_index.sql", 
						dbType, 
						"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "_gix2" /* Index name */, 
						(schema||"") + 
							"geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */, 
						"bbox"																	/* Geometry field name */,
						csvFiles[0].bbox[0]														/* 4: Xmin (4326); e.g. -179.13729006727 */,
						csvFiles[0].bbox[1]														/* 5: Ymin (4326); e.g. -14.3737802873213 */, 
						csvFiles[0].bbox[2]														/* 6: Xmax (4326); e.g.  179.773803959804 */,
						csvFiles[0].bbox[3]														/* 7: Ymax (4326); e.g. 71.352561 */), 
					sqlArray, dbType);	
					
				var sqlStmt=new Sql("Analyze table",
					getSqlFromFile("analyze_table.sql", 
						dbType, 
						(schema||"") + "geometry_" + xmlConfig.dataLoader.geographyName.toLowerCase()		/* Table name */), 
					sqlArray, dbType);	
			}			
		} // End of loadGeometryTable()
		
		/*
		 * Function: 	loadTilesTables()
		 * Parameters:	Schema (rif_data.)
		 * Description:	Load tiles table SQL statements
		 */			
		var loadTilesTables=function loadTilesTables(schema) {
			sqlArray.push(new Sql("Load tiles table"));
		// Tables: t_tiles_<geometry>.csv
		
			for (var i=0; i<csvFiles.length; i++) { // Main file process loop	
				var sqlStmt=new Sql("Load DB dependent tiles table from geolevel CSV files");
				if (dbType == "PostGres") {	
					sqlStmt.sql="\\copy " + "t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() + 
						"(geolevel_id,zoomlevel,x,y,tile_id,areaid_count,optimised_topojson)" + 
						" FROM 'pg_t_tiles_" + xmlConfig.dataLoader.geoLevel[i].geolevelName.toLowerCase() + 
						".csv' DELIMITER ',' CSV HEADER ENCODING 'UTF-8'";				
					sqlStmt.dbType=dbType;
					sqlArray.push(sqlStmt);	
				}
				else if (dbType == "MSSQLServer") {	// Restrict columns using a view	
					var sqlStmt2=new Sql("Create load tiles view", 
						"CREATE VIEW " + (schema||"") + "v_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "\n" +
						"AS\n" + 
						"SELECT geolevel_id, zoomlevel, x, y, tile_id, areaid_count, optimised_topojson\n" +
						"  FROM " + (schema||"") + "t_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(), 
						sqlArray, dbType);					
					sqlStmt.sql="BULK INSERT " + (schema||"") + "v_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase() + "\n" + 
	"FROM '$(pwd)/mssql_t_tiles_" + xmlConfig.dataLoader.geoLevel[i].geolevelName.toLowerCase() + ".csv'" + '	-- Note use of pwd; set via -v pwd="%cd%" in the sqlcmd command line\n' + 
	"WITH\n" + 
	"(\n" + 
	"	FORMATFILE = '$(pwd)/mssql_t_tiles_" + xmlConfig.dataLoader.geoLevel[i].geolevelName.toLowerCase() + ".fmt',		-- Use a format file\n" +
	"	TABLOCK					-- Table lock\n" + 
	")";
					sqlStmt.dbType=dbType;
					sqlArray.push(sqlStmt);	
					var sqlStmt2=new Sql("Create load tiles view", 
						"DROP VIEW " + (schema||"") + "v_tiles_" + xmlConfig.dataLoader.geographyName.toLowerCase(),
						sqlArray, dbType);					
				}	
			} // End of for loop
			
		} // End of loadTilesTables()
		
		var sqlArray=[]; // Re-initialise
		
		beginTransaction(sqlArray, dbType); 
		
		var sqlStmt=new Sql("RIF initialisation", 
			getSqlFromFile("rif_startup.sql", dbType), 
			sqlArray, dbType); 	
			
		var sqlStmt=new Sql("Check if geography is in use in studies. Raise error if it is.", 
			getSqlFromFile("in_use_check.sql", dbType, 
				xmlConfig.dataLoader.geographyName.toUpperCase() /* 1: Geography */), 
			sqlArray, dbType); 
				
		createGeolevelsLookupTables(sqlArray, dbType, 'rif_data.' /* Schema */);
		loadGeolevelsLookupTables('rif_data.' /* Schema */);
		createHierarchyTable(sqlArray, dbType, 'rif_data.' /* Schema */);	
		loadHierarchyTable('rif_data.' /* Schema */);
		createGeometryTable(sqlArray, dbType, 'rif_data.' /* Schema */);
		loadGeometryTable('rif_data.' /* Schema */);
		createAdjacencyTable(sqlArray, dbType, 'rif_data.' /* Schema */);		
		loadAdjacencyTable('rif_data.' /* Schema */);
		var geoLevelsTable="t_rif40_geolevels";
		setupGeography('rif_data.' /* Schema */);
		createTilesTables(sqlArray, dbType, geoLevelsTable, 'rif_data.' /* Schema */);
		loadTilesTables('rif_data.' /* Schema */);
		grantTables('rif_data.' /* Schema */);
		
		commitTransaction(sqlArray, dbType);
		
		analyzeTables('rif_data.' /* Schema */);
		
//
// Write SQL statements to file
//		
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
	} // End of addSQLLoadStatements()
	
	var pgScript="pg_" + xmlConfig.dataLoader.geographyName + ".sql"
	var mssqlScript="mssql_" + xmlConfig.dataLoader.geographyName + ".sql"
	
	var pgStream=createSQLScriptHeader(dir + "/" + pgScript, "PostGres", "header.sql");
	var mssqlStream=createSQLScriptHeader(dir + "/" + mssqlScript, "MSSQLServer", "header.sql");
	
	addSQLStatements(pgStream, csvFiles, xmlConfig.dataLoader.srid, "PostGres");
	addSQLStatements(mssqlStream, csvFiles, xmlConfig.dataLoader.srid, "MSSQLServer");
	
	var endStr="\n\n--\n-- EOF\n";
	pgStream.write(endStr);
	mssqlStream.write(endStr);
	
	pgStream.end();
	mssqlStream.end();	
	
	var msg="Created database load scripts: " + pgScript + " and " + mssqlScript;
	response.message+="\n" + msg;

//
// DB load script
//
	var pgLoadScript="rif_pg_" + xmlConfig.dataLoader.geographyName + ".sql"
	var mssqlLoadScript="rif_mssql_" + xmlConfig.dataLoader.geographyName + ".sql"
	
	var pgLoadStream=createSQLScriptHeader(dir + "/" + pgLoadScript, "PostGres", "rif40_header.sql");
	var mssqlLoadStream=createSQLScriptHeader(dir + "/" + mssqlLoadScript, "MSSQLServer", "rif40_header.sql");
	
	addSQLLoadStatements(pgLoadStream, csvFiles, xmlConfig.dataLoader.srid, "PostGres");
	addSQLLoadStatements(mssqlLoadStream, csvFiles, xmlConfig.dataLoader.srid, "MSSQLServer");
	
	var endStr="\n\n--\n-- EOF\n";
	pgLoadStream.write(endStr);
	mssqlLoadStream.write(endStr);
	
	pgLoadStream.end();
	mssqlLoadStream.end();	

//
// Create all format files for SQL Server
//
	createSqlServerFmtFiles(dir, csvFiles, 
		function createSqlServerFmtFilesEnd(err) {
			//	createLoadSqlServerFmtFiles(dir, csvFiles);
		
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
		} // End of createSqlServerFmtFilesEnd()
	)
	
} // End of CreateDbLoadScripts()

/*
 * Function: 	createSqlServerFmtFile()
 * Parameters:	Directory, table name, rows cobject (first row is a header row), callback
 * Description:	Create SQL Server format file
 */	
function createSqlServerFmtFile(dir, tableName, rows, createSqlServerFmtFileCallback) {
	scopeChecker(__file, __line, {
		dir: dir, 
		tableName: tableName, 
		rows: rows, 
		rowZero: rows[0], 
		callback: createSqlServerFmtFileCallback
	});

	var fmtScriptName="mssql_" + tableName + ".fmt";
	var fmtStream = fs.createWriteStream(dir + "/" + fmtScriptName, { flags : 'w' });	
	fmtStream.on('finish', function fmtStreamClose() {
		createSqlServerFmtFileCallback();
	});		
	fmtStream.on('error', function fmtStreamError(e) {
		createSqlServerFmtFileCallback(e);									
	});
	
	var fmtBuf='<?xml version="1.0"?>\n' +
	'<!-- MS SQL Server bulk load format files\n' +
'	 The insistence on quotes excludes the header row -->\n' +
'<BCPFORMAT xmlns="http://schemas.microsoft.com/sqlserver/2004/bulkload/format"\n' +
'  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n' +
' <RECORD>\n' + 
'   <FIELD ID="0" xsi:type="CharTerm" TERMINATOR=' + "'" + '"' + "' />\n";
	var columnList=Object.keys(rows[0]);
	
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
	fmtStream.end(); // Runs createSqlServerFmtFileCallback()
} // End of createSqlServerFmtFile()
		
module.exports.CreateDbLoadScripts = CreateDbLoadScripts;
module.exports.getSqlFromFile = getSqlFromFile;
module.exports.createSqlServerFmtFile = createSqlServerFmtFile;

// Eof