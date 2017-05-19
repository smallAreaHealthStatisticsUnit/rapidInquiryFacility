\pset pager off
\set ECHO all
\set ON_ERROR_STOP ON
\timing

-- SQL statement 0: Start transaction >>>
BEGIN TRANSACTION;

-- SQL statement 133: Create tiles view >>>
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
 * Note:				%% becomes % after substitution
 */
CREATE OR REPLACE VIEW rif_data.tiles_sahsuland AS 
WITH a AS (
        SELECT geography,
               MAX(geolevel_id) AS max_geolevel_id
          FROM t_rif40_geolevels
		 WHERE geography = 'SAHSULAND'
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
          FROM b, t_rif40_geolevels b2
		 WHERE b.geolevel_id = b2.geolevel_id
		   AND b.geography   = b2.geography
), d AS (
        SELECT generate_series(0, 11, 1) AS zoomlevel
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
		 LEFT JOIN t_tiles_sahsuland h1 ON ( /* Multiple area ids in the geolevel */
				z.zoomlevel    = h1.zoomlevel AND 
				z.x            = h1.x AND 
				z.y            = h1.y AND 
				z.geolevel_id  = h1.geolevel_id)
		 LEFT JOIN t_tiles_sahsuland h2 ON ( /* Single area ids in the geolevel */
				h2.zoomlevel   = 0 AND 
				h2.x           = 0 AND 
				h2.y           = 0 AND 
				h2.geolevel_id = 1);

-- SQL statement 134: Comment tiles view >>>
COMMENT /*
 * SQL statement name: 	comment_view.sql
 * Type:				Postgres/PostGIS PL/pgsql anonymous block
 * Parameters:
 *						1: view; e.g. tiles_cb_us_county_500k
 *						2: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment view
 * Note:				%% becomes % after substitution
 */
	ON VIEW tiles_sahsuland IS 'Maptiles view for geography; empty tiles are added to complete zoomlevels for zoomlevels 0 to 11. This view is efficent!';

-- SQL statement 135: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.geography IS 'Geography';

-- SQL statement 136: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.geolevel_id IS 'ID for ordering (1=lowest resolution). Up to 99 supported.';

-- SQL statement 137: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.zoomlevel IS 'Zoom level: 0 to 11. Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11';

-- SQL statement 138: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.x IS 'X tile number. From 0 to (2**<zoomlevel>)-1';

-- SQL statement 139: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.y IS 'Y tile number. From 0 to (2**<zoomlevel>)-1';

-- SQL statement 140: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.optimised_topojson IS 'Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.';

-- SQL statement 141: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.tile_id IS 'Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>';

-- SQL statement 142: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.geolevel_name IS 'Name of geolevel. This will be a column name in the numerator/denominator tables';

-- SQL statement 143: Comment tiles view column >>>
COMMENT /*
 * SQL statement name: 	comment_view_column.sql
 * Type:				Postgres/PostGIS psql
 * Parameters:
 *						1: view; e.g. tiles_cb_2014_us_county_500k
 *						2: column; e.g. geolevel_name
 *						3: comment. Usual rules for comment text in SQK - single 
 *									quotes (') need to be double ('')
 *
 * Description:			Comment table
 * Note:				%% becomes % after substitution
 */
	ON COLUMN tiles_sahsuland.no_area_ids IS 'Tile contains no area_ids flag: 0/1';
	
GRANT SELECT ON tiles_sahsuland TO PUBLIC;

END TRANSACTION;

--
-- Eof