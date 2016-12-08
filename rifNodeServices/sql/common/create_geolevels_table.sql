/*
 * SQL statement name: 	create_geolevels_table.sql
 * Type:				Common SQL statement
 * Parameters:
 *						1: geolevels table; e.g. geolevels_cb_2014_us_county_500k
 *						2: geography table; e.g. geography_cb_2014_us_county_500k
 *
 * Description:			Create geography table compatible with RIF40_GEOGRAPHIES
 *
 *	CREATE TABLE t_rif40_geolevels
 *	(
 *	  geography character varying(50) NOT NULL, -- Geography (e.g EW2001)
 *	  geolevel_name character varying(30) NOT NULL, -- Name of geolevel. This will be a column name in the numerator/denominator tables
 *	  geolevel_id smallint NOT NULL, -- ID for ordering (1=lowest resolution). Up to 99 supported.
 *	  description character varying(250) NOT NULL, -- Description
 *	  lookup_table character varying(30) NOT NULL, -- Lookup table name. This is used to translate codes to the common names, e.g a LADUA of 00BK is &quot;Westminster&quot;
 *	  lookup_desc_column character varying(30) NOT NULL, -- Lookup table description column name.
 *	  centroidxcoordinate_column character varying(30), -- Lookup table centroid X co-ordinate column name. Can also use CENTROIDSFILE instead.
 *	  centroidycoordinate_column character varying(30), -- Lookup table centroid Y co-ordinate column name.
 *	  shapefile character varying(512), -- Location of the GIS shape file. NULL if PostGress/PostGIS used. Can also use SHAPEFILE_GEOMETRY instead,
 *	  centroidsfile character varying(512), -- Location of the GIS centroids file. Can also use CENTROIDXCOORDINATE_COLUMN, CENTROIDYCOORDINATE_COLUMN instead.
 *	  shapefile_table character varying(30), -- Table containing GIS shape file data (created using shp2pgsql).
 *	  shapefile_area_id_column character varying(30), -- Column containing the AREA_IDs in SHAPEFILE_TABLE
 *	  shapefile_desc_column character varying(30), -- Column containing the AREA_ID descriptions in SHAPEFILE_TABLE
 *	  centroids_table character varying(30), -- Table containing GIS shape file data with Arc GIS calculated population weighted centroids (created using shp2pgsql). PostGIS does not support population weighted centroids.
 *	  centroids_area_id_column character varying(30), -- Column containing the AREA_IDs in CENTROIDS_TABLE. X and Y co-ordinates ciolumns are asummed to be named after CENTROIDXCOORDINATE_COLUMN and CENTROIDYCOORDINATE_COLUMN.
 *	  covariate_table character varying(30), -- Name of table used for covariates at this geolevel
 *	  restricted smallint DEFAULT 0, -- Is geolevel access rectricted by Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. This is enforced by the RIF application.
 *	  resolution smallint NOT NULL, -- Can use a map for selection at this resolution (0/1)
 *	  comparea smallint NOT NULL, -- Able to be used as a comparison area (0/1)
 *	  listing smallint NOT NULL, -- Able to be used in a disease map listing (0/1)
 *	  CONSTRAINT t_rif40_geolevels_pk PRIMARY KEY (geography, geolevel_name),
 *	  CONSTRAINT t_rif40_geol_comparea_ck CHECK (comparea IN (0, 1)),
 *	  CONSTRAINT t_rif40_geol_listing_ck CHECK (listing iN (0, 1)),
 *	  CONSTRAINT t_rif40_geol_resolution_ck CHECK (resolution IN (0, 1)),
 *	  CONSTRAINT t_rif40_geol_restricted_ck CHECK (restricted IN (0, 1))
 *	)
 *		 
 * Note:				%%%% becomes %% after substitution
*/
CREATE TABLE %1 (
       geography                       VARCHAR(50)  NOT NULL,
       geolevel_name                   VARCHAR(30)  NOT NULL,
       geolevel_id			           INTEGER	    NOT NULL,
       description                     VARCHAR(250) NOT NULL,
       lookup_table                    VARCHAR(30)  NOT NULL,
       lookup_desc_column              VARCHAR(30)  NOT NULL,
       shapefile                       VARCHAR(512) NOT NULL,
       shapefile_table                 VARCHAR(30)  NULL,
       shapefile_area_id_column        VARCHAR(30)  NOT NULL,
       shapefile_desc_column           VARCHAR(30)  NULL,
	   centroids_table 				   VARCHAR(30)  NULL, 
	   centroids_area_id_column 	   VARCHAR(30)  NULL,
	   covariate_table 				   VARCHAR(30)  NULL, 
       restricted 					   INTEGER      NULL DEFAULT 0,
       resolution                      INTEGER      NULL,
       comparea                        INTEGER      NULL,
       listing                         INTEGER      NULL,
	   areaid_count 				   INTEGER      NULL,
       CONSTRAINT %1_pk PRIMARY KEY(geography, geolevel_name),
	   CONSTRAINT %1_fk FOREIGN KEY (geography)
			REFERENCES %2 (geography), 
	   CONSTRAINT %1_comparea_ck CHECK (comparea IN (0, 1)),
	   CONSTRAINT %1_listing_ck CHECK (listing iN (0, 1)),
	   CONSTRAINT %1_resolution_ck CHECK (resolution IN (0, 1)),
	   CONSTRAINT %1_restricted_ck CHECK (restricted IN (0, 1))
)