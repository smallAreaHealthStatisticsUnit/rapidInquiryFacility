#!/usr/bin/env node

var pg = require('pg');
var topojson = require('topojson');

//var conString = "postgres://rif40:se11afield2012@wpea-rif1/sahsuland_dev";
var conString = "postgres://rif40@localhost/sahsuland_dev"; // Use 

var geography = process.argv[2];
if (!geography) {
	geography = 'sahsu';
}
var row_count = 0;
var tile_id = [];
var optimised_topojson = [];
var client = new pg.Client(conString);
client.on('drain', client.end.bind(client)); //disconnect client when all queries are finished

client.connect(function(err) {
	if (err) {
		return console.error('Could not connect to postgres using: ' + conString, err);
	}
}); // End of connect

var sql_stmt = 'SELECT tile_id, optimised_geojson::Text AS optimised_geojson FROM t_rif40_' + geography + '_maptiles LIMIT 5';  

var query = client.query(sql_stmt, function(err, result) {
		if (err) {
			client.end();
			return console.error('Error running query: ' + sql_stmt + ';', err);
		}
		else {	
			query.on('row', function(row) {
				//fired once for each row returned
				result.addRow(row);
			});
			query.on('end', function(result) {
			//fired once and only once, after the last row has been returned and after all 'row' events are emitted
			//in this example, the 'rows' array now contains an ordered set of all the rows which we received from postgres
				row_count = result.rowCount;
				tile_id = new Array();
				optimised_topojson = new Array();				
				for (i = 0; i < row_count; i++) { 
					object = JSON.parse(result.rows[i].optimised_geojson);
					tile_id.push(result.rows[i].tile_id);
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
			
					// Clear the objects to allow garbage collection.
					objects = null;
					object = null;
					topology = null;
				}
//				console.log('SQL> ' + sql_stmt + ' complete, ' + row_count + ' rows were processed.');

			}); // End of query close processing
	}
}); // End of query;

var update_stmt = 'UPDATE t_rif40_' + geography + '_maptiles SET optimised_topojson = $1 WHERE tile_id = $2';  

var options = {
		"verbose": true,
		"post-quantization": 1e4};
var optimised_geojson = null;
var object = {};
var objects = {};	
var tile_id = null;
var topology = null;
		
var begin = client.query('BEGIN', function(err, result) {
		if (err) {
			client.end();
			return console.error('Error in BEGIN transaction;', err);
		}
		else {
			console.log('Tranaction start: ' + row_count + ' rows to process');


			for (i = 0; i < row_count; i++) { 

				console.log('BB ' + i + '] ' + tile_id[i]'; length: ' + optimised_geojson[i].length + 
							' => ' + optimised_topojson[i].length + 
							'>>>\n' + optimised_topojson[i].substr(1, 80) + ' ... ');
				var update = client.query(update_stmt, [optimised_topojson, tile_id], function(err, result) {
					if(err) {
						client.end();
						return console.error('Error running update[' + i + ']: ' + tile_id + '; ' + update_stmt + ';', err);
					}
					else {
						update.on('end', function() {
							console.log('Processed tile_id [' + i + '] ' + tile_id + '; length: ' + optimised_geojson.length + 
							' => ' + optimised_topojson.length + 
							'>>>\n' + optimised_topojson.substr(1, 80) + ' ... ');
						});	
					}
				});
			}
		}
});

// Eof