#!/usr/bin/env node

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
// Rapid Enquiry Facility (RIF) - GeoJSON to topoJSON converter
//								  Uses node.js TopoJSON module
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
// Usage: node topojson_convert.js <PGHOST; default: localhost> <geography; default: sahsu>
//
// Connects using Postgres native driver (not JDBC) as rif40.
//
// Uses:
//
// https://github.com/mbostock/topojson
// https://github.com/brianc/node-postgres
//
// See: Node Makefile for build instructions
//
var pg = require('pg');
var topojson = require('topojson');

// Process Args
var pghost = process.argv[2];
if (!pghost) {
	pghost = 'localhost';
}
else {
	console.log('Using arg[2] host: ' + pghost);
}

var geography = process.argv[3];
if (!geography) {
	geography = 'sahsu';
}
else {
	console.log('Using arg[3] geography: ' + geography);
}

var pgdatabase = process.argv[4];
if (!pgdatabase) {
	pgdatabase = 'sahsuland_dev';
}
else {
	console.log('Using arg[4] pgdatabase: ' + pgdatabase);
}
var conString = 'postgres://rif40@'; // Use PGHOST, native authentication (i.e. same as psql)
// If host = localhost, use IPv6 numeric notation. This prevent ENOENT errors from getaddrinfo() in Windows
// when Wireless is disconnected. This is a Windows DNS issue. psql avoids this somehow.
// You do need entries for ::1 in pgpass
//if (pghost == 'localhost') {
//	conString=conString + '[::1]';
//}
//else {
	conString=conString + pghost;
//}
conString=conString + '/' + pgdatabase + '?application_name=topojson_convert';


// Create Postgres client
var client = null;
try {
	client = new pg.Client(conString);
	console.log('Connected to Postgres using: ' + conString);
	
}
catch(err) {
		return console.error('Could create postgres client using: ' + conString, err);
}

// Notice message event processor
client.on('notice', function(msg) {
      console.log('notice: %s', msg);
});
	
// Connect to Postgres database
client.connect(function(err) {
	if (err) {
		return console.error('Could not connect to postgres using: ' + conString, err);
	}
	else {
		var start = new Date().getTime();
		var sql_stmt = 'SELECT tile_id, optimised_geojson::Text AS optimised_geojson FROM t_rif40_' + 
				geography + '_maptiles';
				
		// Connected OK, run SQL query
		var query = client.query(sql_stmt, function(err, result) {
			if (err) {
				// Error handler
				client.end();
				return console.error('Error running query: ' + sql_stmt + ';', err);
			}
			else {	
				// Query OK
			
				var row_count = null;
				var last_tile_id = null;			
				var tile_id = [];
				var optimised_topojson = [];
				var optimised_geojson = [];
				var object = {};
				var objects = {};	
				var tile_id = null;
				var topology = null;
				var options = { // TopoJSON options
					"verbose": true,
					"properties": "gid",					
					"projection": "4326",
					"post-quantization": 1e4,
					"property-transform": myPropertyTransform,
					"id": myId};
//
// Promote tile gid to id
//					
				function myId(d) {
//					console.log('call myId()');					
					return d.properties.gid;
				}
//
// Retain properties gid
//				
				function myPropertyTransform(feature) {
//					console.log('call myPropertyTransform()');
					return { "name": feature.properties.name, "area_id": feature.properties.area_id, "gid": feature.properties.gid };
				}		
				query.on('row', function(row) {
					//fired once for each row returned
					result.addRow(row);
				});
				query.on('end', function(result) {
					// End of query processing - process results array
					row_count = result.rowCount;
					// Setup arrays for results
					tile_id = new Array();
					optimised_topojson = new Array();
					optimised_geojson = new Array();
					for (i = 0; i < row_count; i++) { 
						object = JSON.parse(result.rows[i].optimised_geojson);
						tile_id.push(result.rows[i].tile_id);
						optimised_geojson.push(result.rows[i].optimised_geojson);
						objects = {};
						if (object.type === "Topology") {
							for (var key in object.objects) {
								objects[key] = topojson.feature(object, object.objects[key]);
							}
						}
						else {
							objects[tile_id[i] + '.json'] = object;
						}   
						// Convert GeoJSON to TopoJSON.

						console.log('Converting GeoJSON to TopoJSON for tile [ ' + i + ']: ' + tile_id[i]);					
						topology = topojson.topology(objects, options); // convert to TopoJSON
						optimised_topojson.push(JSON.stringify(topology)); 
			
						// Clear the objects to force garbage collection.
						objects = null;
						object = null;
						topology = null;
					}
					last_tile_id = tile_id[row_count - 1];
					// Call topojson update loop
					update_topojson_loop(tile_id, optimised_topojson, optimised_geojson, row_count, last_tile_id, start);
					// Clear the objects to force garbage collection.
					tile_id = null;
					optimised_topojson = null;
					optimised_geojson = null;
				}); // End of query close processing

		} 
	}); // End of query;

	} // End of else connected OK 
}); // End of connect


