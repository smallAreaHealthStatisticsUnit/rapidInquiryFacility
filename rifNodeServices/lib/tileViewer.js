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
// Rapid Enquiry Facility (RIF) - Node Geospatial webservices
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
//
// Usage: tests/requests.js
//
// Uses:
//
// CONVERTS GEOJSON(MAX 100MB) TO TOPOJSON
// Only POST requests are processed
// Expects a vaild geojson as input
// Topojson have quantization on
// The level of quantization is based on map tile zoom level
// More info on quantization here: https://github.com/mbostock/topojson/wiki/Command-Line-Reference
//
// Prototype author: Federico Fabbri
// Imperial College London
//
var pgClient;	
var mssqlClient;
const pg=require('pg');		
const mssql=require('mssql');

/*
 * Function:	getMapTile()
 * Parameters:	response, HTTP request object, http response object, serverLog object, httpErrorResponse object
 * Returns:		Status array as JSON
 * Description: Get from from DB
 *
 * Example URL:
 *
 * 127.0.0.1:3000/getMapTile/?zoomlevel=0&x=0&y=0&databaseType=PostGres&table_catalog=sahsuland_dev&table_schema=peter&table_name=geography_sahsuland&geography=SAHSULAND&geolevel_id=0
 */	
getMapTile = function getMapTile(response, req, res, serverLog, httpErrorResponse) {
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		response: response,
		req: req,
		res: res
	});

	response.message="In: getMapTile() ";	
	var msg="";	
	var lstart=new Date().getTime();
	response.fields=req.query;

/*
 * Function:	getMapTileErrorHandler()
 * Parameters:	error object
 * Returns:		Nothing
 * Description: Handle error in own context to prevent re-throws
 */		
	function getMapTileErrorHandler(err) {
		msg+=err.message;
		response.message+=err.message;
		try {
			httpErrorResponse.httpErrorResponse(__file, __line, "getMapTile", 
				serverLog, 500, req, res, msg, undefined /* Error: do not re-throw */, response);
		}
		catch (e) {
			console.error("Unexpected re-throw: " + e.message);
		}		
	} // End of getMapTileErrorHandler()

/*
 * Function:	getMapTileResponse()
 * Parameters:	None (callback)
 * Returns:		Nothing
 * Description: Send response to client
 */	
	function getMapTileResponse(result) {
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			response: response,
			req: req,
			res: res
		});	
		
		response.result='{"type": "FeatureCollection","features":[]}';	// null geojson
		response.message+=msg;
		msg+="Return result map tile x: " + response.fields.x + ", y: " + response.fields.y + " z: " + response.fields.zoomlevel + "\n" +
			JSON.stringify(response.fields, null, 2);
		response.message+=msg;
		tileViewerReponseProcessing("getMapTile", response, response.result, req, res, serverLog, httpErrorResponse);
	} // getMapTileResponse()

	/*
 * Function:	getGeographiesDbCallback()
 * Parameters:	databaseType, databaseName, dbRequest
 * Returns:		Nothing
 * Description: Post connection database processing callback
 */		
	function getGeographiesDbCallback(databaseType, databaseName, dbRequest) {
		getAllGeographies(
			databaseType,					// Database type
			databaseName,					// Databse name
			dbRequest,						// dbRequest	
			getGeographiesResponse,			// Callback
			getGeographiesErrorHandler		// Error callback
		)	
	} // End of getGeographiesDbCallback()
	
