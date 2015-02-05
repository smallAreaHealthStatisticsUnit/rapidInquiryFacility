#!/usr/bin/env node

var pg = require('pg');
var topojson = require('topojson');

var conString = "postgres://rif40:se11afield2012@wpea-rif1/sahsuland_dev";
var geography = process.argv[2];
if (!geography) {
	geography = 'sahsu';
}
var client = new pg.Client(conString);
client.connect(function(err) {
  if(err) {
    return console.error('Could not connect to postgres using: ' + conString, err);
  }
    var sql_stmt = 'SELECT tile_id, optimised_geojson::Text AS optimised_geojson FROM t_rif40_' + geography + '_maptiles LIMIT 5';  
    var query = client.query(sql_stmt, function(err, result) {
    if(err) {
		client.end();
		return console.error('Error running query: ' + sql_stmt + ';', err);
    }
	else {
		var rows = [];
		query.on('row', function(row) {
			//fired once for each row returned
			rows.push(row);        
		});
		query.on('end', function(result) {
			//fired once and only once, after the last row has been returned and after all 'row' events are emitted
			//in this example, the 'rows' array now contains an ordered set of all the rows which we received from postgres
		console.log(result.rowCount + ' rows were processed.');
		})
		for (i = 0; i < result.rowCount; i++) { 
			var optimised_geojson = result.rows[i].optimised_geojson;
			var object = JSON.parse(optimised_geojson);
			var objects = {};
			if (object.type === "Topology") {
				for (var key in object.objects) {
					objects[key] = topojson.feature(object, object.objects[key]);
				}
			}
			else {
				client.end();
				return console.error('Not valid GeoJSON tile_id [' + i + '] ' + result.rows[i].tile_id);
			}
			var options = {
				"verbose": true,
				"post-quantization": 1e4};
			// Convert GeoJSON to TopoJSON.
			var topology = topojson.topology(objects, options); // convert to TopoJSON

  // Clear the input objects hash to allow garbage collection.
			objects = null;
			var optimised_topojson = topology.objects.collection; 
			console.log('Processing tile_id [' + i + '] ' + result.rows[i].tile_id + '; length: ' + optimised_geojson.length + ' => ' + optimised_topojson.length);
		}
	}
    client.end();
  });
});