/* 
 * Function: 	do_update()
 * Parameters: 	Tile Id, optimised topoJSON (as text), row count, last tile ID, start time
 * Returns:		N/A
 * Description: Process Update row.
 *				When processed last tile ID, commit transaction, VACUUM ANALYZE maptile table, logoff
 */
function do_update(ptile_id, poptimised_topojson, lrow_count, llast_tile_id, lstart) {
	var update_stmt = 'WITH a AS ( UPDATE t_rif40_' + 
			geography + '_maptiles SET optimised_topojson = ' +
					'REPLACE($1::Text, ' +
							'tile_id||\'.json\', ' +
							'zoomlevel::Text||\'_\'||x_tile_number::Text||\'_\'||y_tile_number::Text)::JSON WHERE tile_id = $2 ' + 
			'RETURNING tile_id, LENGTH(optimised_geojson::Text) AS length_geojson, ' + 
			'LENGTH(optimised_topojson::Text) AS length_topojson, ' + 
			'ROUND((1-(LENGTH(optimised_topojson::Text)::NUMERIC/LENGTH(optimised_geojson::Text)::NUMERIC))*100, 2) AS pct_reduction) ' + 
			'SELECT a.tile_id, a.length_geojson, a.length_topojson, a.pct_reduction FROM a'; 
				
	var update = client.query(update_stmt, [poptimised_topojson,ptile_id], function(err, result) {
		if(err) {
			client.end();
			return console.error('Error running update >>>\n' + 
				update_stmt + ';', err);
		}
		else {
			// Update OK
			update.on('row', function(row) {
				//fired once for each row returned
				result.addRow(row);
			});
			// End of update processing
			update.on('end', function(result) {	
				// Check 1 row updated
				if (result.rowCount != 1) {
					client.end();
					return console.error('Error running update, expected: 1 got: ' + result.rowCount + ' >>>\n' + 
						update_stmt + ';', err);
				}
				else {
					console.log('Processed tile_id: ' + result.rows[0].tile_id +
						'; lengths geoJSON: ' + result.rows[0].length_geojson +
						', topoJSON: ' + result.rows[0].length_topojson +
						', % reduction: ' + result.rows[0].pct_reduction);
					// Commit and disconnect after last row				
					if (llast_tile_id === result.rows[0].tile_id) { 
						// Commit transaction 
						var END = client.query('COMMIT', function(err, result) {
							if (err) {
								client.end();
								return console.error('Error in COMMIT transaction;', err);
							}
							else {
								console.log('Tranaction end: ' + lrow_count + ' rows processed; ' + result.command);
							}
						});	
						client.on('drain', client.end.bind(client)); 
						// Vacuum analyze maptiles table and disconnect client when all queries are finished
						var analyze = client.query('VACUUM ANALYZE VERBOSE t_rif40_' + 
								geography + '_maptiles', function(err, result) {
							if (err) {
								client.end();
								return console.error('Error in VACUUM ANALYZE', err);
							}
							else {
								var end = new Date().getTime();
								var time = (end - lstart)/1000;		
								var rows_per_sec = lrow_count/time;
								console.log('VACUUM ANALYZE complete; overall ' + lrow_count + ' tiles processed in: ' + 
									time + ' S; ' + Math.round(rows_per_sec*100)/100 + ' tiles/S');								
							}
						});							
					}
				}					
			});
		}
	});
}

/* 
 * Function: 	update_topojson_loop()
 * Parameters: 	Tile Id array, optimised topoJSON array (as text), optimised geoJSON array (as text),row count, last tile ID, start time
 * Returns:		N/A
 * Description: For each row call do_update()
 */
function update_topojson_loop(ltile_id, loptimised_topojson, loptimised_geojson, lrow_count, llast_tile_id, lstart) {

	var begin = client.query('BEGIN', function(err, result) {
		if (err) {
			client.end();
			return console.error('Error in BEGIN transaction;', err);
		}
		else {
			// Transaction start OK 
			console.log('Tranaction start: ' + lrow_count + ' rows to process');
			for (var i = 0; i < lrow_count; i++) { 
				do_update(ltile_id[i], loptimised_topojson[i], lrow_count, llast_tile_id, lstart);
			}
		}
	});
}

// Eof