/*
 * Function:	getMapTileDbCallback()
 * Parameters:	database, databaseName, dbRequest
 * Returns:		Nothing
 * Description: Post connection database processing callback
 */		
	function getMapTileDbCallback(databaseType, databaseName, dbRequest) {	
		getMapTileResponse(response.result); // Replace with just geojson
	}

	var requiredArgs=["zoomlevel", "x", "y", "databaseType", "table_catalog", "table_schema", "table_name", 
		"geography", "geolevel_id"];
	
	for (var i=0; i<requiredArgs.length; i++) { // Validate fields
		if (response.fields[requiredArgs[i]] == undefined) {
			getMapTileErrorHandler(new Error("getMapTile() Missing argument: " + requiredArgs[i]));
		}
		else if (requiredArgs[i] == 'x' &&
			typeof(Number(response.fields[requiredArgs[i]])) != "number") { // Numeric field
			getMapTileErrorHandler(new Error("getMapTile() Argument: " + requiredArgs[i] + 
				" is not a number: " + response.fields[requiredArgs[i]] + 
				"; is: " + typeof(response.fields[requiredArgs[i]])));
		}
		else if (requiredArgs[i] == 'y' &&
			typeof(Number(response.fields[requiredArgs[i]])) != "number") { // Numeric field
			getMapTileErrorHandler(new Error("getMapTile() Argument: " + requiredArgs[i] + 
				" is not a number: " + response.fields[requiredArgs[i]] + 
				"; is: " + typeof(response.fields[requiredArgs[i]])));
		}
		else if (requiredArgs[i] == 'zoomlevel' &&
			typeof(Number(response.fields[requiredArgs[i]])) != "number") { // Numeric field
			getMapTileErrorHandler(new Error("getMapTile() Argument: " + requiredArgs[i] + 
				" is not a number: " + response.fields[requiredArgs[i]] + 
				"; is: " + typeof(response.fields[requiredArgs[i]])));
		}
		else if (requiredArgs[i] == 'geolevel_id' &&
			typeof(Number(response.fields[requiredArgs[i]])) != "number") { // Numeric field
			getMapTileErrorHandler(new Error("getMapTile() Argument: " + requiredArgs[i] + 
				" is not a number: " + response.fields[requiredArgs[i]] + 
				"; is: " + typeof(response.fields[requiredArgs[i]])));
		}
		else if (requiredArgs[i] == "databaseType" && 
				response.fields[requiredArgs[i]] != 'PostGres' && response.fields[requiredArgs[i]] != 'MSSQLServer') {
			getMapTileErrorHandler(new Error("getMapTile() Invalid database type: " + response.fields[requiredArgs[i]]));
		}
	}
 
	try { 
		dbConnect(response, getMapTileDbCallback, getMapTileErrorHandler);
	}	
	catch (err) {	
		getMapTileErrorHandler(err);	// Use scope of calling function
	}
} // End of getMapTile

/*
 * Function:	tileViewerReponseProcessing()
 * Parameters:	service, response, HTTP request object, http response object, result object, serverLog object, httpErrorResponse object
 * Returns:		Nothing
 * Description: Send response to client
 */	
function tileViewerReponseProcessing(service, response, result, req, res, serverLog, httpErrorResponse) {
	scopeChecker(__file, __line, {
		service: service,
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		response: response,
		result: result,
		req: req,
		res: res
	});
	
	if (!res.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed

		var output = JSON.stringify(result);// Convert output response to JSON 
// Need to test res was not finished by an expection to avoid "write after end" errors			
		try {
			res.write(output);                  // Write output  
			res.end();	
			if (response.message) {
				console.error(service + "() Complete OK; Diagnostics >>>\n" + response.message + "\n<<< End of diagnostics");
			}
		}
		catch(e) {
			serverLog.serverError(__file, __line, "getStatus", "Error in sending response to client", req, e, response);
		}
	}
	else {
		serverLog.serverError2(__file, __line, "getStatus", 
			"Unable to return OK response to user - httpErrorResponse() already processed", 
			req, undefined /* err */, response);
	}		
} // End of tileViewerReponseProcessing()

/*
 * Function:	getGeographies()
 * Parameters:	response, HTTP request object, http response object, serverLog object, httpErrorResponse object
 * Returns:		Status array as JSON
 * Description: Get geographies in a database
 */	
getGeographies = function getGeographies(response, req, res, serverLog, httpErrorResponse) {
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		response: response,
		req: req,
		res: res
	});

	response.message="In: getGeographies() ";	
	var msg="";	
	var lstart=new Date().getTime();
	response.fields=req.query;

/*
 * Function:	getGeographiesErrorHandler()
 * Parameters:	error object
 * Returns:		Nothing
 * Description: Handle error in own context to prevent re-throws
 */		
	function getGeographiesErrorHandler(err) {
		msg+=err.message;
		response.message+=err.message;
		try {
			httpErrorResponse.httpErrorResponse(__file, __line, "getGeographies", 
				serverLog, 500, req, res, msg, undefined /* Error: do not re-throw */, response);
		}
		catch (e) {
			console.error("Unexpected re-throw: " + e.message);
		}		
	} // End of getGeographiesErrorHandler()

