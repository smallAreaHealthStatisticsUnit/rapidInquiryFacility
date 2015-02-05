README for node.js programs

node.js is required to build to geoJSON to topoJSON converter by Mike Bostock at: https://github.com/mbostock/topojson/wiki/Installation

node.js is available from: http://nodejs.org/

Install MS Visual Studio from: http://e5.onthehub.com/WebStore/ProductsByMajorVersionList.aspx?ws=9cc4656f-b735-e211-aed3-f04da23e67f6&vsro=8 (UK Universities only)
[Install GDAL if QGis is not installed]
Install Python (2.7 or later) from https://www.python.org/downloads/ (NOT 3.x.x series!)
[Install FWTools (2.4.7) - not needed]
Install node.js
Then install topojson through npm:

npm install -g topojson

Test:

make 

C:\Users\pch\AppData\Roaming\npm\topojson.cmd -q 1e6 -o test_6_geojson_test_01.json ..\psql_scripts\test_scripts\data\test_6_geojson_test_01.json
bounds: -6.68852598 54.6456466 -6.32507059 55.01219818 (spherical)
pre-quantization: 0.0404m (3.63e-7°) 0.0408m (3.67e-7°)
topology: 160 arcs, 3502 points
prune: retained 160 / 160 arcs (100%)

Install Postgres connector

first type: pg_config is test Postgres extensibility is installed, pg-native requires MS Visual Studio.

P:\Github\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>npm install pg
pg@4.2.0 node_modules\pg
+-- packet-reader@0.2.0
+-- pg-connection-string@0.1.3
+-- buffer-writer@1.0.0
+-- generic-pool@2.1.1
+-- pg-types@1.6.0
+-- semver@4.2.0
+-- pgpass@0.0.3 (split@0.3.3)

SELECT tile_id
 FROM t_rif40_sahsu_maptiles;
 
SELECT tile_id, ST_AsText(ST_GeomFromGeoJSON(optimised_geojson::Text)) AS area
 FROM t_rif40_sahsu_maptiles
  LIMIT 1;
 
 create extension postgis_topology; /* As postgres */
GRANT USAGE ON SCHEMA topology TO rif40;
GRANT USAGE, SELECT ON SEQUENCE topology_id_seq TO rif40;
GRANT ALL ON topology TO rif40;
GRANT ALL ON layer TO rif40;


SELECT topology.CreateTopology('sahsuland_topo', 4326);

-- Add a layer
SELECT topology.AddTopoGeometryColumn('sahsuland_topo', 'rif40', 't_rif40_sahsu_geometry', 'topogeom', 'MULTIPOLYGON');

 
WITH a AS (
	SELECT tile_id, json_each(optimised_geojson) AS js
	  FROM t_rif40_sahsu_maptiles
	 WHERE tile_id = 'SAHSU_4_LEVEL4_0_0_0'
), b AS (
	SELECT tile_id, (js).key AS key, json_each(json_array_elements((js).value)) AS js
	  FROM a
	WHERE (js).key = 'features'
), c AS (
	SELECT tile_id, ST_SetSRID(ST_GeomFromGeoJSON((js).value::Text), 4326) AS geom, (js).value::Text AS js
	  FROM b
	 WHERE (js).key = 'geometry'
)
SELECT tile_id, ST_Area(geom) AS area, 
       SUBSTRING(js FROM 1 FOR 70) AS js,
	   SUBSTRING(
			topology.AsTopoJSON(
					topology.toTopoGeom(geom, 'sahsuland_topo', 1), NULL) FROM 1 FOR 70) AS ts
  FROM c;
	  
	  
	  -- (topology_id, layer_id, topogeo_id, type)
	  -- CONTEXT:  PL/pgSQL function totopogeom(geometry,character varying,integer,double precision) line 94 at assignment
NOTICE:  TopoGeometry is "(2,1,1230,3)", its topology_id is "2"
CONTEXT:  PL/pgSQL function totopogeom(geometry,character varying,integer,double precision) line 94 at assignment
NOTICE:  TopoGeometry is "(2,1,1231,3)", its topology_id is "2"
CONTEXT:  PL/pgSQL function totopogeom(geometry,character varying,integer,double precision) line 94 at assignment

WITH a AS (
	SELECT tile_id, json_each(optimised_geojson) AS js
	  FROM t_rif40_sahsu_maptiles
	 WHERE tile_id = 'SAHSU_4_LEVEL4_0_0_0'
), b AS (
	SELECT tile_id, (js).key AS key, json_each(json_array_elements((js).value)) AS js
	  FROM a
	WHERE (js).key = 'features'
)
SELECT tile_id, (js).key, SUBSTRING((js).value::Text FROM 1 FOR 70) AS js
  FROM b;

\copy (SELECT optimised_geojson FROM t_rif40_sahsu_maptiles WHERE tile_id = 'SAHSU_4_LEVEL4_0_0_0') to SAHSU_4_LEVEL4_0_0_0.json
\dS a

 
  LIMIT 1;