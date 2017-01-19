/*
 * SQL statement name: 	create_tiles_view.sql
 * Type:				Postgres/PostGIS SQL statement
 * Parameters:
 *						1: tiles view; e.g. tiles_cb_2014_us_county_500k
 *						2: geolevel table; e.g. geolevels_cb_2014_us_county_500k
 *						3: JSON datatype (Postgres JSON, SQL server VARCHAR) [No longer used]
 *						4: tiles table; e.g. t_tiles_cb_2014_us_500k
 *  					5: Max zoomlevel; e.g. 11
 *						6: Schema; e.g. rif_data. or ""
 *						7: RIF or user schema; e.g. $(USERNAME) or rif40
 *						8: Geography; e.g. USA_2014
 *
 * Description:			Create tiles view
 * Note:				%%%% becomes %% after substitution
 */
CREATE VIEW %6%1 AS 
WITH a AS (
        SELECT geography,
               MAX(geolevel_id) AS max_geolevel_id
          FROM %2
		 WHERE geography = '%8'
         GROUP BY geography
), b AS (
         SELECT a.geography,
                generate_series(1, a.max_geolevel_id::INTEGER, 1) AS geolevel_id
           FROM a
), c AS (
        SELECT b2.geolevel_name,
               b.geolevel_id,
               b.geography,
			   b2.areaid_count
          FROM b, %2 b2
		 WHERE b.geolevel_id = b2.geolevel_id
		   AND b.geography   = b2.geography
), d AS (
        SELECT generate_series(0, %5, 1) AS zoomlevel
), ex AS (
         SELECT d.zoomlevel,
                generate_series(0, POWER(2::DOUBLE PRECISION, d.zoomlevel::DOUBLE PRECISION)::INTEGER - 1, 1) AS xy_series
           FROM d
), ey AS (
        SELECT c.geolevel_name,
			   c.areaid_count,
               c.geolevel_id,
               c.geography,
               ex.zoomlevel,
               ex.xy_series
          FROM c,
               ex 
)
SELECT z.geography,
       z.geolevel_id,
       z.geolevel_name,
       CASE
            WHEN h1.tile_id IS NULL AND h2.tile_id IS NULL THEN 1
            ELSE 0
       END AS no_area_ids, 
       COALESCE(h1.tile_id, 
				z.geolevel_id::VARCHAR||'_'||z.geolevel_name||'_'||z.zoomlevel::VARCHAR||'_'||z.x::VARCHAR||'_'||z.y::VARCHAR) AS tile_id,
       z.x,
       z.y,
       z.zoomlevel,
       COALESCE(h1.optimised_topojson, 
				h2.optimised_topojson, 
				'{"type": "FeatureCollection","features":[]}'::JSON /* NULL geojson */) AS optimised_topojson
  FROM ( 
		SELECT ey.geolevel_name,
				ey.areaid_count,
                ey.geolevel_id,
                ey.geography,
                ex.zoomlevel,
                ex.xy_series AS x,
                ey.xy_series AS y
           FROM ey, ex /* Cross join */
          WHERE ex.zoomlevel = ey.zoomlevel
		) z 
		 LEFT JOIN %4 h1 ON ( /* Multiple area ids in the geolevel */
				z.areaid_count > 1 AND
				z.zoomlevel    = h1.zoomlevel AND 
				z.x            = h1.x AND 
				z.y            = h1.y AND 
				z.geolevel_id  = h1.geolevel_id)
		 LEFT JOIN %4 h2 ON ( /* Single area ids in the geolevel */
				z.areaid_count = 1 AND
				h2.zoomlevel   = 0 AND 
				h2.x           = 0 AND 
				h2.y           = 0 AND 
				h2.geolevel_id = 1)