/*
 * Function:	getGeographiesResponse()
 * Parameters:	Result array
 * Returns:		Nothing
 * Description: Send response to client
 */	
	function getGeographiesResponse(result) {
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			response: response,
			req: req,
			res: res
		});	
		
		response.geographies=result;	
		msg+="Return result geography: " + (result && result.length) + " rows\n";
		response.message+=msg;
		tileViewerReponseProcessing("getGeographies", response, response, req, res, serverLog, httpErrorResponse);
	} // End of getGeographiesResponse()

/*
 * Function:	getGeographiesDbCallback()
 * Parameters:	databaseType, databaseName, dbRequest
 * Returns:		Nothing
 * Description: Post connection database processing callback
 */		
	function getGeographiesDbCallback(databaseType, databaseName, dbRequest) {
		getAllGeographies(
			databaseType,					// Database type
			databaseName,					// Databse name
			dbRequest,						// dbRequest	
			getGeographiesResponse,			// Callback
			getGeographiesErrorHandler		// Error callback
		)	
	} // End of getGeographiesDbCallback()
	
	try { 
		dbConnect(response, getGeographiesDbCallback, getGeographiesErrorHandler);
	}	
	catch (err) {	
		getGeographiesErrorHandler(err);	// Use scope of calling function
	}
} // End of getGeographies()

/*
 * Function:	dbConnect()
 * Parameters:	response, db callback, dbErrorHandler callback (to use scope of callimng function)
 * Returns:		Nothing
 * Description: Post connection database processing callback
 */	
function dbConnect(response, dbCallback, dbErrorHandler) {
	if (response.fields && response.fields["databaseType"]) { // Can get geographies
		if (response.fields["databaseType"] == "PostGres") {

			var p_database=process.env["PGDATABASE"];			
			if (p_database == undefined) {
				throw new Error("Unable to determine postgres database from: PGDATABASE");
			}
			if (pgClient) {
				dbCallback(
					response.fields["databaseType"],// Database type
					p_database,						// Databse name
					pgClient						// dbRequest	
				)
			}
			else {
				var p_user=process.env["PGUSER"] || process.env["USERNAME"] || process.env["USER"];
				if (p_user == undefined) {
					throw new Error("Unable to determine postgres user logon from: PGUSER/USERNAME/USER");
				}
				var p_hostname=process.env["PGHOST"] || "localhost";
				var p_port=process.env["PGPOST"] || 5432;
			
				var pgConnectionString = 'postgres://' + p_user + '@' + p_hostname + ':' + p_port + '/' + p_database + '?application_name=tileViewer';
				// Use PGHOST, native authentication (i.e. same as psql)
				pgClient = new pg.Client(pgConnectionString);
				// Connect to Postgres database
				pgClient.connect(function(err) {	
					if (err) {
						var nerr=new Error("Unable to connect to Postgres using: " + pgConnectionString + "; error: " + err.message);
						nerr.stack=err.stack;
						dbErrorHandler(nerr);	// Use scope of calling function	
					}
					else {
						dbCallback(
							response.fields["databaseType"],// Database type
							p_database,						// Databse name
							pgClient						// dbRequest	
						)
					}
				});						
			}

		}
		else if (response.fields["databaseType"] == "MSSQLServer") {

			var p_database=process.env["SQLCMDDBNAME"] || "" // Use sql Server defined default;				
			if (mssqlClient) {
				var dbRequest=new mssql.Request();
				dbCallback(
					response.fields["databaseType"],// Database type
					p_database,						// Databse name
					dbRequest						// dbRequest	
				)				
			}
			else {
				var p_hostname=process.env["SQLCMDSERVER"] || "localhost";
				var config = {
					driver: 'msnodesqlv8',
					server: p_hostname,
					database: p_database,
					options: {
						trustedConnection: true,
						useUTC: true,
						appName: 'tileViewer.js'
					}
				};
				
				// Connect to SQL server database
				mssqlClient=mssql.connect(config, function(err) {
					if (err) {
						var nerr=new Error("Unable to connect to SQL server using: " + JSON.stringify(config, null, 4), + "; error: " + err.message);
						nerr.stack=err.stack;
						dbErrorHandler(nerr);	// Use scope of calling function							
					}
					else {
						var dbRequest=new mssql.Request();
						dbCallback(
							response.fields["databaseType"],// Database type
							p_database,						// Databse name
							dbRequest						// dbRequest	
						)						
					}
				});						
			}
	
		}
		else {
			throw new Error("Invalid database type: " + response.fields["databaseType"]);
		}
	}
	else {
		throw new Error("\nCannot determine database from fields: " + JSON.stringify(response.fields, null, 2));
	}		
} // End of dbConnect()
	
