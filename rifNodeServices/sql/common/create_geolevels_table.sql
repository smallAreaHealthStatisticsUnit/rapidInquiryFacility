CREATE TABLE %1 (
       geography                       VARCHAR(50)  NOT NULL,
       geolevel_name                   VARCHAR(30)  NOT NULL,
       geolevel_id			        	integer	     NOT NULL,
       description                     VARCHAR(250) NOT NULL,
       lookup_table                    VARCHAR(30)  NOT NULL,
       lookup_desc_column              VARCHAR(30)  NOT NULL,
       shapefile                       VARCHAR(512) NOT NULL,
       shapefile_table                 VARCHAR(30)  NULL,
       shapefile_area_id_column        VARCHAR(30)  NOT NULL,
       shapefile_desc_column           VARCHAR(30)  NULL,
       resolution                      integer      NULL,
       comparea                        integer      NULL,
       listing                         integer      NULL,
       CONSTRAINT %1_pk PRIMARY KEY(geography, geolevel_name)
)