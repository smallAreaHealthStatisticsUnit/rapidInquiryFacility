/*
 * SQL statement name: 	create_geography_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: table; e.g. geography_cb_2014_us_county_500k
 *
 * Description:			Create geography table compatible with RIF40_GEOGRAPHIES
 *
 *		CREATE TABLE rif40_geographies
 *		(
 *		  geography character varying(50) NOT NULL, -- Geography name
 *		  description character varying(250) NOT NULL, -- Description
 *		  hierarchytable character varying(30) NOT NULL, -- Hierarchy table
 *		  tiletable character varying(30) NOT NULL, -- Tile table
 *		  geometrytable character varying(30) NOT NULL, -- Geometry table
 *		  srid integer DEFAULT 0, -- Postgres projection SRID
 *		  defaultcomparea character varying(30), -- Default comparison area
 *		  defaultstudyarea character varying(30), -- Default study area
 *		  postal_population_table character varying(30), -- Postal population table. Table of postal points (e.g. postcodes, ZIP codes); geolevels; X and YCOORDINATES (in projection SRID); male, female and total populations. Converted to SRID points by loader [not in 4326 Web Mercator lat/long]. Used in creating population wieght centroids and in converting postal points to geolevels. Expected columns &lt;postal_point_column&gt;, XCOORDINATE, YCOORDINATE, 1+ &lt;GEOLEVEL_NAME&gt;, MALES, FEMALES, TOTAL
 *		  postal_point_column character varying(30), -- Column name for postal points (e.g. POSTCODE, ZIP_CODE)
 *		  partition smallint DEFAULT 0, -- Enable partitioning. Extract tables will be partition if the number of years >= 2x the RIF40_PARAMETERS parameters Parallelisation [which has a default of 4, so extracts covering 8 years or more will be partitioned].
 *		  max_geojson_digits smallint DEFAULT 8, -- Max digits in ST_AsGeoJson() [optimises file size by removing unecessary precision, the default value of 8 is normally fine.]
 *		  CONSTRAINT rif40_geographies_pk PRIMARY KEY (geography),
 *		  CONSTRAINT partition_ck CHECK (partition = ANY (ARRAY[0, 1])),
 *		  CONSTRAINT postal_population_table_ck CHECK (postal_population_table IS NOT NULL AND postal_point_column IS NOT NULL OR postal_population_table IS NULL AND postal_point_column IS NULL)
 *		)
 *		 
 * Note:				%%%% becomes %% after substitution
 */
CREATE TABLE %1 (
       geography               VARCHAR(50)  NOT NULL,
       description             VARCHAR(250) NOT NULL,
       hierarchytable          VARCHAR(30)  NOT NULL,
       geometrytable           VARCHAR(30)  NOT NULL,
       tiletable               VARCHAR(30)  NOT NULL,			/* New for DB */
       srid                    INTEGER      NOT NULL DEFAULT 0,
       defaultcomparea         VARCHAR(30)  NULL,
       defaultstudyarea        VARCHAR(30)  NULL,
       minzoomlevel       	   INTEGER      NOT NULL DEFAULT 6,  /* New for DB */
       maxzoomlevel       	   INTEGER      NOT NULL DEFAULT 11, /* New for DB */
       postal_population_table VARCHAR(30)  NULL,
       postal_point_column 	   VARCHAR(30)  NULL,
       partition 			   INTEGER      NOT NULL DEFAULT 0, 
       max_geojson_digits 	   INTEGER      NOT NULL DEFAULT 8, 	   
       CONSTRAINT %1_pk PRIMARY KEY(geography),
	   CONSTRAINT partition_ck CHECK (partition IN (0, 1)),
	   CONSTRAINT postal_population_table_ck CHECK (
			postal_population_table IS NOT NULL AND postal_point_column IS NOT NULL OR postal_population_table IS NULL AND postal_point_column IS NULL)
	)