/*
 * Function:	getAllGeographies()
 * Parameters:	database type, database name, database request object, getAllGeographiesCallback, getGeographiesErrorHandler
 * Returns:		Nothing
 * Description: Handle error in own context to prevent re-throws
 */	
function getAllGeographies(databaseType, databaseName, dbRequest, getAllGeographiesCallback, getGeographiesErrorHandler) {
	scopeChecker(__file, __line, {
		callback: getAllGeographiesCallback,
		callback: getGeographiesErrorHandler,
		databaseType: databaseType,
		databaseName:databaseName,
		dbRequest: dbRequest
	});
	
	var sql="SELECT table_catalog, table_schema, table_name\n" + 
			"  FROM information_schema.columns\n" +
			" WHERE (table_name LIKE 'geography%' OR table_name = 'rif40_geographies')\n" + 
			"   AND column_name = 'tiletable'"; 
	var query=dbRequest.query(sql, function getAllGeographiesTables(err, sqlResult) {
		if (err) {
			var nerr=new Error(databaseType + " database: " + databaseName + "; error: " + err.message + "\nin SQL> " + sql +";");
			nerr.stack=err.stack;
			getGeographiesErrorHandler(nerr);	
		}
		else {
			var result;
			if (databaseType == "PostGres") {
				result=sqlResult.rows;
			}
			else if (databaseType == "MSSQLServer") {
				result=sqlResult;
			}	
			
			var sql="";
			if (result.length == 0) {
				getGeographiesErrorHandler(new Error(databaseType + " database: " + databaseName + "; has no tile maker or RIF tables"));
			}
			else {
//				console.error("RESULT: " + JSON.stringify(result, null, 2));
				for (i=0; i<result.length; i++) {
					if (i == 0) {
						sql+="SELECT '" + databaseType + "' AS database_type, '" + 
								result[i].table_catalog + "' AS table_catalog, '" + 
								result[i].table_schema + "' AS table_schema, '" + result[i].table_name + 
							"' AS table_name,\n" + 
							        "geography, description, maxzoomlevel, minzoomlevel, tiletable\n" + 
							"  FROM " + result[i].table_schema + "." + result[i].table_name + '\n' +
							" WHERE tiletable IS NOT NULL";
					}
					else {
						sql+="\nUNION\n" + 
							"SELECT '" + databaseType + "' AS database_type, '" + 
								result[i].table_catalog + "' AS table_catalog, '" + 
								result[i].table_schema + "' AS table_schema, '" + result[i].table_name + 
							"' AS table_name,\n" + 
							        "geography, description, maxzoomlevel, minzoomlevel, tiletable\n" + 
							"  FROM " + result[i].table_schema + "." + result[i].table_name + ' a\n' +
							" WHERE tiletable IS NOT NULL";	
					}	
				}
			}
			if (sql) {
				sql+="\n ORDER BY 1, 2, 3, 4, 5";
//				console.error("SQL> " + sql);
			}
			else {
				getGeographiesErrorHandler(new Error(databaseType + " database: " + databaseName + 
					"; getAllGeographiesTables() no SQL generated"));
			}
	
			var query=dbRequest.query(sql, function getAllGeographiesResult(err, sqlResult) {
				if (err) {
					var nerr=new Error(databaseType + " database: " + databaseName + "; error: " + err.message + "\nin SQL> " + sql +";");
					nerr.stack=err.stack;
					getGeographiesErrorHandler(nerr);	
				}
				else {
					var result;
					if (databaseType == "PostGres") {
						result=sqlResult.rows;
					}
					else if (databaseType == "MSSQLServer") {
						result=sqlResult;
					}
					getAllGeographiesCallback(result);
				}
			} // End of getAllGeographiesResult()
			);
		}
	} // End of getAllGeographiesTables()
	); 
} // End of selectFromRif40Geographies()

module.exports.getMapTile = getMapTile;
module.exports.getGeographies = getGeographies;

// Eof