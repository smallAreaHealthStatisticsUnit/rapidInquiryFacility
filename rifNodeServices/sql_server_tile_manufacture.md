SQL Server tile manufacture example log
=======================================

```
Created info log file: mssqlTileMaker.log
XML Directory C:/Users/Peter/AppData/Local/Temp/shpConvert/9c13713b-28cd-4c5e-b358-49bb8d23f0a5 does not exist: ENOENT; using : C:\Users\phamb\Documents\Local Data Loading\Tile maker USA
XML Directory C:\Users\phamb\Documents\Local Data Loading\Tile maker USA is readable
Parsed XML config file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/geoDataLoader.xml
About to connected to SQL server using: {
    "driver": "msnodesqlv8",
    "server": "localhost",
    "options": {
        "trustedConnection": false,
        "useUTC": true,
        "appName": "mssqlTileMaker.js",
        "encrypt": true
    },
    "database": "sahsuland_dev",
    "user": "peter",
    "password": "XXXXXX"
}
(node:15528) [DEP0064] DeprecationWarning: tls.createSecurePair() is deprecated. Please use tls.Socket instead.
Connected to SQL server using: {
    "driver": "msnodesqlv8",
    "server": "localhost",
    "options": {
        "trustedConnection": false,
        "useUTC": true,
        "appName": "mssqlTileMaker.js",
        "encrypt": true
    },
    "database": "sahsuland_dev",
    "user": "peter",
    "password": "XXXXXX",
    "port": 1433,
    "stream": false,
    "parseJSON": false
}; log level: info
SQL Server path OK
Creating hierarchy CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_hierarchy_usa_2014.csv for USA_2014: US 2014 Census geography to county level
Creating lookup CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_lookup_cb_2014_us_nation_5m.csv for USA_2014: US 2014 Census geography to county level
Creating lookup CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_lookup_cb_2014_us_state_500k.csv for USA_2014: US 2014 Census geography to county level
Creating lookup CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_lookup_cb_2014_us_county_500k.csv for USA_2014: US 2014 Census geography to county level
Creating adjacency CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_adjacency_usa_2014.csv for USA_2014: US 2014 Census geography to county level
Creating geometry CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_geometry_usa_2014.csv for USA_2014: US 2014 Census geography to county level
Created tile blocks table: tile_blocks_usa_2014
Processing geography_USA_2014
Description: US 2014 Census geography to county level
Sun, 03 Jun 2018 13:17:08 GMT tedious deprecated The `IntN` data type is internal and will be removed. at ..\..\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\mssql\lib\tedious.js:63:20
Zoomlevels to process per geolevel: 9
Creating tile CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_t_tiles_cb_2014_us_nation_5m.csv
Geolevel: 1; zooomlevel: 0; block: 1/1; 1 tile intersects processed; 1 tiles in 0.098 S; 10.2 tiles/S; total: 1 tiles in 12.406 S; size: 177.65KB
Geolevel: 1; zooomlevel: 1; END block: 0/0; 0 tile intersects processed no tiles in 0.01 S; total: 1 tiles in 12.583 S; size: 177.65KB
Geolevel: 1; zooomlevel: 2; END block: 0/0; 0 tile intersects processed no tiles in 0.001 S; total: 1 tiles in 12.588 S; size: 177.65KB
Geolevel: 1; zooomlevel: 3; END block: 0/0; 0 tile intersects processed no tiles in 0.001 S; total: 1 tiles in 12.592 S; size: 177.65KB
Geolevel: 1; zooomlevel: 4; END block: 0/0; 0 tile intersects processed no tiles in 0.001 S; total: 1 tiles in 12.597 S; size: 177.65KB
Geolevel: 1; zooomlevel: 5; END block: 0/0; 0 tile intersects processed no tiles in 0.001 S; total: 1 tiles in 12.602 S; size: 177.65KB
Geolevel: 1; zooomlevel: 6; END block: 0/0; 0 tile intersects processed no tiles in 0.001 S; total: 1 tiles in 12.607 S; size: 177.65KB
Geolevel: 1; zooomlevel: 7; END block: 0/0; 0 tile intersects processed no tiles in 0.001 S; total: 1 tiles in 12.611 S; size: 177.65KB
Geolevel: 1; zooomlevel: 8; END block: 0/0; 0 tile intersects processed no tiles in 0.001 S; total: 1 tiles in 12.616 S; size: 177.65KB
Geolevel: 1; zooomlevel: 9; END block: 0/0; 0 tile intersects processed no tiles in 0.002 S; total: 1 tiles in 12.621 S; size: 177.65KB
Creating tile CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_t_tiles_cb_2014_us_state_500k.csv
Geolevel: 2; zooomlevel: 0; block: 1/1; 56 tile intersects processed; 1 tiles in 0.383 S; 2.61 tiles/S; total: 2 tiles in 13.062 S; size: 1.24MB
Geolevel: 2; zooomlevel: 1; block: 1/1; 57 tile intersects processed; 3 tiles in 0.503 S; 5.96 tiles/S; total: 5 tiles in 14.088 S; size: 2.95MB
Geolevel: 2; zooomlevel: 2; block: 1/1; 67 tile intersects processed; 5 tiles in 0.865 S; 5.78 tiles/S; total: 10 tiles in 15.763 S; size: 5.52MB
Geolevel: 2; zooomlevel: 3; block: 1/2; 79 tile intersects processed; 9 tiles in 0.868 S; 10.37 tiles/S; total: 19 tiles in 17.516 S; size: 8.99MB
Geolevel: 2; zooomlevel: 3; block: 2/2; 2 tile intersects processed; 1 tiles in 0.005 S; 200 tiles/S; total: 20 tiles in 18.719 S; size: 9MB
Geolevel: 2; zooomlevel: 4; block: 1/3; 9 tile intersects processed; 9 tiles in 0.821 S; 10.96 tiles/S; total: 29 tiles in 19.548 S; size: 12.49MB
Geolevel: 2; zooomlevel: 4; block: 2/3; 81 tile intersects processed; 10 tiles in 0.618 S; 16.18 tiles/S; total: 39 tiles in 21.219 S; size: 14.96MB
Geolevel: 2; zooomlevel: 4; block: 3/3; 5 tile intersects processed; 3 tiles in 0.145 S; 20.69 tiles/S; total: 42 tiles in 22.146 S; size: 15.57MB
Geolevel: 2; zooomlevel: 5; block: 1/5; 9 tile intersects processed; 9 tiles in 0.838 S; 10.74 tiles/S; total: 51 tiles in 23.178 S; size: 19.04MB
Geolevel: 2; zooomlevel: 5; block: 2/5; 10 tile intersects processed; 10 tiles in 4.464 S; 2.24 tiles/S; total: 61 tiles in 28.766 S; size: 23.12MB
Geolevel: 2; zooomlevel: 5; block: 3/5; 23 tile intersects processed; 10 tiles in 0.64 S; 15.63 tiles/S; total: 71 tiles in 30.633 S; size: 25.82MB
Geolevel: 2; zooomlevel: 5; block: 4/5; 69 tile intersects processed; 10 tiles in 0.339 S; 29.5 tiles/S; total: 81 tiles in 31.904 S; size: 27.11MB
Geolevel: 2; zooomlevel: 5; block: 5/5; 31 tile intersects processed; 8 tiles in 0.263 S; 30.42 tiles/S; total: 89 tiles in 32.645 S; size: 28.29MB
Geolevel: 2; zooomlevel: 6; block: 1/12; 9 tile intersects processed; 9 tiles in 1.004 S; 8.96 tiles/S; total: 98 tiles in 33.988 S; size: 32.35MB
Geolevel: 2; zooomlevel: 6; block: 2/12; 10 tile intersects processed; 10 tiles in 1.381 S; 7.24 tiles/S; total: 108 tiles in 36.649 S; size: 36.39MB
Geolevel: 2; zooomlevel: 6; block: 3/12; 10 tile intersects processed; 10 tiles in 1.273 S; 7.86 tiles/S; total: 118 tiles in 39.155 S; size: 41.02MB
Geolevel: 2; zooomlevel: 6; block: 4/12; 10 tile intersects processed; 10 tiles in 1.396 S; 7.16 tiles/S; total: 128 tiles in 42.172 S; size: 46.2MB
Geolevel: 2; zooomlevel: 6; block: 5/12; 12 tile intersects processed; 10 tiles in 1.161 S; 8.61 tiles/S; total: 138 tiles in 45.207 S; size: 50.9MB
Geolevel: 2; zooomlevel: 6; block: 6/12; 26 tile intersects processed; 10 tiles in 0.144 S; 69.44 tiles/S; total: 148 tiles in 46.927 S; size: 51.47MB
Geolevel: 2; zooomlevel: 6; block: 7/12; 28 tile intersects processed; 10 tiles in 0.07 S; 142.86 tiles/S; total: 158 tiles in 47.186 S; size: 51.77MB
Geolevel: 2; zooomlevel: 6; block: 8/12; 28 tile intersects processed; 10 tiles in 0.164 S; 60.98 tiles/S; total: 168 tiles in 47.453 S; size: 52.44MB
Geolevel: 2; zooomlevel: 6; block: 9/12; 45 tile intersects processed; 10 tiles in 0.256 S; 39.06 tiles/S; total: 178 tiles in 47.931 S; size: 53.53MB
Geolevel: 2; zooomlevel: 6; block: 10/12; 43 tile intersects processed; 10 tiles in 0.24 S; 41.67 tiles/S; total: 188 tiles in 48.519 S; size: 54.57MB
Geolevel: 2; zooomlevel: 6; block: 11/12; 19 tile intersects processed; 10 tiles in 0.214 S; 46.73 tiles/S; total: 198 tiles in 49.046 S; size: 55.63MB
Geolevel: 2; zooomlevel: 6; block: 12/12; 2 tile intersects processed; 2 tiles in 0.271 S; 7.38 tiles/S; total: 200 tiles in 49.626 S; size: 56.78MB
Geolevel: 2; zooomlevel: 7; block: 1/29; 9 tile intersects processed; 9 tiles in 1.138 S; 7.91 tiles/S; total: 209 tiles in 51.256 S; size: 61.33MB
Geolevel: 2; zooomlevel: 7; block: 2/29; 10 tile intersects processed; 10 tiles in 1.511 S; 6.62 tiles/S; total: 219 tiles in 54.228 S; size: 67.29MB
Geolevel: 2; zooomlevel: 7; block: 3/29; 10 tile intersects processed; 10 tiles in 1.528 S; 6.54 tiles/S; total: 229 tiles in 57.621 S; size: 73.3MB
Geolevel: 2; zooomlevel: 7; block: 4/29; 10 tile intersects processed; 10 tiles in 2.041 S; 4.9 tiles/S; total: 239 tiles in 61.659 S; size: 80.74MB
Geolevel: 2; zooomlevel: 7; block: 5/29; 10 tile intersects processed; 10 tiles in 1.761 S; 5.68 tiles/S; total: 249 tiles in 65.748 S; size: 87.47MB
Geolevel: 2; zooomlevel: 7; block: 6/29; 10 tile intersects processed; 10 tiles in 1.342 S; 7.45 tiles/S; total: 259 tiles in 69.647 S; size: 92.76MB
Geolevel: 2; zooomlevel: 7; block: 7/29; 10 tile intersects processed; 10 tiles in 1.531 S; 6.53 tiles/S; total: 269 tiles in 72.84 S; size: 98.77MB
Geolevel: 2; zooomlevel: 7; block: 8/29; 10 tile intersects processed; 10 tiles in 2.026 S; 4.94 tiles/S; total: 279 tiles in 76.995 S; size: 106.21MB
Geolevel: 2; zooomlevel: 7; block: 9/29; 10 tile intersects processed; 10 tiles in 2.21 S; 4.52 tiles/S; total: 289 tiles in 82.129 S; size: 113.65MB
Geolevel: 2; zooomlevel: 7; block: 10/29; 10 tile intersects processed; 10 tiles in 1.857 S; 5.39 tiles/S; total: 299 tiles in 86.346 S; size: 121.09MB
Geolevel: 2; zooomlevel: 7; block: 11/29; 12 tile intersects processed; 10 tiles in 0.838 S; 11.93 tiles/S; total: 309 tiles in 90.132 S; size: 124.42MB
Geolevel: 2; zooomlevel: 7; block: 12/29; 16 tile intersects processed; 10 tiles in 0.128 S; 78.13 tiles/S; total: 319 tiles in 91.332 S; size: 125.06MB
Geolevel: 2; zooomlevel: 7; block: 13/29; 22 tile intersects processed; 10 tiles in 0.152 S; 65.79 tiles/S; total: 329 tiles in 91.661 S; size: 125.7MB
Geolevel: 2; zooomlevel: 7; block: 14/29; 21 tile intersects processed; 10 tiles in 0.105 S; 95.24 tiles/S; total: 339 tiles in 91.951 S; size: 126.06MB
Geolevel: 2; zooomlevel: 7; block: 15/29; 19 tile intersects processed; 10 tiles in 0.102 S; 98.04 tiles/S; total: 349 tiles in 92.195 S; size: 126.28MB
Geolevel: 2; zooomlevel: 7; block: 16/29; 21 tile intersects processed; 10 tiles in 0.082 S; 121.95 tiles/S; total: 359 tiles in 92.347 S; size: 126.65MB
Geolevel: 2; zooomlevel: 7; block: 17/29; 27 tile intersects processed; 10 tiles in 0.165 S; 60.61 tiles/S; total: 369 tiles in 92.767 S; size: 127.27MB
Geolevel: 2; zooomlevel: 7; block: 18/29; 16 tile intersects processed; 10 tiles in 0.209 S; 47.85 tiles/S; total: 379 tiles in 93.277 S; size: 127.96MB
Geolevel: 2; zooomlevel: 7; block: 19/29; 23 tile intersects processed; 10 tiles in 2.906 S; 3.44 tiles/S; total: 389 tiles in 96.414 S; size: 128.65MB
Geolevel: 2; zooomlevel: 7; block: 20/29; 22 tile intersects processed; 10 tiles in 0.189 S; 52.91 tiles/S; total: 399 tiles in 96.912 S; size: 129.46MB
Geolevel: 2; zooomlevel: 7; block: 21/29; 26 tile intersects processed; 10 tiles in 0.266 S; 37.59 tiles/S; total: 409 tiles in 97.432 S; size: 130.41MB
Geolevel: 2; zooomlevel: 7; block: 22/29; 26 tile intersects processed; 10 tiles in 0.196 S; 51.02 tiles/S; total: 419 tiles in 97.926 S; size: 131.23MB
Geolevel: 2; zooomlevel: 7; block: 23/29; 27 tile intersects processed; 10 tiles in 0.205 S; 48.78 tiles/S; total: 429 tiles in 98.393 S; size: 132.18MB
Geolevel: 2; zooomlevel: 7; block: 24/29; 24 tile intersects processed; 10 tiles in 0.247 S; 40.49 tiles/S; total: 439 tiles in 98.942 S; size: 133.11MB
Geolevel: 2; zooomlevel: 7; block: 25/29; 22 tile intersects processed; 10 tiles in 0.2 S; 50 tiles/S; total: 449 tiles in 99.447 S; size: 133.93MB
Geolevel: 2; zooomlevel: 7; block: 26/29; 31 tile intersects processed; 10 tiles in 0.179 S; 55.87 tiles/S; total: 459 tiles in 99.888 S; size: 134.74MB
Geolevel: 2; zooomlevel: 7; block: 27/29; 20 tile intersects processed; 10 tiles in 0.159 S; 62.89 tiles/S; total: 469 tiles in 100.284 S; size: 135.56MB
Geolevel: 2; zooomlevel: 7; block: 28/29; 11 tile intersects processed; 10 tiles in 0.431 S; 23.2 tiles/S; total: 479 tiles in 100.944 S; size: 137.31MB
Geolevel: 2; zooomlevel: 7; block: 29/29; 2 tile intersects processed; 2 tiles in 0.418 S; 4.78 tiles/S; total: 481 tiles in 101.998 S; size: 138.8MB
Geolevel: 2; zooomlevel: 8; block: 1/67; 9 tile intersects processed; 9 tiles in 1.86 S; 4.84 tiles/S; total: 490 tiles in 104.593 S; size: 145.3MB
Geolevel: 2; zooomlevel: 8; block: 2/67; 10 tile intersects processed; 10 tiles in 1.838 S; 5.44 tiles/S; total: 500 tiles in 108.786 S; size: 151.83MB
Geolevel: 2; zooomlevel: 8; block: 3/67; 10 tile intersects processed; 10 tiles in 2.375 S; 4.21 tiles/S; total: 510 tiles in 113.556 S; size: 160.12MB
Geolevel: 2; zooomlevel: 8; block: 4/67; 10 tile intersects processed; 10 tiles in 2.918 S; 3.43 tiles/S; total: 520 tiles in 119.647 S; size: 168.44MB
Geolevel: 2; zooomlevel: 8; block: 5/67; 10 tile intersects processed; 10 tiles in 2.54 S; 3.94 tiles/S; total: 530 tiles in 125.165 S; size: 177.64MB
Geolevel: 2; zooomlevel: 8; block: 6/67; 10 tile intersects processed; 10 tiles in 5.961 S; 1.68 tiles/S; total: 540 tiles in 138.037 S; size: 185.95MB
Geolevel: 2; zooomlevel: 8; block: 7/67; 10 tile intersects processed; 10 tiles in 2.771 S; 3.61 tiles/S; total: 550 tiles in 144.08 S; size: 195.15MB
Geolevel: 2; zooomlevel: 8; block: 8/67; 10 tile intersects processed; 10 tiles in 2.679 S; 3.73 tiles/S; total: 560 tiles in 149.9 S; size: 204.36MB
Geolevel: 2; zooomlevel: 8; block: 9/67; 10 tile intersects processed; 10 tiles in 2.528 S; 3.96 tiles/S; total: 570 tiles in 155.602 S; size: 213.56MB
Geolevel: 2; zooomlevel: 8; block: 10/67; 10 tile intersects processed; 10 tiles in 2.373 S; 4.21 tiles/S; total: 580 tiles in 161.067 S; size: 221.87MB
Geolevel: 2; zooomlevel: 8; block: 11/67; 10 tile intersects processed; 10 tiles in 2.424 S; 4.13 tiles/S; total: 590 tiles in 166.766 S; size: 230.19MB
Geolevel: 2; zooomlevel: 8; block: 12/67; 10 tile intersects processed; 10 tiles in 2.332 S; 4.29 tiles/S; total: 600 tiles in 172.317 S; size: 236.72MB
Geolevel: 2; zooomlevel: 8; block: 13/67; 10 tile intersects processed; 10 tiles in 2.023 S; 4.94 tiles/S; total: 610 tiles in 176.73 S; size: 244.14MB
Geolevel: 2; zooomlevel: 8; block: 14/67; 10 tile intersects processed; 10 tiles in 1.846 S; 5.42 tiles/S; total: 620 tiles in 181.162 S; size: 250.67MB
Geolevel: 2; zooomlevel: 8; block: 15/67; 10 tile intersects processed; 10 tiles in 2.574 S; 3.89 tiles/S; total: 630 tiles in 186.01 S; size: 259.88MB
Geolevel: 2; zooomlevel: 8; block: 16/67; 10 tile intersects processed; 10 tiles in 2.664 S; 3.75 tiles/S; total: 640 tiles in 191.808 S; size: 269.08MB
Geolevel: 2; zooomlevel: 8; block: 17/67; 10 tile intersects processed; 10 tiles in 2.439 S; 4.1 tiles/S; total: 650 tiles in 197.378 S; size: 278.28MB
Geolevel: 2; zooomlevel: 8; block: 18/67; 10 tile intersects processed; 10 tiles in 2.464 S; 4.06 tiles/S; total: 660 tiles in 202.912 S; size: 287.49MB
Geolevel: 2; zooomlevel: 8; block: 19/67; 10 tile intersects processed; 10 tiles in 2.547 S; 3.93 tiles/S; total: 670 tiles in 208.519 S; size: 296.69MB
Geolevel: 2; zooomlevel: 8; block: 20/67; 10 tile intersects processed; 10 tiles in 2.992 S; 3.34 tiles/S; total: 680 tiles in 214.766 S; size: 305.89MB
Geolevel: 2; zooomlevel: 8; block: 21/67; 10 tile intersects processed; 10 tiles in 2.452 S; 4.08 tiles/S; total: 690 tiles in 220.397 S; size: 315.09MB
Geolevel: 2; zooomlevel: 8; block: 22/67; 10 tile intersects processed; 10 tiles in 2.493 S; 4.01 tiles/S; total: 700 tiles in 226.078 S; size: 324.3MB
Geolevel: 2; zooomlevel: 8; block: 23/67; 10 tile intersects processed; 10 tiles in 2.495 S; 4.01 tiles/S; total: 710 tiles in 231.674 S; size: 333.5MB
Geolevel: 2; zooomlevel: 8; block: 24/67; 11 tile intersects processed; 10 tiles in 1.564 S; 6.39 tiles/S; total: 720 tiles in 236.422 S; size: 339.29MB
Geolevel: 2; zooomlevel: 8; block: 25/67; 12 tile intersects processed; 10 tiles in 0.116 S; 86.21 tiles/S; total: 730 tiles in 238.584 S; size: 339.88MB
Geolevel: 2; zooomlevel: 8; block: 26/67; 14 tile intersects processed; 10 tiles in 0.135 S; 74.07 tiles/S; total: 740 tiles in 238.863 S; size: 340.61MB
Geolevel: 2; zooomlevel: 8; block: 27/67; 16 tile intersects processed; 10 tiles in 0.136 S; 73.53 tiles/S; total: 750 tiles in 239.194 S; size: 341.27MB
Geolevel: 2; zooomlevel: 8; block: 28/67; 14 tile intersects processed; 10 tiles in 0.124 S; 80.65 tiles/S; total: 760 tiles in 239.493 S; size: 341.86MB
Geolevel: 2; zooomlevel: 8; block: 29/67; 22 tile intersects processed; 10 tiles in 0.176 S; 56.82 tiles/S; total: 770 tiles in 239.835 S; size: 342.67MB
Geolevel: 2; zooomlevel: 8; block: 30/67; 17 tile intersects processed; 10 tiles in 0.11 S; 90.91 tiles/S; total: 780 tiles in 240.171 S; size: 343.17MB
Geolevel: 2; zooomlevel: 8; block: 31/67; 22 tile intersects processed; 10 tiles in 0.09 S; 111.11 tiles/S; total: 790 tiles in 240.418 S; size: 343.5MB
Geolevel: 2; zooomlevel: 8; block: 32/67; 18 tile intersects processed; 10 tiles in 0.09 S; 111.11 tiles/S; total: 800 tiles in 240.628 S; size: 343.9MB
Geolevel: 2; zooomlevel: 8; block: 33/67; 19 tile intersects processed; 10 tiles in 0.072 S; 138.89 tiles/S; total: 810 tiles in 240.825 S; size: 344.23MB
Geolevel: 2; zooomlevel: 8; block: 34/67; 21 tile intersects processed; 10 tiles in 0.039 S; 256.41 tiles/S; total: 820 tiles in 240.983 S; size: 344.43MB
Geolevel: 2; zooomlevel: 8; block: 35/67; 17 tile intersects processed; 10 tiles in 0.042 S; 238.1 tiles/S; total: 830 tiles in 241.09 S; size: 344.63MB
Geolevel: 2; zooomlevel: 8; block: 36/67; 19 tile intersects processed; 10 tiles in 0.158 S; 63.29 tiles/S; total: 840 tiles in 241.309 S; size: 345.19MB
Geolevel: 2; zooomlevel: 8; block: 37/67; 19 tile intersects processed; 10 tiles in 0.203 S; 49.26 tiles/S; total: 850 tiles in 241.71 S; size: 345.99MB
Geolevel: 2; zooomlevel: 8; block: 38/67; 21 tile intersects processed; 10 tiles in 0.223 S; 44.84 tiles/S; total: 860 tiles in 242.164 S; size: 346.98MB
Geolevel: 2; zooomlevel: 8; block: 39/67; 20 tile intersects processed; 10 tiles in 0.147 S; 68.03 tiles/S; total: 870 tiles in 242.59 S; size: 347.61MB
Geolevel: 2; zooomlevel: 8; block: 40/67; 16 tile intersects processed; 10 tiles in 0.208 S; 48.08 tiles/S; total: 880 tiles in 242.989 S; size: 348.5MB
Geolevel: 2; zooomlevel: 8; block: 41/67; 17 tile intersects processed; 10 tiles in 0.196 S; 51.02 tiles/S; total: 890 tiles in 243.447 S; size: 349.32MB
Geolevel: 2; zooomlevel: 8; block: 42/67; 17 tile intersects processed; 10 tiles in 0.184 S; 54.35 tiles/S; total: 900 tiles in 243.873 S; size: 350.09MB
Geolevel: 2; zooomlevel: 8; block: 43/67; 25 tile intersects processed; 10 tiles in 0.189 S; 52.91 tiles/S; total: 910 tiles in 244.295 S; size: 350.82MB
Geolevel: 2; zooomlevel: 8; block: 44/67; 20 tile intersects processed; 10 tiles in 0.174 S; 57.47 tiles/S; total: 920 tiles in 244.698 S; size: 351.59MB
Geolevel: 2; zooomlevel: 8; block: 45/67; 18 tile intersects processed; 10 tiles in 0.226 S; 44.25 tiles/S; total: 930 tiles in 245.159 S; size: 352.61MB
Geolevel: 2; zooomlevel: 8; block: 46/67; 21 tile intersects processed; 10 tiles in 0.266 S; 37.59 tiles/S; total: 940 tiles in 245.705 S; size: 353.86MB
Geolevel: 2; zooomlevel: 8; block: 47/67; 18 tile intersects processed; 10 tiles in 0.189 S; 52.91 tiles/S; total: 950 tiles in 246.228 S; size: 354.77MB
Geolevel: 2; zooomlevel: 8; block: 48/67; 24 tile intersects processed; 10 tiles in 0.165 S; 60.61 tiles/S; total: 960 tiles in 246.631 S; size: 355.45MB
Geolevel: 2; zooomlevel: 8; block: 49/67; 22 tile intersects processed; 10 tiles in 0.249 S; 40.16 tiles/S; total: 970 tiles in 247.092 S; size: 356.64MB
Geolevel: 2; zooomlevel: 8; block: 50/67; 14 tile intersects processed; 10 tiles in 0.212 S; 47.17 tiles/S; total: 980 tiles in 247.617 S; size: 357.68MB
Geolevel: 2; zooomlevel: 8; block: 51/67; 24 tile intersects processed; 10 tiles in 0.139 S; 71.94 tiles/S; total: 990 tiles in 248.023 S; size: 358.33MB
Geolevel: 2; zooomlevel: 8; block: 52/67; 18 tile intersects processed; 10 tiles in 0.247 S; 40.49 tiles/S; total: 1000 tiles in 248.464 S; size: 359.54MB
Geolevel: 2; zooomlevel: 8; block: 53/67; 18 tile intersects processed; 10 tiles in 0.189 S; 52.91 tiles/S; total: 1010 tiles in 248.958 S; size: 360.53MB
Geolevel: 2; zooomlevel: 8; block: 54/67; 18 tile intersects processed; 10 tiles in 0.202 S; 49.5 tiles/S; total: 1020 tiles in 249.424 S; size: 361.55MB
Geolevel: 2; zooomlevel: 8; block: 55/67; 22 tile intersects processed; 10 tiles in 0.247 S; 40.49 tiles/S; total: 1030 tiles in 249.934 S; size: 362.68MB
Geolevel: 2; zooomlevel: 8; block: 56/67; 23 tile intersects processed; 10 tiles in 0.221 S; 45.25 tiles/S; total: 1040 tiles in 250.453 S; size: 363.56MB
Geolevel: 2; zooomlevel: 8; block: 57/67; 16 tile intersects processed; 10 tiles in 0.164 S; 60.98 tiles/S; total: 1050 tiles in 250.902 S; size: 364.31MB
Geolevel: 2; zooomlevel: 8; block: 58/67; 16 tile intersects processed; 10 tiles in 0.171 S; 58.48 tiles/S; total: 1060 tiles in 251.299 S; size: 365.16MB
Geolevel: 2; zooomlevel: 8; block: 59/67; 19 tile intersects processed; 10 tiles in 0.164 S; 60.98 tiles/S; total: 1070 tiles in 251.699 S; size: 365.97MB
Geolevel: 2; zooomlevel: 8; block: 60/67; 16 tile intersects processed; 10 tiles in 0.164 S; 60.98 tiles/S; total: 1080 tiles in 252.108 S; size: 366.79MB
Geolevel: 2; zooomlevel: 8; block: 61/67; 20 tile intersects processed; 10 tiles in 0.154 S; 64.94 tiles/S; total: 1090 tiles in 252.483 S; size: 367.54MB
Geolevel: 2; zooomlevel: 8; block: 62/67; 24 tile intersects processed; 10 tiles in 0.176 S; 56.82 tiles/S; total: 1100 tiles in 252.874 S; size: 368.33MB
Geolevel: 2; zooomlevel: 8; block: 63/67; 24 tile intersects processed; 10 tiles in 0.154 S; 64.94 tiles/S; total: 1110 tiles in 253.249 S; size: 369.09MB
Geolevel: 2; zooomlevel: 8; block: 64/67; 11 tile intersects processed; 10 tiles in 0.15 S; 66.67 tiles/S; total: 1120 tiles in 253.611 S; size: 369.89MB
Geolevel: 2; zooomlevel: 8; block: 65/67; 12 tile intersects processed; 10 tiles in 0.121 S; 82.64 tiles/S; total: 1130 tiles in 253.958 S; size: 370.53MB
Geolevel: 2; zooomlevel: 8; block: 66/67; 10 tile intersects processed; 10 tiles in 0.252 S; 39.68 tiles/S; total: 1140 tiles in 254.384 S; size: 371.58MB
Geolevel: 2; zooomlevel: 8; block: 67/67; 6 tile intersects processed; 6 tiles in 1.497 S; 4.01 tiles/S; total: 1146 tiles in 256.232 S; size: 377.1MB
Geolevel: 2; zooomlevel: 9; block: 1/157; 9 tile intersects processed; 9 tiles in 2.603 S; 3.46 tiles/S; total: 1155 tiles in 260.76 S; size: 385.59MB
Geolevel: 2; zooomlevel: 9; block: 2/157; 10 tile intersects processed; 10 tiles in 2.34 S; 4.27 tiles/S; total: 1165 tiles in 266.358 S; size: 393.1MB
Geolevel: 2; zooomlevel: 9; block: 3/157; 10 tile intersects processed; 10 tiles in 3.276 S; 3.05 tiles/S; total: 1175 tiles in 272.823 S; size: 403.67MB
Geolevel: 2; zooomlevel: 9; block: 4/157; 10 tile intersects processed; 10 tiles in 2.657 S; 3.76 tiles/S; total: 1185 tiles in 279.63 S; size: 412.18MB
Geolevel: 2; zooomlevel: 9; block: 5/157; 10 tile intersects processed; 10 tiles in 3.168 S; 3.16 tiles/S; total: 1195 tiles in 286.092 S; size: 422.75MB
Geolevel: 2; zooomlevel: 9; block: 6/157; 10 tile intersects processed; 10 tiles in 3.31 S; 3.02 tiles/S; total: 1205 tiles in 293.481 S; size: 433.32MB
Geolevel: 2; zooomlevel: 9; block: 7/157; 10 tile intersects processed; 10 tiles in 2.894 S; 3.46 tiles/S; total: 1215 tiles in 300.382 S; size: 442.87MB
Geolevel: 2; zooomlevel: 9; block: 8/157; 10 tile intersects processed; 10 tiles in 3.44 S; 2.91 tiles/S; total: 1225 tiles in 307.568 S; size: 453.44MB
Geolevel: 2; zooomlevel: 9; block: 9/157; 10 tile intersects processed; 10 tiles in 3.295 S; 3.03 tiles/S; total: 1235 tiles in 314.978 S; size: 464.01MB
Geolevel: 2; zooomlevel: 9; block: 10/157; 10 tile intersects processed; 10 tiles in 2.891 S; 3.46 tiles/S; total: 1245 tiles in 321.907 S; size: 473.55MB
Geolevel: 2; zooomlevel: 9; block: 11/157; 10 tile intersects processed; 10 tiles in 3.244 S; 3.08 tiles/S; total: 1255 tiles in 328.718 S; size: 484.12MB
Geolevel: 2; zooomlevel: 9; block: 12/157; 10 tile intersects processed; 10 tiles in 3.243 S; 3.08 tiles/S; total: 1265 tiles in 335.951 S; size: 494.69MB
Geolevel: 2; zooomlevel: 9; block: 13/157; 10 tile intersects processed; 10 tiles in 3.272 S; 3.06 tiles/S; total: 1275 tiles in 343.262 S; size: 505.26MB
Geolevel: 2; zooomlevel: 9; block: 14/157; 10 tile intersects processed; 10 tiles in 3.214 S; 3.11 tiles/S; total: 1285 tiles in 350.424 S; size: 515.84MB
Geolevel: 2; zooomlevel: 9; block: 15/157; 10 tile intersects processed; 10 tiles in 3.207 S; 3.12 tiles/S; total: 1295 tiles in 357.647 S; size: 526.41MB
Geolevel: 2; zooomlevel: 9; block: 16/157; 10 tile intersects processed; 10 tiles in 3.25 S; 3.08 tiles/S; total: 1305 tiles in 364.863 S; size: 536.98MB
Geolevel: 2; zooomlevel: 9; block: 17/157; 10 tile intersects processed; 10 tiles in 3.225 S; 3.1 tiles/S; total: 1315 tiles in 372.034 S; size: 547.55MB
Geolevel: 2; zooomlevel: 9; block: 18/157; 10 tile intersects processed; 10 tiles in 3.193 S; 3.13 tiles/S; total: 1325 tiles in 379.226 S; size: 558.12MB
Geolevel: 2; zooomlevel: 9; block: 19/157; 10 tile intersects processed; 10 tiles in 3.245 S; 3.08 tiles/S; total: 1335 tiles in 386.444 S; size: 568.69MB
Geolevel: 2; zooomlevel: 9; block: 20/157; 10 tile intersects processed; 10 tiles in 2.887 S; 3.46 tiles/S; total: 1345 tiles in 393.276 S; size: 578.23MB
Geolevel: 2; zooomlevel: 9; block: 21/157; 10 tile intersects processed; 10 tiles in 3.196 S; 3.13 tiles/S; total: 1355 tiles in 400.047 S; size: 588.81MB
Geolevel: 2; zooomlevel: 9; block: 22/157; 10 tile intersects processed; 10 tiles in 3.307 S; 3.02 tiles/S; total: 1365 tiles in 407.318 S; size: 599.38MB
Geolevel: 2; zooomlevel: 9; block: 23/157; 10 tile intersects processed; 10 tiles in 3.026 S; 3.3 tiles/S; total: 1375 tiles in 414.465 S; size: 608.92MB
Geolevel: 2; zooomlevel: 9; block: 24/157; 10 tile intersects processed; 10 tiles in 3.186 S; 3.14 tiles/S; total: 1385 tiles in 421.184 S; size: 619.49MB
Geolevel: 2; zooomlevel: 9; block: 25/157; 10 tile intersects processed; 10 tiles in 2.55 S; 3.92 tiles/S; total: 1395 tiles in 427.695 S; size: 628.02MB
Geolevel: 2; zooomlevel: 9; block: 26/157; 10 tile intersects processed; 10 tiles in 2.642 S; 3.79 tiles/S; total: 1405 tiles in 433.747 S; size: 636.55MB
Geolevel: 2; zooomlevel: 9; block: 27/157; 10 tile intersects processed; 10 tiles in 3.33 S; 3 tiles/S; total: 1415 tiles in 440.334 S; size: 646.1MB
Geolevel: 2; zooomlevel: 9; block: 28/157; 10 tile intersects processed; 10 tiles in 2.561 S; 3.9 tiles/S; total: 1425 tiles in 446.455 S; size: 654.62MB
Geolevel: 2; zooomlevel: 9; block: 29/157; 10 tile intersects processed; 10 tiles in 2.883 S; 3.47 tiles/S; total: 1435 tiles in 452.504 S; size: 664.17MB
Geolevel: 2; zooomlevel: 9; block: 30/157; 10 tile intersects processed; 10 tiles in 2.557 S; 3.91 tiles/S; total: 1445 tiles in 458.599 S; size: 672.7MB
Geolevel: 2; zooomlevel: 9; block: 31/157; 10 tile intersects processed; 10 tiles in 1.983 S; 5.04 tiles/S; total: 1455 tiles in 463.766 S; size: 679.18MB
Geolevel: 2; zooomlevel: 9; block: 32/157; 10 tile intersects processed; 10 tiles in 2.244 S; 4.46 tiles/S; total: 1465 tiles in 468.445 S; size: 686.69MB
Geolevel: 2; zooomlevel: 9; block: 33/157; 10 tile intersects processed; 10 tiles in 3.316 S; 3.02 tiles/S; total: 1475 tiles in 474.564 S; size: 697.26MB
Geolevel: 2; zooomlevel: 9; block: 34/157; 10 tile intersects processed; 10 tiles in 3.268 S; 3.06 tiles/S; total: 1485 tiles in 481.843 S; size: 707.83MB
Geolevel: 2; zooomlevel: 9; block: 35/157; 10 tile intersects processed; 10 tiles in 3.212 S; 3.11 tiles/S; total: 1495 tiles in 489.022 S; size: 718.4MB
Geolevel: 2; zooomlevel: 9; block: 36/157; 10 tile intersects processed; 10 tiles in 3.239 S; 3.09 tiles/S; total: 1505 tiles in 496.159 S; size: 728.97MB
Geolevel: 2; zooomlevel: 9; block: 37/157; 10 tile intersects processed; 10 tiles in 3.18 S; 3.14 tiles/S; total: 1515 tiles in 503.274 S; size: 739.54MB
Geolevel: 2; zooomlevel: 9; block: 38/157; 10 tile intersects processed; 10 tiles in 3.182 S; 3.14 tiles/S; total: 1525 tiles in 510.415 S; size: 750.11MB
Geolevel: 2; zooomlevel: 9; block: 39/157; 10 tile intersects processed; 10 tiles in 3.205 S; 3.12 tiles/S; total: 1535 tiles in 517.617 S; size: 760.68MB
Geolevel: 2; zooomlevel: 9; block: 40/157; 10 tile intersects processed; 10 tiles in 3.166 S; 3.16 tiles/S; total: 1545 tiles in 524.748 S; size: 771.25MB
Geolevel: 2; zooomlevel: 9; block: 41/157; 10 tile intersects processed; 10 tiles in 3.16 S; 3.16 tiles/S; total: 1555 tiles in 531.865 S; size: 781.82MB
Geolevel: 2; zooomlevel: 9; block: 42/157; 10 tile intersects processed; 10 tiles in 3.291 S; 3.04 tiles/S; total: 1565 tiles in 539.122 S; size: 792.39MB
Geolevel: 2; zooomlevel: 9; block: 43/157; 10 tile intersects processed; 10 tiles in 3.269 S; 3.06 tiles/S; total: 1575 tiles in 546.374 S; size: 802.96MB
Geolevel: 2; zooomlevel: 9; block: 44/157; 10 tile intersects processed; 10 tiles in 3.167 S; 3.16 tiles/S; total: 1585 tiles in 553.523 S; size: 813.53MB
Geolevel: 2; zooomlevel: 9; block: 45/157; 10 tile intersects processed; 10 tiles in 3.193 S; 3.13 tiles/S; total: 1595 tiles in 560.731 S; size: 824.1MB
Geolevel: 2; zooomlevel: 9; block: 46/157; 10 tile intersects processed; 10 tiles in 3.201 S; 3.12 tiles/S; total: 1605 tiles in 567.876 S; size: 834.67MB
Geolevel: 2; zooomlevel: 9; block: 47/157; 10 tile intersects processed; 10 tiles in 3.457 S; 2.89 tiles/S; total: 1615 tiles in 575.416 S; size: 845.24MB
Geolevel: 2; zooomlevel: 9; block: 48/157; 10 tile intersects processed; 10 tiles in 3.362 S; 2.97 tiles/S; total: 1625 tiles in 582.958 S; size: 855.81MB
Geolevel: 2; zooomlevel: 9; block: 49/157; 10 tile intersects processed; 10 tiles in 3.302 S; 3.03 tiles/S; total: 1635 tiles in 590.568 S; size: 866.38MB
Geolevel: 2; zooomlevel: 9; block: 50/157; 10 tile intersects processed; 10 tiles in 3.324 S; 3.01 tiles/S; total: 1645 tiles in 598.16 S; size: 876.95MB
Geolevel: 2; zooomlevel: 9; block: 51/157; 10 tile intersects processed; 10 tiles in 3.302 S; 3.03 tiles/S; total: 1655 tiles in 605.8 S; size: 887.52MB
Geolevel: 2; zooomlevel: 9; block: 52/157; 10 tile intersects processed; 10 tiles in 3.352 S; 2.98 tiles/S; total: 1665 tiles in 613.358 S; size: 898.09MB
Geolevel: 2; zooomlevel: 9; block: 53/157; 10 tile intersects processed; 10 tiles in 3.205 S; 3.12 tiles/S; total: 1675 tiles in 620.463 S; size: 908.66MB
Geolevel: 2; zooomlevel: 9; block: 54/157; 10 tile intersects processed; 10 tiles in 3.201 S; 3.12 tiles/S; total: 1685 tiles in 627.643 S; size: 919.23MB
Geolevel: 2; zooomlevel: 9; block: 55/157; 10 tile intersects processed; 10 tiles in 3.187 S; 3.14 tiles/S; total: 1695 tiles in 634.781 S; size: 929.8MB
Geolevel: 2; zooomlevel: 9; block: 56/157; 10 tile intersects processed; 10 tiles in 3.2 S; 3.13 tiles/S; total: 1705 tiles in 641.922 S; size: 940.37MB
Geolevel: 2; zooomlevel: 9; block: 57/157; 10 tile intersects processed; 10 tiles in 3.192 S; 3.13 tiles/S; total: 1715 tiles in 649.064 S; size: 950.94MB
Geolevel: 2; zooomlevel: 9; block: 58/157; 10 tile intersects processed; 10 tiles in 2.323 S; 4.3 tiles/S; total: 1725 tiles in 655.308 S; size: 958.57MB
Geolevel: 2; zooomlevel: 9; block: 59/157; 11 tile intersects processed; 10 tiles in 0.167 S; 59.88 tiles/S; total: 1735 tiles in 658.277 S; size: 959.23MB
Geolevel: 2; zooomlevel: 9; block: 60/157; 11 tile intersects processed; 10 tiles in 0.139 S; 71.94 tiles/S; total: 1745 tiles in 658.606 S; size: 959.85MB
Geolevel: 2; zooomlevel: 9; block: 61/157; 12 tile intersects processed; 10 tiles in 0.19 S; 52.63 tiles/S; total: 1755 tiles in 658.954 S; size: 960.78MB
Geolevel: 2; zooomlevel: 9; block: 62/157; 14 tile intersects processed; 10 tiles in 0.202 S; 49.5 tiles/S; total: 1765 tiles in 659.398 S; size: 961.72MB
Geolevel: 2; zooomlevel: 9; block: 63/157; 13 tile intersects processed; 10 tiles in 0.205 S; 48.78 tiles/S; total: 1775 tiles in 659.853 S; size: 962.64MB
Geolevel: 2; zooomlevel: 9; block: 64/157; 13 tile intersects processed; 10 tiles in 0.174 S; 57.47 tiles/S; total: 1785 tiles in 660.281 S; size: 963.5MB
Geolevel: 2; zooomlevel: 9; block: 65/157; 17 tile intersects processed; 10 tiles in 0.183 S; 54.64 tiles/S; total: 1795 tiles in 660.702 S; size: 964.33MB
Geolevel: 2; zooomlevel: 9; block: 66/157; 16 tile intersects processed; 10 tiles in 0.187 S; 53.48 tiles/S; total: 1805 tiles in 661.119 S; size: 965.09MB
Geolevel: 2; zooomlevel: 9; block: 67/157; 15 tile intersects processed; 10 tiles in 0.163 S; 61.35 tiles/S; total: 1815 tiles in 661.503 S; size: 965.83MB
Geolevel: 2; zooomlevel: 9; block: 68/157; 15 tile intersects processed; 10 tiles in 0.187 S; 53.48 tiles/S; total: 1825 tiles in 661.905 S; size: 966.69MB
Geolevel: 2; zooomlevel: 9; block: 69/157; 21 tile intersects processed; 10 tiles in 0.229 S; 43.67 tiles/S; total: 1835 tiles in 662.391 S; size: 967.72MB
Geolevel: 2; zooomlevel: 9; block: 70/157; 18 tile intersects processed; 10 tiles in 0.142 S; 70.42 tiles/S; total: 1845 tiles in 662.827 S; size: 968.37MB
Geolevel: 2; zooomlevel: 9; block: 71/157; 18 tile intersects processed; 10 tiles in 0.132 S; 75.76 tiles/S; total: 1855 tiles in 663.209 S; size: 968.94MB
Geolevel: 2; zooomlevel: 9; block: 72/157; 18 tile intersects processed; 10 tiles in 0.14 S; 71.43 tiles/S; total: 1865 tiles in 663.531 S; size: 969.48MB
Geolevel: 2; zooomlevel: 9; block: 73/157; 20 tile intersects processed; 10 tiles in 0.15 S; 66.67 tiles/S; total: 1875 tiles in 663.867 S; size: 970.05MB
Geolevel: 2; zooomlevel: 9; block: 74/157; 21 tile intersects processed; 10 tiles in 0.064 S; 156.25 tiles/S; total: 1885 tiles in 664.099 S; size: 970.31MB
Geolevel: 2; zooomlevel: 9; block: 75/157; 20 tile intersects processed; 10 tiles in 0.105 S; 95.24 tiles/S; total: 1895 tiles in 664.301 S; size: 970.77MB
Geolevel: 2; zooomlevel: 9; block: 76/157; 17 tile intersects processed; 10 tiles in 0.099 S; 101.01 tiles/S; total: 1905 tiles in 664.537 S; size: 971.17MB
Geolevel: 2; zooomlevel: 9; block: 77/157; 15 tile intersects processed; 10 tiles in 0.068 S; 147.06 tiles/S; total: 1915 tiles in 664.75 S; size: 971.49MB
Geolevel: 2; zooomlevel: 9; block: 78/157; 18 tile intersects processed; 10 tiles in 0.098 S; 102.04 tiles/S; total: 1925 tiles in 664.955 S; size: 971.87MB
Geolevel: 2; zooomlevel: 9; block: 79/157; 19 tile intersects processed; 10 tiles in 0.065 S; 153.85 tiles/S; total: 1935 tiles in 665.148 S; size: 972.2MB
Geolevel: 2; zooomlevel: 9; block: 80/157; 19 tile intersects processed; 10 tiles in 0.043 S; 232.56 tiles/S; total: 1945 tiles in 665.293 S; size: 972.4MB
Geolevel: 2; zooomlevel: 9; block: 81/157; 22 tile intersects processed; 10 tiles in 0.057 S; 175.44 tiles/S; total: 1955 tiles in 665.427 S; size: 972.66MB
Geolevel: 2; zooomlevel: 9; block: 82/157; 17 tile intersects processed; 10 tiles in 0.055 S; 181.82 tiles/S; total: 1965 tiles in 665.558 S; size: 972.92MB
Geolevel: 2; zooomlevel: 9; block: 83/157; 15 tile intersects processed; 10 tiles in 0.049 S; 204.08 tiles/S; total: 1975 tiles in 665.683 S; size: 973.11MB
Geolevel: 2; zooomlevel: 9; block: 84/157; 19 tile intersects processed; 10 tiles in 0.159 S; 62.89 tiles/S; total: 1985 tiles in 665.919 S; size: 973.79MB
Geolevel: 2; zooomlevel: 9; block: 85/157; 14 tile intersects processed; 10 tiles in 0.235 S; 42.55 tiles/S; total: 1995 tiles in 666.356 S; size: 974.75MB
Geolevel: 2; zooomlevel: 9; block: 86/157; 17 tile intersects processed; 10 tiles in 0.21 S; 47.62 tiles/S; total: 2005 tiles in 666.838 S; size: 975.7MB
Geolevel: 2; zooomlevel: 9; block: 87/157; 22 tile intersects processed; 10 tiles in 0.124 S; 80.65 tiles/S; total: 2015 tiles in 667.214 S; size: 976.26MB
Geolevel: 2; zooomlevel: 9; block: 88/157; 20 tile intersects processed; 10 tiles in 0.147 S; 68.03 tiles/S; total: 2025 tiles in 667.536 S; size: 976.9MB
Geolevel: 2; zooomlevel: 9; block: 89/157; 22 tile intersects processed; 10 tiles in 0.343 S; 29.15 tiles/S; total: 2035 tiles in 668.054 S; size: 978.34MB
Geolevel: 2; zooomlevel: 9; block: 90/157; 18 tile intersects processed; 10 tiles in 0.135 S; 74.07 tiles/S; total: 2045 tiles in 668.591 S; size: 978.98MB
Geolevel: 2; zooomlevel: 9; block: 91/157; 19 tile intersects processed; 10 tiles in 0.159 S; 62.89 tiles/S; total: 2055 tiles in 668.925 S; size: 979.64MB
Geolevel: 2; zooomlevel: 9; block: 92/157; 17 tile intersects processed; 10 tiles in 0.232 S; 43.1 tiles/S; total: 2065 tiles in 669.342 S; size: 980.6MB
Geolevel: 2; zooomlevel: 9; block: 93/157; 16 tile intersects processed; 10 tiles in 0.26 S; 38.46 tiles/S; total: 2075 tiles in 669.868 S; size: 981.68MB
Geolevel: 2; zooomlevel: 9; block: 94/157; 17 tile intersects processed; 10 tiles in 0.259 S; 38.61 tiles/S; total: 2085 tiles in 670.431 S; size: 982.77MB
Geolevel: 2; zooomlevel: 9; block: 95/157; 16 tile intersects processed; 10 tiles in 0.267 S; 37.45 tiles/S; total: 2095 tiles in 671.017 S; size: 983.85MB
Geolevel: 2; zooomlevel: 9; block: 96/157; 15 tile intersects processed; 10 tiles in 0.248 S; 40.32 tiles/S; total: 2105 tiles in 671.567 S; size: 984.92MB
Geolevel: 2; zooomlevel: 9; block: 97/157; 20 tile intersects processed; 10 tiles in 0.233 S; 42.92 tiles/S; total: 2115 tiles in 672.113 S; size: 985.81MB
Geolevel: 2; zooomlevel: 9; block: 98/157; 16 tile intersects processed; 10 tiles in 0.29 S; 34.48 tiles/S; total: 2125 tiles in 672.684 S; size: 987MB
Geolevel: 2; zooomlevel: 9; block: 99/157; 23 tile intersects processed; 10 tiles in 0.204 S; 49.02 tiles/S; total: 2135 tiles in 673.25 S; size: 987.9MB
Geolevel: 2; zooomlevel: 9; block: 100/157; 19 tile intersects processed; 10 tiles in 0.238 S; 42.02 tiles/S; total: 2145 tiles in 673.753 S; size: 988.91MB
Geolevel: 2; zooomlevel: 9; block: 101/157; 18 tile intersects processed; 10 tiles in 0.269 S; 37.17 tiles/S; total: 2155 tiles in 674.31 S; size: 989.93MB
Geolevel: 2; zooomlevel: 9; block: 102/157; 15 tile intersects processed; 10 tiles in 0.26 S; 38.46 tiles/S; total: 2165 tiles in 674.859 S; size: 991.04MB
Geolevel: 2; zooomlevel: 9; block: 103/157; 23 tile intersects processed; 10 tiles in 0.214 S; 46.73 tiles/S; total: 2175 tiles in 675.37 S; size: 991.81MB
Geolevel: 2; zooomlevel: 9; block: 104/157; 19 tile intersects processed; 10 tiles in 0.342 S; 29.24 tiles/S; total: 2185 tiles in 675.931 S; size: 993.33MB
Geolevel: 2; zooomlevel: 9; block: 105/157; 19 tile intersects processed; 10 tiles in 0.339 S; 29.5 tiles/S; total: 2195 tiles in 676.701 S; size: 994.83MB
Geolevel: 2; zooomlevel: 9; block: 106/157; 17 tile intersects processed; 10 tiles in 0.267 S; 37.45 tiles/S; total: 2205 tiles in 677.353 S; size: 996.1MB
Geolevel: 2; zooomlevel: 9; block: 107/157; 16 tile intersects processed; 10 tiles in 0.216 S; 46.3 tiles/S; total: 2215 tiles in 677.884 S; size: 997.05MB
Geolevel: 2; zooomlevel: 9; block: 108/157; 22 tile intersects processed; 10 tiles in 0.238 S; 42.02 tiles/S; total: 2225 tiles in 678.377 S; size: 998.07MB
Geolevel: 2; zooomlevel: 9; block: 109/157; 16 tile intersects processed; 10 tiles in 0.222 S; 45.05 tiles/S; total: 2235 tiles in 678.907 S; size: 999.08MB
Geolevel: 2; zooomlevel: 9; block: 110/157; 21 tile intersects processed; 10 tiles in 0.179 S; 55.87 tiles/S; total: 2245 tiles in 679.376 S; size: 999.83MB
Geolevel: 2; zooomlevel: 9; block: 111/157; 17 tile intersects processed; 10 tiles in 0.266 S; 37.59 tiles/S; total: 2255 tiles in 679.88 S; size: 1001.05MB
Geolevel: 2; zooomlevel: 9; block: 112/157; 22 tile intersects processed; 10 tiles in 0.179 S; 55.87 tiles/S; total: 2265 tiles in 680.387 S; size: 1001.8MB
Geolevel: 2; zooomlevel: 9; block: 113/157; 18 tile intersects processed; 10 tiles in 0.238 S; 42.02 tiles/S; total: 2275 tiles in 680.856 S; size: 1002.91MB
Geolevel: 2; zooomlevel: 9; block: 114/157; 15 tile intersects processed; 10 tiles in 0.279 S; 35.84 tiles/S; total: 2285 tiles in 681.439 S; size: 1004.16MB
Geolevel: 2; zooomlevel: 9; block: 115/157; 20 tile intersects processed; 10 tiles in 0.262 S; 38.17 tiles/S; total: 2295 tiles in 682.013 S; size: 1005.35MB
Geolevel: 2; zooomlevel: 9; block: 116/157; 13 tile intersects processed; 10 tiles in 0.266 S; 37.59 tiles/S; total: 2305 tiles in 682.605 S; size: 1006.59MB
Geolevel: 2; zooomlevel: 9; block: 117/157; 23 tile intersects processed; 10 tiles in 0.164 S; 60.98 tiles/S; total: 2315 tiles in 683.09 S; size: 1007.34MB
Geolevel: 2; zooomlevel: 9; block: 118/157; 15 tile intersects processed; 10 tiles in 0.305 S; 32.79 tiles/S; total: 2325 tiles in 683.603 S; size: 1008.69MB
Geolevel: 2; zooomlevel: 9; block: 119/157; 17 tile intersects processed; 10 tiles in 0.154 S; 64.94 tiles/S; total: 2335 tiles in 684.11 S; size: 1009.39MB
Geolevel: 2; zooomlevel: 9; block: 120/157; 17 tile intersects processed; 10 tiles in 0.283 S; 35.34 tiles/S; total: 2345 tiles in 684.598 S; size: 1010.66MB
Geolevel: 2; zooomlevel: 9; block: 121/157; 15 tile intersects processed; 10 tiles in 0.255 S; 39.22 tiles/S; total: 2355 tiles in 685.185 S; size: 1011.9MB
Geolevel: 2; zooomlevel: 9; block: 122/157; 10 tile intersects processed; 10 tiles in 0.376 S; 26.6 tiles/S; total: 2365 tiles in 685.881 S; size: 1013.71MB
Geolevel: 2; zooomlevel: 9; block: 123/157; 16 tile intersects processed; 10 tiles in 0.272 S; 36.76 tiles/S; total: 2375 tiles in 686.617 S; size: 1014.93MB
Geolevel: 2; zooomlevel: 9; block: 124/157; 19 tile intersects processed; 10 tiles in 0.236 S; 42.37 tiles/S; total: 2385 tiles in 687.146 S; size: 1015.95MB
Geolevel: 2; zooomlevel: 9; block: 125/157; 15 tile intersects processed; 10 tiles in 0.282 S; 35.46 tiles/S; total: 2395 tiles in 687.708 S; size: 1017.22MB
Geolevel: 2; zooomlevel: 9; block: 126/157; 22 tile intersects processed; 10 tiles in 0.175 S; 57.14 tiles/S; total: 2405 tiles in 688.22 S; size: 1018.07MB
Geolevel: 2; zooomlevel: 9; block: 127/157; 16 tile intersects processed; 10 tiles in 0.286 S; 34.97 tiles/S; total: 2415 tiles in 688.747 S; size: 1019.37MB
Geolevel: 2; zooomlevel: 9; block: 128/157; 20 tile intersects processed; 10 tiles in 0.28 S; 35.71 tiles/S; total: 2425 tiles in 689.375 S; size: 1020.59MB
Geolevel: 2; zooomlevel: 9; block: 129/157; 14 tile intersects processed; 10 tiles in 0.339 S; 29.5 tiles/S; total: 2435 tiles in 690.037 S; size: 1022.15MB
Geolevel: 2; zooomlevel: 9; block: 130/157; 16 tile intersects processed; 10 tiles in 0.285 S; 35.09 tiles/S; total: 2445 tiles in 690.728 S; size: 1023.43MB
Geolevel: 2; zooomlevel: 9; block: 131/157; 20 tile intersects processed; 10 tiles in 0.239 S; 41.84 tiles/S; total: 2455 tiles in 691.3 S; size: 1GB
Geolevel: 2; zooomlevel: 9; block: 132/157; 13 tile intersects processed; 10 tiles in 0.21 S; 47.62 tiles/S; total: 2465 tiles in 691.829 S; size: 1GB
Geolevel: 2; zooomlevel: 9; block: 133/157; 21 tile intersects processed; 10 tiles in 0.206 S; 48.54 tiles/S; total: 2475 tiles in 692.287 S; size: 1GB
Geolevel: 2; zooomlevel: 9; block: 134/157; 15 tile intersects processed; 10 tiles in 0.226 S; 44.25 tiles/S; total: 2485 tiles in 692.783 S; size: 1GB
Geolevel: 2; zooomlevel: 9; block: 135/157; 15 tile intersects processed; 10 tiles in 0.199 S; 50.25 tiles/S; total: 2495 tiles in 693.266 S; size: 1GB
Geolevel: 2; zooomlevel: 9; block: 136/157; 20 tile intersects processed; 10 tiles in 0.199 S; 50.25 tiles/S; total: 2505 tiles in 693.727 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 137/157; 14 tile intersects processed; 10 tiles in 0.209 S; 47.85 tiles/S; total: 2515 tiles in 694.187 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 138/157; 19 tile intersects processed; 10 tiles in 0.243 S; 41.15 tiles/S; total: 2525 tiles in 694.692 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 139/157; 15 tile intersects processed; 10 tiles in 0.227 S; 44.05 tiles/S; total: 2535 tiles in 695.21 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 140/157; 19 tile intersects processed; 10 tiles in 0.227 S; 44.05 tiles/S; total: 2545 tiles in 695.715 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 141/157; 18 tile intersects processed; 10 tiles in 0.242 S; 41.32 tiles/S; total: 2555 tiles in 696.257 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 142/157; 18 tile intersects processed; 10 tiles in 0.255 S; 39.22 tiles/S; total: 2565 tiles in 696.798 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 143/157; 13 tile intersects processed; 10 tiles in 0.189 S; 52.91 tiles/S; total: 2575 tiles in 697.297 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 144/157; 17 tile intersects processed; 10 tiles in 0.19 S; 52.63 tiles/S; total: 2585 tiles in 697.727 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 145/157; 16 tile intersects processed; 10 tiles in 0.203 S; 49.26 tiles/S; total: 2595 tiles in 698.182 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 146/157; 17 tile intersects processed; 10 tiles in 0.124 S; 80.65 tiles/S; total: 2605 tiles in 698.57 S; size: 1.01GB
Geolevel: 2; zooomlevel: 9; block: 147/157; 20 tile intersects processed; 10 tiles in 0.141 S; 70.92 tiles/S; total: 2615 tiles in 698.877 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 148/157; 17 tile intersects processed; 10 tiles in 0.093 S; 107.53 tiles/S; total: 2625 tiles in 699.167 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 149/157; 23 tile intersects processed; 10 tiles in 0.171 S; 58.48 tiles/S; total: 2635 tiles in 699.481 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 150/157; 18 tile intersects processed; 10 tiles in 0.196 S; 51.02 tiles/S; total: 2645 tiles in 699.909 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 151/157; 10 tile intersects processed; 10 tiles in 0.142 S; 70.42 tiles/S; total: 2655 tiles in 700.308 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 152/157; 10 tile intersects processed; 10 tiles in 0.183 S; 54.64 tiles/S; total: 2665 tiles in 700.7 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 153/157; 10 tile intersects processed; 10 tiles in 0.198 S; 50.51 tiles/S; total: 2675 tiles in 701.142 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 154/157; 11 tile intersects processed; 10 tiles in 0.073 S; 136.99 tiles/S; total: 2685 tiles in 701.484 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 155/157; 10 tile intersects processed; 10 tiles in 0.046 S; 217.39 tiles/S; total: 2695 tiles in 701.65 S; size: 1.02GB
Geolevel: 2; zooomlevel: 9; block: 156/157; 10 tile intersects processed; 10 tiles in 1.949 S; 5.13 tiles/S; total: 2705 tiles in 703.662 S; size: 1.03GB
Geolevel: 2; zooomlevel: 9; block: 157/157; 9 tile intersects processed; 9 tiles in 2.909 S; 3.09 tiles/S; total: 2714 tiles in 708.99 S; size: 1.04GB
Creating tile CSV file: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/mssql_t_tiles_cb_2014_us_county_500k.csv
Geolevel: 3; zooomlevel: 0; block: 1/1; 3233 tile intersects processed; 1 tiles in 1.8 S; 0.56 tiles/S; total: 2715 tiles in 714.332 S; size: 1.04GB
Geolevel: 3; zooomlevel: 1; block: 1/1; 3234 tile intersects processed; 3 tiles in 1.697 S; 1.77 tiles/S; total: 2718 tiles in 718.45 S; size: 1.05GB
Geolevel: 3; zooomlevel: 2; block: 1/1; 3292 tile intersects processed; 5 tiles in 1.755 S; 2.85 tiles/S; total: 2723 tiles in 722.698 S; size: 1.05GB
Geolevel: 3; zooomlevel: 3; block: 1/2; 3386 tile intersects processed; 9 tiles in 1.898 S; 4.74 tiles/S; total: 2732 tiles in 727.098 S; size: 1.06GB
Geolevel: 3; zooomlevel: 3; block: 2/2; 5 tile intersects processed; 1 tiles in 0.005 S; 200 tiles/S; total: 2733 tiles in 729.696 S; size: 1.06GB
Geolevel: 3; zooomlevel: 4; block: 1/3; 49 tile intersects processed; 9 tiles in 0.201 S; 44.78 tiles/S; total: 2742 tiles in 729.906 S; size: 1.06GB
Geolevel: 3; zooomlevel: 4; block: 2/3; 3305 tile intersects processed; 10 tiles in 1.733 S; 5.77 tiles/S; total: 2752 tiles in 731.9 S; size: 1.06GB
Geolevel: 3; zooomlevel: 4; block: 3/3; 87 tile intersects processed; 3 tiles in 0.048 S; 62.5 tiles/S; total: 2755 tiles in 734.419 S; size: 1.06GB
Geolevel: 3; zooomlevel: 5; block: 1/5; 18 tile intersects processed; 9 tiles in 0.079 S; 113.92 tiles/S; total: 2764 tiles in 734.591 S; size: 1.06GB
Geolevel: 3; zooomlevel: 5; block: 2/5; 36 tile intersects processed; 10 tiles in 0.159 S; 62.89 tiles/S; total: 2774 tiles in 734.855 S; size: 1.06GB
Geolevel: 3; zooomlevel: 5; block: 3/5; 200 tile intersects processed; 10 tiles in 0.288 S; 34.72 tiles/S; total: 2784 tiles in 735.365 S; size: 1.07GB
Geolevel: 3; zooomlevel: 5; block: 4/5; 1643 tile intersects processed; 10 tiles in 0.766 S; 13.05 tiles/S; total: 2794 tiles in 736.517 S; size: 1.07GB
Geolevel: 3; zooomlevel: 5; block: 5/5; 1762 tile intersects processed; 10 tiles in 0.955 S; 10.47 tiles/S; total: 2804 tiles in 738.587 S; size: 1.07GB
Geolevel: 3; zooomlevel: 6; block: 1/12; 9 tile intersects processed; 9 tiles in 0.113 S; 79.65 tiles/S; total: 2813 tiles in 740.051 S; size: 1.07GB
Geolevel: 3; zooomlevel: 6; block: 2/12; 18 tile intersects processed; 10 tiles in 0.081 S; 123.46 tiles/S; total: 2823 tiles in 740.252 S; size: 1.07GB
Geolevel: 3; zooomlevel: 6; block: 3/12; 25 tile intersects processed; 10 tiles in 0.126 S; 79.37 tiles/S; total: 2833 tiles in 740.484 S; size: 1.07GB
Geolevel: 3; zooomlevel: 6; block: 4/12; 28 tile intersects processed; 10 tiles in 0.119 S; 84.03 tiles/S; total: 2843 tiles in 740.747 S; size: 1.07GB
Geolevel: 3; zooomlevel: 6; block: 5/12; 26 tile intersects processed; 10 tiles in 0.129 S; 77.52 tiles/S; total: 2853 tiles in 741.032 S; size: 1.07GB
Geolevel: 3; zooomlevel: 6; block: 6/12; 97 tile intersects processed; 10 tiles in 0.171 S; 58.48 tiles/S; total: 2863 tiles in 741.363 S; size: 1.07GB
Geolevel: 3; zooomlevel: 6; block: 7/12; 247 tile intersects processed; 10 tiles in 0.23 S; 43.48 tiles/S; total: 2873 tiles in 741.823 S; size: 1.08GB
Geolevel: 3; zooomlevel: 6; block: 8/12; 413 tile intersects processed; 10 tiles in 0.224 S; 44.64 tiles/S; total: 2883 tiles in 742.324 S; size: 1.08GB
Geolevel: 3; zooomlevel: 6; block: 9/12; 687 tile intersects processed; 10 tiles in 0.271 S; 36.9 tiles/S; total: 2893 tiles in 742.906 S; size: 1.08GB
Geolevel: 3; zooomlevel: 6; block: 10/12; 1345 tile intersects processed; 10 tiles in 0.58 S; 17.24 tiles/S; total: 2903 tiles in 743.882 S; size: 1.08GB
Geolevel: 3; zooomlevel: 6; block: 11/12; 1080 tile intersects processed; 10 tiles in 0.612 S; 16.34 tiles/S; total: 2913 tiles in 745.387 S; size: 1.08GB
Geolevel: 3; zooomlevel: 6; block: 12/12; 94 tile intersects processed; 10 tiles in 0.084 S; 119.05 tiles/S; total: 2923 tiles in 746.319 S; size: 1.08GB
Geolevel: 3; zooomlevel: 7; block: 1/34; 9 tile intersects processed; 9 tiles in 0.105 S; 85.71 tiles/S; total: 2932 tiles in 746.551 S; size: 1.08GB
Geolevel: 3; zooomlevel: 7; block: 2/34; 12 tile intersects processed; 10 tiles in 0.114 S; 87.72 tiles/S; total: 2942 tiles in 746.791 S; size: 1.08GB
Geolevel: 3; zooomlevel: 7; block: 3/34; 12 tile intersects processed; 10 tiles in 0.094 S; 106.38 tiles/S; total: 2952 tiles in 747.035 S; size: 1.08GB
Geolevel: 3; zooomlevel: 7; block: 4/34; 14 tile intersects processed; 10 tiles in 0.086 S; 116.28 tiles/S; total: 2962 tiles in 747.252 S; size: 1.08GB
Geolevel: 3; zooomlevel: 7; block: 5/34; 18 tile intersects processed; 10 tiles in 0.114 S; 87.72 tiles/S; total: 2972 tiles in 747.493 S; size: 1.08GB
Geolevel: 3; zooomlevel: 7; block: 6/34; 15 tile intersects processed; 10 tiles in 0.096 S; 104.17 tiles/S; total: 2982 tiles in 747.738 S; size: 1.08GB
Geolevel: 3; zooomlevel: 7; block: 7/34; 20 tile intersects processed; 10 tiles in 0.107 S; 93.46 tiles/S; total: 2992 tiles in 747.98 S; size: 1.08GB
Geolevel: 3; zooomlevel: 7; block: 8/34; 27 tile intersects processed; 10 tiles in 0.166 S; 60.24 tiles/S; total: 3002 tiles in 748.303 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 9/34; 23 tile intersects processed; 10 tiles in 0.168 S; 59.52 tiles/S; total: 3012 tiles in 748.7 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 10/34; 17 tile intersects processed; 10 tiles in 0.107 S; 93.46 tiles/S; total: 3022 tiles in 749.081 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 11/34; 25 tile intersects processed; 10 tiles in 0.152 S; 65.79 tiles/S; total: 3032 tiles in 749.371 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 12/34; 18 tile intersects processed; 10 tiles in 0.127 S; 78.74 tiles/S; total: 3042 tiles in 749.706 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 13/34; 22 tile intersects processed; 10 tiles in 0.149 S; 67.11 tiles/S; total: 3052 tiles in 750.006 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 14/34; 43 tile intersects processed; 10 tiles in 0.209 S; 47.85 tiles/S; total: 3062 tiles in 750.41 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 15/34; 123 tile intersects processed; 10 tiles in 0.273 S; 36.63 tiles/S; total: 3072 tiles in 750.939 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 16/34; 121 tile intersects processed; 10 tiles in 0.121 S; 82.64 tiles/S; total: 3082 tiles in 751.253 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 17/34; 99 tile intersects processed; 10 tiles in 0.122 S; 81.97 tiles/S; total: 3092 tiles in 751.544 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 18/34; 102 tile intersects processed; 10 tiles in 0.106 S; 94.34 tiles/S; total: 3102 tiles in 751.8 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 19/34; 112 tile intersects processed; 10 tiles in 0.112 S; 89.29 tiles/S; total: 3112 tiles in 752.064 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 20/34; 99 tile intersects processed; 10 tiles in 0.084 S; 119.05 tiles/S; total: 3122 tiles in 752.3 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 21/34; 128 tile intersects processed; 10 tiles in 0.085 S; 117.65 tiles/S; total: 3132 tiles in 752.497 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 22/34; 207 tile intersects processed; 10 tiles in 0.091 S; 109.89 tiles/S; total: 3142 tiles in 752.707 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 23/34; 310 tile intersects processed; 10 tiles in 0.118 S; 84.75 tiles/S; total: 3152 tiles in 752.971 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 24/34; 323 tile intersects processed; 10 tiles in 0.151 S; 66.23 tiles/S; total: 3162 tiles in 753.407 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 25/34; 341 tile intersects processed; 10 tiles in 0.168 S; 59.52 tiles/S; total: 3172 tiles in 753.795 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 26/34; 368 tile intersects processed; 10 tiles in 0.207 S; 48.31 tiles/S; total: 3182 tiles in 754.25 S; size: 1.09GB
Geolevel: 3; zooomlevel: 7; block: 27/34; 399 tile intersects processed; 10 tiles in 0.231 S; 43.29 tiles/S; total: 3192 tiles in 754.804 S; size: 1.1GB
Geolevel: 3; zooomlevel: 7; block: 28/34; 482 tile intersects processed; 10 tiles in 0.247 S; 40.49 tiles/S; total: 3202 tiles in 755.402 S; size: 1.1GB
Geolevel: 3; zooomlevel: 7; block: 29/34; 577 tile intersects processed; 10 tiles in 0.337 S; 29.67 tiles/S; total: 3212 tiles in 756.118 S; size: 1.1GB
Geolevel: 3; zooomlevel: 7; block: 30/34; 312 tile intersects processed; 10 tiles in 0.213 S; 46.95 tiles/S; total: 3222 tiles in 756.844 S; size: 1.1GB
Geolevel: 3; zooomlevel: 7; block: 31/34; 359 tile intersects processed; 10 tiles in 0.248 S; 40.32 tiles/S; total: 3232 tiles in 757.41 S; size: 1.1GB
Geolevel: 3; zooomlevel: 7; block: 32/34; 154 tile intersects processed; 10 tiles in 0.137 S; 72.99 tiles/S; total: 3242 tiles in 757.926 S; size: 1.1GB
Geolevel: 3; zooomlevel: 7; block: 33/34; 95 tile intersects processed; 10 tiles in 0.051 S; 196.08 tiles/S; total: 3252 tiles in 758.173 S; size: 1.1GB
Geolevel: 3; zooomlevel: 7; block: 34/34; 4 tile intersects processed; 4 tiles in 0.08 S; 50 tiles/S; total: 3256 tiles in 758.353 S; size: 1.1GB
Geolevel: 3; zooomlevel: 8; block: 1/100; 9 tile intersects processed; 9 tiles in 0.212 S; 42.45 tiles/S; total: 3265 tiles in 758.691 S; size: 1.1GB
Geolevel: 3; zooomlevel: 8; block: 2/100; 10 tile intersects processed; 10 tiles in 0.105 S; 95.24 tiles/S; total: 3275 tiles in 759.109 S; size: 1.1GB
Geolevel: 3; zooomlevel: 8; block: 3/100; 11 tile intersects processed; 10 tiles in 0.227 S; 44.05 tiles/S; total: 3285 tiles in 759.498 S; size: 1.1GB
Geolevel: 3; zooomlevel: 8; block: 4/100; 10 tile intersects processed; 10 tiles in 0.157 S; 63.69 tiles/S; total: 3295 tiles in 759.94 S; size: 1.1GB
Geolevel: 3; zooomlevel: 8; block: 5/100; 10 tile intersects processed; 10 tiles in 0.079 S; 126.58 tiles/S; total: 3305 tiles in 760.284 S; size: 1.1GB
Geolevel: 3; zooomlevel: 8; block: 6/100; 12 tile intersects processed; 10 tiles in 0.148 S; 67.57 tiles/S; total: 3315 tiles in 760.554 S; size: 1.1GB
Geolevel: 3; zooomlevel: 8; block: 7/100; 12 tile intersects processed; 10 tiles in 0.11 S; 90.91 tiles/S; total: 3325 tiles in 760.858 S; size: 1.1GB
Geolevel: 3; zooomlevel: 8; block: 8/100; 16 tile intersects processed; 10 tiles in 0.114 S; 87.72 tiles/S; total: 3335 tiles in 761.118 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 9/100; 12 tile intersects processed; 10 tiles in 0.12 S; 83.33 tiles/S; total: 3345 tiles in 761.408 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 10/100; 12 tile intersects processed; 10 tiles in 0.144 S; 69.44 tiles/S; total: 3355 tiles in 761.697 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 11/100; 16 tile intersects processed; 10 tiles in 0.127 S; 78.74 tiles/S; total: 3365 tiles in 761.992 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 12/100; 16 tile intersects processed; 10 tiles in 0.131 S; 76.34 tiles/S; total: 3375 tiles in 762.279 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 13/100; 21 tile intersects processed; 10 tiles in 0.138 S; 72.46 tiles/S; total: 3385 tiles in 762.587 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 14/100; 15 tile intersects processed; 10 tiles in 0.168 S; 59.52 tiles/S; total: 3395 tiles in 762.952 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 15/100; 16 tile intersects processed; 10 tiles in 0.124 S; 80.65 tiles/S; total: 3405 tiles in 763.303 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 16/100; 21 tile intersects processed; 10 tiles in 0.126 S; 79.37 tiles/S; total: 3415 tiles in 763.593 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 17/100; 18 tile intersects processed; 10 tiles in 0.15 S; 66.67 tiles/S; total: 3425 tiles in 763.915 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 18/100; 19 tile intersects processed; 10 tiles in 0.224 S; 44.64 tiles/S; total: 3435 tiles in 764.345 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 19/100; 17 tile intersects processed; 10 tiles in 0.131 S; 76.34 tiles/S; total: 3445 tiles in 764.752 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 20/100; 17 tile intersects processed; 10 tiles in 0.216 S; 46.3 tiles/S; total: 3455 tiles in 765.126 S; size: 1.11GB
Geolevel: 3; zooomlevel: 8; block: 21/100; 19 tile intersects processed; 10 tiles in 0.214 S; 46.73 tiles/S; total: 3465 tiles in 765.603 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 22/100; 16 tile intersects processed; 10 tiles in 0.116 S; 86.21 tiles/S; total: 3475 tiles in 765.977 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 23/100; 16 tile intersects processed; 10 tiles in 0.15 S; 66.67 tiles/S; total: 3485 tiles in 766.276 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 24/100; 22 tile intersects processed; 10 tiles in 0.175 S; 57.14 tiles/S; total: 3495 tiles in 766.65 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 25/100; 20 tile intersects processed; 10 tiles in 0.21 S; 47.62 tiles/S; total: 3505 tiles in 767.115 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 26/100; 16 tile intersects processed; 10 tiles in 0.161 S; 62.11 tiles/S; total: 3515 tiles in 767.515 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 27/100; 19 tile intersects processed; 10 tiles in 0.174 S; 57.47 tiles/S; total: 3525 tiles in 767.898 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 28/100; 16 tile intersects processed; 10 tiles in 0.161 S; 62.11 tiles/S; total: 3535 tiles in 768.286 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 29/100; 12 tile intersects processed; 10 tiles in 0.076 S; 131.58 tiles/S; total: 3545 tiles in 768.579 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 30/100; 13 tile intersects processed; 10 tiles in 0.127 S; 78.74 tiles/S; total: 3555 tiles in 768.831 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 31/100; 21 tile intersects processed; 10 tiles in 0.186 S; 53.76 tiles/S; total: 3565 tiles in 769.171 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 32/100; 26 tile intersects processed; 10 tiles in 0.285 S; 35.09 tiles/S; total: 3575 tiles in 769.696 S; size: 1.12GB
Geolevel: 3; zooomlevel: 8; block: 33/100; 19 tile intersects processed; 10 tiles in 0.261 S; 38.31 tiles/S; total: 3585 tiles in 770.32 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 34/100; 29 tile intersects processed; 10 tiles in 0.036 S; 277.78 tiles/S; total: 3595 tiles in 770.676 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 35/100; 72 tile intersects processed; 10 tiles in 0.108 S; 92.59 tiles/S; total: 3605 tiles in 770.852 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 36/100; 56 tile intersects processed; 10 tiles in 0.089 S; 112.36 tiles/S; total: 3615 tiles in 771.093 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 37/100; 66 tile intersects processed; 10 tiles in 0.129 S; 77.52 tiles/S; total: 3625 tiles in 771.336 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 38/100; 64 tile intersects processed; 10 tiles in 0.096 S; 104.17 tiles/S; total: 3635 tiles in 771.587 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 39/100; 47 tile intersects processed; 10 tiles in 0.062 S; 161.29 tiles/S; total: 3645 tiles in 771.766 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 40/100; 48 tile intersects processed; 10 tiles in 0.071 S; 140.85 tiles/S; total: 3655 tiles in 771.945 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 41/100; 59 tile intersects processed; 10 tiles in 0.052 S; 192.31 tiles/S; total: 3665 tiles in 772.098 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 42/100; 39 tile intersects processed; 10 tiles in 0.045 S; 222.22 tiles/S; total: 3675 tiles in 772.232 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 43/100; 46 tile intersects processed; 10 tiles in 0.06 S; 166.67 tiles/S; total: 3685 tiles in 772.36 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 44/100; 48 tile intersects processed; 10 tiles in 0.128 S; 78.13 tiles/S; total: 3695 tiles in 772.575 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 45/100; 48 tile intersects processed; 10 tiles in 0.079 S; 126.58 tiles/S; total: 3705 tiles in 772.87 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 46/100; 57 tile intersects processed; 10 tiles in 0.097 S; 103.09 tiles/S; total: 3715 tiles in 773.056 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 47/100; 43 tile intersects processed; 10 tiles in 0.068 S; 147.06 tiles/S; total: 3725 tiles in 773.252 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 48/100; 73 tile intersects processed; 10 tiles in 0.129 S; 77.52 tiles/S; total: 3735 tiles in 773.484 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 49/100; 42 tile intersects processed; 10 tiles in 0.058 S; 172.41 tiles/S; total: 3745 tiles in 773.681 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 50/100; 59 tile intersects processed; 10 tiles in 0.117 S; 85.47 tiles/S; total: 3755 tiles in 773.871 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 51/100; 45 tile intersects processed; 10 tiles in 0.053 S; 188.68 tiles/S; total: 3765 tiles in 774.06 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 52/100; 46 tile intersects processed; 10 tiles in 0.042 S; 238.1 tiles/S; total: 3775 tiles in 774.175 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 53/100; 45 tile intersects processed; 10 tiles in 0.047 S; 212.77 tiles/S; total: 3785 tiles in 774.299 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 54/100; 55 tile intersects processed; 10 tiles in 0.047 S; 212.77 tiles/S; total: 3795 tiles in 774.421 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 55/100; 58 tile intersects processed; 10 tiles in 0.063 S; 158.73 tiles/S; total: 3805 tiles in 774.569 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 56/100; 54 tile intersects processed; 10 tiles in 0.042 S; 238.1 tiles/S; total: 3815 tiles in 774.702 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 57/100; 66 tile intersects processed; 10 tiles in 0.05 S; 200 tiles/S; total: 3825 tiles in 774.812 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 58/100; 55 tile intersects processed; 10 tiles in 0.038 S; 263.16 tiles/S; total: 3835 tiles in 774.924 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 59/100; 85 tile intersects processed; 10 tiles in 0.041 S; 243.9 tiles/S; total: 3845 tiles in 775.039 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 60/100; 69 tile intersects processed; 10 tiles in 0.054 S; 185.19 tiles/S; total: 3855 tiles in 775.164 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 61/100; 88 tile intersects processed; 10 tiles in 0.041 S; 243.9 tiles/S; total: 3865 tiles in 775.28 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 62/100; 84 tile intersects processed; 10 tiles in 0.048 S; 208.33 tiles/S; total: 3875 tiles in 775.391 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 63/100; 103 tile intersects processed; 10 tiles in 0.064 S; 156.25 tiles/S; total: 3885 tiles in 775.527 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 64/100; 95 tile intersects processed; 10 tiles in 0.058 S; 172.41 tiles/S; total: 3895 tiles in 775.669 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 65/100; 117 tile intersects processed; 10 tiles in 0.048 S; 208.33 tiles/S; total: 3905 tiles in 775.789 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 66/100; 108 tile intersects processed; 10 tiles in 0.056 S; 178.57 tiles/S; total: 3915 tiles in 775.923 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 67/100; 120 tile intersects processed; 10 tiles in 0.054 S; 185.19 tiles/S; total: 3925 tiles in 776.062 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 68/100; 142 tile intersects processed; 10 tiles in 0.078 S; 128.21 tiles/S; total: 3935 tiles in 776.235 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 69/100; 112 tile intersects processed; 10 tiles in 0.066 S; 151.52 tiles/S; total: 3945 tiles in 776.427 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 70/100; 155 tile intersects processed; 10 tiles in 0.118 S; 84.75 tiles/S; total: 3955 tiles in 776.639 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 71/100; 100 tile intersects processed; 10 tiles in 0.065 S; 153.85 tiles/S; total: 3965 tiles in 776.856 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 72/100; 167 tile intersects processed; 10 tiles in 0.132 S; 75.76 tiles/S; total: 3975 tiles in 777.079 S; size: 1.13GB
Geolevel: 3; zooomlevel: 8; block: 73/100; 114 tile intersects processed; 10 tiles in 0.075 S; 133.33 tiles/S; total: 3985 tiles in 777.326 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 74/100; 175 tile intersects processed; 10 tiles in 0.144 S; 69.44 tiles/S; total: 3995 tiles in 777.592 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 75/100; 125 tile intersects processed; 10 tiles in 0.063 S; 158.73 tiles/S; total: 4005 tiles in 777.84 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 76/100; 140 tile intersects processed; 10 tiles in 0.122 S; 81.97 tiles/S; total: 4015 tiles in 778.067 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 77/100; 130 tile intersects processed; 10 tiles in 0.073 S; 136.99 tiles/S; total: 4025 tiles in 778.314 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 78/100; 142 tile intersects processed; 10 tiles in 0.139 S; 71.94 tiles/S; total: 4035 tiles in 778.585 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 79/100; 154 tile intersects processed; 10 tiles in 0.079 S; 126.58 tiles/S; total: 4045 tiles in 778.844 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 80/100; 122 tile intersects processed; 10 tiles in 0.079 S; 126.58 tiles/S; total: 4055 tiles in 779.051 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 81/100; 165 tile intersects processed; 10 tiles in 0.097 S; 103.09 tiles/S; total: 4065 tiles in 779.272 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 82/100; 110 tile intersects processed; 10 tiles in 0.087 S; 114.94 tiles/S; total: 4075 tiles in 779.493 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 83/100; 206 tile intersects processed; 10 tiles in 0.137 S; 72.99 tiles/S; total: 4085 tiles in 779.766 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 84/100; 164 tile intersects processed; 10 tiles in 0.095 S; 105.26 tiles/S; total: 4095 tiles in 780.073 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 85/100; 193 tile intersects processed; 10 tiles in 0.14 S; 71.43 tiles/S; total: 4105 tiles in 780.352 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 86/100; 241 tile intersects processed; 10 tiles in 0.193 S; 51.81 tiles/S; total: 4115 tiles in 780.762 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 87/100; 162 tile intersects processed; 10 tiles in 0.121 S; 82.64 tiles/S; total: 4125 tiles in 781.155 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 88/100; 204 tile intersects processed; 10 tiles in 0.199 S; 50.25 tiles/S; total: 4135 tiles in 781.53 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 89/100; 153 tile intersects processed; 10 tiles in 0.122 S; 81.97 tiles/S; total: 4145 tiles in 781.917 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 90/100; 89 tile intersects processed; 10 tiles in 0.105 S; 95.24 tiles/S; total: 4155 tiles in 782.213 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 91/100; 162 tile intersects processed; 10 tiles in 0.152 S; 65.79 tiles/S; total: 4165 tiles in 782.521 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 92/100; 156 tile intersects processed; 10 tiles in 0.144 S; 69.44 tiles/S; total: 4175 tiles in 782.866 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 93/100; 163 tile intersects processed; 10 tiles in 0.161 S; 62.11 tiles/S; total: 4185 tiles in 783.218 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 94/100; 130 tile intersects processed; 10 tiles in 0.122 S; 81.97 tiles/S; total: 4195 tiles in 783.55 S; size: 1.14GB
Geolevel: 3; zooomlevel: 8; block: 95/100; 103 tile intersects processed; 10 tiles in 0.079 S; 126.58 tiles/S; total: 4205 tiles in 783.806 S; size: 1.15GB
Geolevel: 3; zooomlevel: 8; block: 96/100; 78 tile intersects processed; 10 tiles in 0.079 S; 126.58 tiles/S; total: 4215 tiles in 784.008 S; size: 1.15GB
Geolevel: 3; zooomlevel: 8; block: 97/100; 38 tile intersects processed; 10 tiles in 0.071 S; 140.85 tiles/S; total: 4225 tiles in 784.189 S; size: 1.15GB
Geolevel: 3; zooomlevel: 8; block: 98/100; 108 tile intersects processed; 10 tiles in 0.072 S; 138.89 tiles/S; total: 4235 tiles in 784.357 S; size: 1.15GB
Geolevel: 3; zooomlevel: 8; block: 99/100; 13 tile intersects processed; 10 tiles in 0.114 S; 87.72 tiles/S; total: 4245 tiles in 784.58 S; size: 1.15GB
Geolevel: 3; zooomlevel: 8; block: 100/100; 3 tile intersects processed; 3 tiles in 0.087 S; 34.48 tiles/S; total: 4248 tiles in 784.821 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 1/314; 9 tile intersects processed; 9 tiles in 0.341 S; 26.39 tiles/S; total: 4257 tiles in 785.286 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 2/314; 10 tile intersects processed; 10 tiles in 0.291 S; 34.36 tiles/S; total: 4267 tiles in 785.975 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 3/314; 10 tile intersects processed; 10 tiles in 0.211 S; 47.39 tiles/S; total: 4277 tiles in 786.553 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 4/314; 10 tile intersects processed; 10 tiles in 0.203 S; 49.26 tiles/S; total: 4287 tiles in 787.024 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 5/314; 11 tile intersects processed; 10 tiles in 0.292 S; 34.25 tiles/S; total: 4297 tiles in 787.583 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 6/314; 10 tile intersects processed; 10 tiles in 0.187 S; 53.48 tiles/S; total: 4307 tiles in 788.123 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 7/314; 10 tile intersects processed; 10 tiles in 0.241 S; 41.49 tiles/S; total: 4317 tiles in 788.6 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 8/314; 10 tile intersects processed; 10 tiles in 0.167 S; 59.88 tiles/S; total: 4327 tiles in 789.067 S; size: 1.15GB
Geolevel: 3; zooomlevel: 9; block: 9/314; 10 tile intersects processed; 10 tiles in 0.213 S; 46.95 tiles/S; total: 4337 tiles in 789.502 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 10/314; 11 tile intersects processed; 10 tiles in 0.212 S; 47.17 tiles/S; total: 4347 tiles in 789.983 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 11/314; 11 tile intersects processed; 10 tiles in 0.117 S; 85.47 tiles/S; total: 4357 tiles in 790.381 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 12/314; 11 tile intersects processed; 10 tiles in 0.178 S; 56.18 tiles/S; total: 4367 tiles in 790.724 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 13/314; 12 tile intersects processed; 10 tiles in 0.139 S; 71.94 tiles/S; total: 4377 tiles in 791.1 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 14/314; 13 tile intersects processed; 10 tiles in 0.208 S; 48.08 tiles/S; total: 4387 tiles in 791.475 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 15/314; 16 tile intersects processed; 10 tiles in 0.164 S; 60.98 tiles/S; total: 4397 tiles in 791.904 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 16/314; 11 tile intersects processed; 10 tiles in 0.225 S; 44.44 tiles/S; total: 4407 tiles in 792.319 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 17/314; 12 tile intersects processed; 10 tiles in 0.124 S; 80.65 tiles/S; total: 4417 tiles in 792.714 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 18/314; 11 tile intersects processed; 10 tiles in 0.209 S; 47.85 tiles/S; total: 4427 tiles in 793.085 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 19/314; 13 tile intersects processed; 10 tiles in 0.158 S; 63.29 tiles/S; total: 4437 tiles in 793.5 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 20/314; 11 tile intersects processed; 10 tiles in 0.198 S; 50.51 tiles/S; total: 4447 tiles in 793.9 S; size: 1.16GB
Geolevel: 3; zooomlevel: 9; block: 21/314; 14 tile intersects processed; 10 tiles in 0.167 S; 59.88 tiles/S; total: 4457 tiles in 794.328 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 22/314; 11 tile intersects processed; 10 tiles in 0.216 S; 46.3 tiles/S; total: 4467 tiles in 794.764 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 23/314; 11 tile intersects processed; 10 tiles in 0.15 S; 66.67 tiles/S; total: 4477 tiles in 795.201 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 24/314; 17 tile intersects processed; 10 tiles in 0.194 S; 51.55 tiles/S; total: 4487 tiles in 795.597 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 25/314; 12 tile intersects processed; 10 tiles in 0.181 S; 55.25 tiles/S; total: 4497 tiles in 796.025 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 26/314; 21 tile intersects processed; 10 tiles in 0.183 S; 54.64 tiles/S; total: 4507 tiles in 796.456 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 27/314; 12 tile intersects processed; 10 tiles in 0.213 S; 46.95 tiles/S; total: 4517 tiles in 796.909 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 28/314; 20 tile intersects processed; 10 tiles in 0.213 S; 46.95 tiles/S; total: 4527 tiles in 797.407 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 29/314; 13 tile intersects processed; 10 tiles in 0.253 S; 39.53 tiles/S; total: 4537 tiles in 797.928 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 30/314; 18 tile intersects processed; 10 tiles in 0.185 S; 54.05 tiles/S; total: 4547 tiles in 798.416 S; size: 1.17GB
Geolevel: 3; zooomlevel: 9; block: 31/314; 16 tile intersects processed; 10 tiles in 0.257 S; 38.91 tiles/S; total: 4557 tiles in 798.913 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 32/314; 14 tile intersects processed; 10 tiles in 0.153 S; 65.36 tiles/S; total: 4567 tiles in 799.378 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 33/314; 11 tile intersects processed; 10 tiles in 0.136 S; 73.53 tiles/S; total: 4577 tiles in 799.718 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 34/314; 17 tile intersects processed; 10 tiles in 0.153 S; 65.36 tiles/S; total: 4587 tiles in 800.06 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 35/314; 13 tile intersects processed; 10 tiles in 0.15 S; 66.67 tiles/S; total: 4597 tiles in 800.424 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 36/314; 19 tile intersects processed; 10 tiles in 0.189 S; 52.91 tiles/S; total: 4607 tiles in 800.815 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 37/314; 17 tile intersects processed; 10 tiles in 0.18 S; 55.56 tiles/S; total: 4617 tiles in 801.231 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 38/314; 14 tile intersects processed; 10 tiles in 0.198 S; 50.51 tiles/S; total: 4627 tiles in 801.662 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 39/314; 18 tile intersects processed; 10 tiles in 0.208 S; 48.08 tiles/S; total: 4637 tiles in 802.126 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 40/314; 13 tile intersects processed; 10 tiles in 0.184 S; 54.35 tiles/S; total: 4647 tiles in 802.56 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 41/314; 20 tile intersects processed; 10 tiles in 0.222 S; 45.05 tiles/S; total: 4657 tiles in 803.011 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 42/314; 13 tile intersects processed; 10 tiles in 0.22 S; 45.45 tiles/S; total: 4667 tiles in 803.524 S; size: 1.18GB
Geolevel: 3; zooomlevel: 9; block: 43/314; 18 tile intersects processed; 10 tiles in 0.205 S; 48.78 tiles/S; total: 4677 tiles in 804 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 44/314; 14 tile intersects processed; 10 tiles in 0.305 S; 32.79 tiles/S; total: 4687 tiles in 804.571 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 45/314; 21 tile intersects processed; 10 tiles in 0.4 S; 25 tiles/S; total: 4697 tiles in 805.322 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 46/314; 13 tile intersects processed; 10 tiles in 0.199 S; 50.25 tiles/S; total: 4707 tiles in 806.021 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 47/314; 21 tile intersects processed; 10 tiles in 0.23 S; 43.48 tiles/S; total: 4717 tiles in 806.489 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 48/314; 12 tile intersects processed; 10 tiles in 0.225 S; 44.44 tiles/S; total: 4727 tiles in 806.982 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 49/314; 16 tile intersects processed; 10 tiles in 0.204 S; 49.02 tiles/S; total: 4737 tiles in 807.45 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 50/314; 14 tile intersects processed; 10 tiles in 0.188 S; 53.19 tiles/S; total: 4747 tiles in 807.894 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 51/314; 12 tile intersects processed; 10 tiles in 0.258 S; 38.76 tiles/S; total: 4757 tiles in 808.399 S; size: 1.19GB
Geolevel: 3; zooomlevel: 9; block: 52/314; 15 tile intersects processed; 10 tiles in 0.167 S; 59.88 tiles/S; total: 4767 tiles in 808.871 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 53/314; 17 tile intersects processed; 10 tiles in 0.176 S; 56.82 tiles/S; total: 4777 tiles in 809.26 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 54/314; 16 tile intersects processed; 10 tiles in 0.181 S; 55.25 tiles/S; total: 4787 tiles in 809.653 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 55/314; 21 tile intersects processed; 10 tiles in 0.213 S; 46.95 tiles/S; total: 4797 tiles in 810.101 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 56/314; 19 tile intersects processed; 10 tiles in 0.283 S; 35.34 tiles/S; total: 4807 tiles in 810.655 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 57/314; 16 tile intersects processed; 10 tiles in 0.217 S; 46.08 tiles/S; total: 4817 tiles in 811.195 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 58/314; 15 tile intersects processed; 10 tiles in 0.305 S; 32.79 tiles/S; total: 4827 tiles in 811.768 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 59/314; 21 tile intersects processed; 10 tiles in 0.233 S; 42.92 tiles/S; total: 4837 tiles in 812.365 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 60/314; 14 tile intersects processed; 10 tiles in 0.283 S; 35.34 tiles/S; total: 4847 tiles in 812.941 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 61/314; 17 tile intersects processed; 10 tiles in 0.205 S; 48.78 tiles/S; total: 4857 tiles in 813.493 S; size: 1.2GB
Geolevel: 3; zooomlevel: 9; block: 62/314; 17 tile intersects processed; 10 tiles in 0.191 S; 52.36 tiles/S; total: 4867 tiles in 813.938 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 63/314; 16 tile intersects processed; 10 tiles in 0.231 S; 43.29 tiles/S; total: 4877 tiles in 814.415 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 64/314; 19 tile intersects processed; 10 tiles in 0.247 S; 40.49 tiles/S; total: 4887 tiles in 814.952 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 65/314; 17 tile intersects processed; 10 tiles in 0.245 S; 40.82 tiles/S; total: 4897 tiles in 815.502 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 66/314; 17 tile intersects processed; 10 tiles in 0.222 S; 45.05 tiles/S; total: 4907 tiles in 816.033 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 67/314; 13 tile intersects processed; 10 tiles in 0.197 S; 50.76 tiles/S; total: 4917 tiles in 816.524 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 68/314; 11 tile intersects processed; 10 tiles in 0.057 S; 175.44 tiles/S; total: 4927 tiles in 816.837 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 69/314; 10 tile intersects processed; 10 tiles in 0.047 S; 212.77 tiles/S; total: 4937 tiles in 816.985 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 70/314; 12 tile intersects processed; 10 tiles in 0.208 S; 48.08 tiles/S; total: 4947 tiles in 817.257 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 71/314; 10 tile intersects processed; 10 tiles in 0.038 S; 263.16 tiles/S; total: 4957 tiles in 817.601 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 72/314; 14 tile intersects processed; 10 tiles in 0.152 S; 65.79 tiles/S; total: 4967 tiles in 817.818 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 73/314; 16 tile intersects processed; 10 tiles in 0.234 S; 42.74 tiles/S; total: 4977 tiles in 818.267 S; size: 1.21GB
Geolevel: 3; zooomlevel: 9; block: 74/314; 18 tile intersects processed; 10 tiles in 0.199 S; 50.25 tiles/S; total: 4987 tiles in 818.763 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 75/314; 18 tile intersects processed; 10 tiles in 0.236 S; 42.37 tiles/S; total: 4997 tiles in 819.243 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 76/314; 17 tile intersects processed; 10 tiles in 0.319 S; 31.35 tiles/S; total: 5007 tiles in 819.852 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 77/314; 18 tile intersects processed; 10 tiles in 0.312 S; 32.05 tiles/S; total: 5017 tiles in 820.546 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 78/314; 16 tile intersects processed; 10 tiles in 0.279 S; 35.84 tiles/S; total: 5027 tiles in 821.22 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 79/314; 18 tile intersects processed; 10 tiles in 0.302 S; 33.11 tiles/S; total: 5037 tiles in 821.885 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 80/314; 15 tile intersects processed; 10 tiles in 0.244 S; 40.98 tiles/S; total: 5047 tiles in 822.526 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 81/314; 15 tile intersects processed; 10 tiles in 0.096 S; 104.17 tiles/S; total: 5057 tiles in 822.914 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 82/314; 26 tile intersects processed; 10 tiles in 0.04 S; 250 tiles/S; total: 5067 tiles in 823.081 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 83/314; 17 tile intersects processed; 10 tiles in 0.032 S; 312.5 tiles/S; total: 5077 tiles in 823.17 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 84/314; 40 tile intersects processed; 10 tiles in 0.054 S; 185.19 tiles/S; total: 5087 tiles in 823.274 S; size: 1.22GB
Geolevel: 3; zooomlevel: 9; block: 85/314; 32 tile intersects processed; 10 tiles in 0.072 S; 138.89 tiles/S; total: 5097 tiles in 823.436 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 86/314; 40 tile intersects processed; 10 tiles in 0.071 S; 140.85 tiles/S; total: 5107 tiles in 823.618 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 87/314; 35 tile intersects processed; 10 tiles in 0.075 S; 133.33 tiles/S; total: 5117 tiles in 823.792 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 88/314; 31 tile intersects processed; 10 tiles in 0.071 S; 140.85 tiles/S; total: 5127 tiles in 823.974 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 89/314; 29 tile intersects processed; 10 tiles in 0.058 S; 172.41 tiles/S; total: 5137 tiles in 824.12 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 90/314; 38 tile intersects processed; 10 tiles in 0.082 S; 121.95 tiles/S; total: 5147 tiles in 824.283 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 91/314; 33 tile intersects processed; 10 tiles in 0.055 S; 181.82 tiles/S; total: 5157 tiles in 824.459 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 92/314; 53 tile intersects processed; 10 tiles in 0.111 S; 90.09 tiles/S; total: 5167 tiles in 824.659 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 93/314; 26 tile intersects processed; 10 tiles in 0.063 S; 158.73 tiles/S; total: 5177 tiles in 824.866 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 94/314; 28 tile intersects processed; 10 tiles in 0.035 S; 285.71 tiles/S; total: 5187 tiles in 825.007 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 95/314; 32 tile intersects processed; 10 tiles in 0.095 S; 105.26 tiles/S; total: 5197 tiles in 825.156 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 96/314; 32 tile intersects processed; 10 tiles in 0.055 S; 181.82 tiles/S; total: 5207 tiles in 825.336 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 97/314; 42 tile intersects processed; 10 tiles in 0.05 S; 200 tiles/S; total: 5217 tiles in 825.475 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 98/314; 26 tile intersects processed; 10 tiles in 0.077 S; 129.87 tiles/S; total: 5227 tiles in 825.634 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 99/314; 31 tile intersects processed; 10 tiles in 0.04 S; 250 tiles/S; total: 5237 tiles in 825.768 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 100/314; 34 tile intersects processed; 10 tiles in 0.038 S; 263.16 tiles/S; total: 5247 tiles in 825.866 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 101/314; 29 tile intersects processed; 10 tiles in 0.068 S; 147.06 tiles/S; total: 5257 tiles in 825.99 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 102/314; 30 tile intersects processed; 10 tiles in 0.04 S; 250 tiles/S; total: 5267 tiles in 826.13 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 103/314; 27 tile intersects processed; 10 tiles in 0.037 S; 270.27 tiles/S; total: 5277 tiles in 826.237 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 104/314; 22 tile intersects processed; 10 tiles in 0.036 S; 277.78 tiles/S; total: 5287 tiles in 826.33 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 105/314; 34 tile intersects processed; 10 tiles in 0.05 S; 200 tiles/S; total: 5297 tiles in 826.431 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 106/314; 30 tile intersects processed; 10 tiles in 0.043 S; 232.56 tiles/S; total: 5307 tiles in 826.537 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 107/314; 35 tile intersects processed; 10 tiles in 0.034 S; 294.12 tiles/S; total: 5317 tiles in 826.638 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 108/314; 32 tile intersects processed; 10 tiles in 0.044 S; 227.27 tiles/S; total: 5327 tiles in 826.743 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 109/314; 21 tile intersects processed; 10 tiles in 0.028 S; 357.14 tiles/S; total: 5337 tiles in 826.833 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 110/314; 42 tile intersects processed; 10 tiles in 0.084 S; 119.05 tiles/S; total: 5347 tiles in 826.961 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 111/314; 29 tile intersects processed; 10 tiles in 0.04 S; 250 tiles/S; total: 5357 tiles in 827.099 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 112/314; 22 tile intersects processed; 10 tiles in 0.04 S; 250 tiles/S; total: 5367 tiles in 827.195 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 113/314; 24 tile intersects processed; 10 tiles in 0.058 S; 172.41 tiles/S; total: 5377 tiles in 827.316 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 114/314; 23 tile intersects processed; 10 tiles in 0.035 S; 285.71 tiles/S; total: 5387 tiles in 827.427 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 115/314; 36 tile intersects processed; 10 tiles in 0.115 S; 86.96 tiles/S; total: 5397 tiles in 827.597 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 116/314; 26 tile intersects processed; 10 tiles in 0.037 S; 270.27 tiles/S; total: 5407 tiles in 827.785 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 117/314; 24 tile intersects processed; 10 tiles in 0.055 S; 181.82 tiles/S; total: 5417 tiles in 827.9 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 118/314; 33 tile intersects processed; 10 tiles in 0.098 S; 102.04 tiles/S; total: 5427 tiles in 828.074 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 119/314; 32 tile intersects processed; 10 tiles in 0.044 S; 227.27 tiles/S; total: 5437 tiles in 828.268 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 120/314; 24 tile intersects processed; 10 tiles in 0.053 S; 188.68 tiles/S; total: 5447 tiles in 828.378 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 121/314; 32 tile intersects processed; 10 tiles in 0.091 S; 109.89 tiles/S; total: 5457 tiles in 828.546 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 122/314; 29 tile intersects processed; 10 tiles in 0.04 S; 250 tiles/S; total: 5467 tiles in 828.748 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 123/314; 25 tile intersects processed; 10 tiles in 0.063 S; 158.73 tiles/S; total: 5477 tiles in 828.896 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 124/314; 32 tile intersects processed; 10 tiles in 0.08 S; 125 tiles/S; total: 5487 tiles in 829.056 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 125/314; 37 tile intersects processed; 10 tiles in 0.039 S; 256.41 tiles/S; total: 5497 tiles in 829.214 S; size: 1.23GB
Geolevel: 3; zooomlevel: 9; block: 126/314; 23 tile intersects processed; 10 tiles in 0.054 S; 185.19 tiles/S; total: 5507 tiles in 829.341 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 127/314; 32 tile intersects processed; 10 tiles in 0.064 S; 156.25 tiles/S; total: 5517 tiles in 829.482 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 128/314; 47 tile intersects processed; 10 tiles in 0.077 S; 129.87 tiles/S; total: 5527 tiles in 829.648 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 129/314; 20 tile intersects processed; 10 tiles in 0.036 S; 277.78 tiles/S; total: 5537 tiles in 829.782 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 130/314; 39 tile intersects processed; 10 tiles in 0.075 S; 133.33 tiles/S; total: 5547 tiles in 829.909 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 131/314; 41 tile intersects processed; 10 tiles in 0.069 S; 144.93 tiles/S; total: 5557 tiles in 830.085 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 132/314; 23 tile intersects processed; 10 tiles in 0.06 S; 166.67 tiles/S; total: 5567 tiles in 830.246 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 133/314; 28 tile intersects processed; 10 tiles in 0.039 S; 256.41 tiles/S; total: 5577 tiles in 830.364 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 134/314; 38 tile intersects processed; 10 tiles in 0.056 S; 178.57 tiles/S; total: 5587 tiles in 830.479 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 135/314; 28 tile intersects processed; 10 tiles in 0.078 S; 128.21 tiles/S; total: 5597 tiles in 830.657 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 136/314; 28 tile intersects processed; 10 tiles in 0.059 S; 169.49 tiles/S; total: 5607 tiles in 830.835 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 137/314; 31 tile intersects processed; 10 tiles in 0.041 S; 243.9 tiles/S; total: 5617 tiles in 830.952 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 138/314; 27 tile intersects processed; 10 tiles in 0.056 S; 178.57 tiles/S; total: 5627 tiles in 831.073 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 139/314; 28 tile intersects processed; 10 tiles in 0.047 S; 212.77 tiles/S; total: 5637 tiles in 831.182 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 140/314; 28 tile intersects processed; 10 tiles in 0.035 S; 285.71 tiles/S; total: 5647 tiles in 831.281 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 141/314; 33 tile intersects processed; 10 tiles in 0.045 S; 222.22 tiles/S; total: 5657 tiles in 831.395 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 142/314; 29 tile intersects processed; 10 tiles in 0.035 S; 285.71 tiles/S; total: 5667 tiles in 831.51 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 143/314; 30 tile intersects processed; 10 tiles in 0.044 S; 227.27 tiles/S; total: 5677 tiles in 831.611 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 144/314; 25 tile intersects processed; 10 tiles in 0.023 S; 434.78 tiles/S; total: 5687 tiles in 831.697 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 145/314; 21 tile intersects processed; 10 tiles in 0.043 S; 232.56 tiles/S; total: 5697 tiles in 831.778 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 146/314; 28 tile intersects processed; 10 tiles in 0.035 S; 285.71 tiles/S; total: 5707 tiles in 831.871 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 147/314; 34 tile intersects processed; 10 tiles in 0.036 S; 277.78 tiles/S; total: 5717 tiles in 831.961 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 148/314; 24 tile intersects processed; 10 tiles in 0.023 S; 434.78 tiles/S; total: 5727 tiles in 832.038 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 149/314; 29 tile intersects processed; 10 tiles in 0.038 S; 263.16 tiles/S; total: 5737 tiles in 832.116 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 150/314; 40 tile intersects processed; 10 tiles in 0.053 S; 188.68 tiles/S; total: 5747 tiles in 832.233 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 151/314; 28 tile intersects processed; 10 tiles in 0.025 S; 400 tiles/S; total: 5757 tiles in 832.331 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 152/314; 25 tile intersects processed; 10 tiles in 0.033 S; 303.03 tiles/S; total: 5767 tiles in 832.412 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 153/314; 38 tile intersects processed; 10 tiles in 0.053 S; 188.68 tiles/S; total: 5777 tiles in 832.512 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 154/314; 32 tile intersects processed; 10 tiles in 0.028 S; 357.14 tiles/S; total: 5787 tiles in 832.62 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 155/314; 28 tile intersects processed; 10 tiles in 0.029 S; 344.83 tiles/S; total: 5797 tiles in 832.694 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 156/314; 42 tile intersects processed; 10 tiles in 0.068 S; 147.06 tiles/S; total: 5807 tiles in 832.818 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 157/314; 31 tile intersects processed; 10 tiles in 0.039 S; 256.41 tiles/S; total: 5817 tiles in 832.946 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 158/314; 30 tile intersects processed; 10 tiles in 0.023 S; 434.78 tiles/S; total: 5827 tiles in 833.027 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 159/314; 40 tile intersects processed; 10 tiles in 0.041 S; 243.9 tiles/S; total: 5837 tiles in 833.113 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 160/314; 40 tile intersects processed; 10 tiles in 0.045 S; 222.22 tiles/S; total: 5847 tiles in 833.214 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 161/314; 28 tile intersects processed; 10 tiles in 0.033 S; 303.03 tiles/S; total: 5857 tiles in 833.33 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 162/314; 27 tile intersects processed; 10 tiles in 0.016 S; 625 tiles/S; total: 5867 tiles in 833.403 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 163/314; 28 tile intersects processed; 10 tiles in 0.034 S; 294.12 tiles/S; total: 5877 tiles in 833.468 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 164/314; 24 tile intersects processed; 10 tiles in 0.024 S; 416.67 tiles/S; total: 5887 tiles in 833.54 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 165/314; 35 tile intersects processed; 10 tiles in 0.033 S; 303.03 tiles/S; total: 5897 tiles in 833.612 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 166/314; 48 tile intersects processed; 10 tiles in 0.029 S; 344.83 tiles/S; total: 5907 tiles in 833.686 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 167/314; 39 tile intersects processed; 10 tiles in 0.029 S; 344.83 tiles/S; total: 5917 tiles in 833.764 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 168/314; 32 tile intersects processed; 10 tiles in 0.059 S; 169.49 tiles/S; total: 5927 tiles in 833.865 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 169/314; 34 tile intersects processed; 10 tiles in 0.032 S; 312.5 tiles/S; total: 5937 tiles in 833.97 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 170/314; 33 tile intersects processed; 10 tiles in 0.02 S; 500 tiles/S; total: 5947 tiles in 834.032 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 171/314; 39 tile intersects processed; 10 tiles in 0.021 S; 476.19 tiles/S; total: 5957 tiles in 834.085 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 172/314; 34 tile intersects processed; 10 tiles in 0.055 S; 181.82 tiles/S; total: 5967 tiles in 834.172 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 173/314; 38 tile intersects processed; 10 tiles in 0.045 S; 222.22 tiles/S; total: 5977 tiles in 834.28 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 174/314; 48 tile intersects processed; 10 tiles in 0.021 S; 476.19 tiles/S; total: 5987 tiles in 834.377 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 175/314; 53 tile intersects processed; 10 tiles in 0.021 S; 476.19 tiles/S; total: 5997 tiles in 834.439 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 176/314; 31 tile intersects processed; 10 tiles in 0.041 S; 243.9 tiles/S; total: 6007 tiles in 834.52 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 177/314; 27 tile intersects processed; 10 tiles in 0.034 S; 294.12 tiles/S; total: 6017 tiles in 834.613 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 178/314; 44 tile intersects processed; 10 tiles in 0.017 S; 588.24 tiles/S; total: 6027 tiles in 834.674 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 179/314; 49 tile intersects processed; 10 tiles in 0.03 S; 333.33 tiles/S; total: 6037 tiles in 834.738 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 180/314; 29 tile intersects processed; 10 tiles in 0.036 S; 277.78 tiles/S; total: 6047 tiles in 834.821 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 181/314; 38 tile intersects processed; 10 tiles in 0.042 S; 238.1 tiles/S; total: 6057 tiles in 834.912 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 182/314; 52 tile intersects processed; 10 tiles in 0.026 S; 384.62 tiles/S; total: 6067 tiles in 835.005 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 183/314; 46 tile intersects processed; 10 tiles in 0.027 S; 370.37 tiles/S; total: 6077 tiles in 835.073 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 184/314; 36 tile intersects processed; 10 tiles in 0.027 S; 370.37 tiles/S; total: 6087 tiles in 835.145 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 185/314; 42 tile intersects processed; 10 tiles in 0.037 S; 270.27 tiles/S; total: 6097 tiles in 835.227 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 186/314; 56 tile intersects processed; 10 tiles in 0.027 S; 370.37 tiles/S; total: 6107 tiles in 835.321 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 187/314; 48 tile intersects processed; 10 tiles in 0.023 S; 434.78 tiles/S; total: 6117 tiles in 835.392 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 188/314; 35 tile intersects processed; 10 tiles in 0.021 S; 476.19 tiles/S; total: 6127 tiles in 835.454 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 189/314; 56 tile intersects processed; 10 tiles in 0.042 S; 238.1 tiles/S; total: 6137 tiles in 835.535 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 190/314; 51 tile intersects processed; 10 tiles in 0.031 S; 322.58 tiles/S; total: 6147 tiles in 835.637 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 191/314; 51 tile intersects processed; 10 tiles in 0.027 S; 370.37 tiles/S; total: 6157 tiles in 835.724 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 192/314; 34 tile intersects processed; 10 tiles in 0.022 S; 454.55 tiles/S; total: 6167 tiles in 835.794 S; size: 1.24GB
Geolevel: 3; zooomlevel: 9; block: 193/314; 43 tile intersects processed; 10 tiles in 0.023 S; 434.78 tiles/S; total: 6177 tiles in 835.874 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 194/314; 61 tile intersects processed; 10 tiles in 0.027 S; 370.37 tiles/S; total: 6187 tiles in 835.952 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 195/314; 58 tile intersects processed; 10 tiles in 0.038 S; 263.16 tiles/S; total: 6197 tiles in 836.049 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 196/314; 38 tile intersects processed; 10 tiles in 0.035 S; 285.71 tiles/S; total: 6207 tiles in 836.171 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 197/314; 46 tile intersects processed; 10 tiles in 0.027 S; 370.37 tiles/S; total: 6217 tiles in 836.246 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 198/314; 61 tile intersects processed; 10 tiles in 0.028 S; 357.14 tiles/S; total: 6227 tiles in 836.32 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 199/314; 56 tile intersects processed; 10 tiles in 0.03 S; 333.33 tiles/S; total: 6237 tiles in 836.417 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 200/314; 60 tile intersects processed; 10 tiles in 0.058 S; 172.41 tiles/S; total: 6247 tiles in 836.538 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 201/314; 43 tile intersects processed; 10 tiles in 0.037 S; 270.27 tiles/S; total: 6257 tiles in 836.667 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 202/314; 56 tile intersects processed; 10 tiles in 0.028 S; 357.14 tiles/S; total: 6267 tiles in 836.759 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 203/314; 58 tile intersects processed; 10 tiles in 0.041 S; 243.9 tiles/S; total: 6277 tiles in 836.856 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 204/314; 61 tile intersects processed; 10 tiles in 0.059 S; 169.49 tiles/S; total: 6287 tiles in 836.997 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 205/314; 33 tile intersects processed; 10 tiles in 0.048 S; 208.33 tiles/S; total: 6297 tiles in 837.174 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 206/314; 73 tile intersects processed; 10 tiles in 0.045 S; 222.22 tiles/S; total: 6307 tiles in 837.289 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 207/314; 57 tile intersects processed; 10 tiles in 0.037 S; 270.27 tiles/S; total: 6317 tiles in 837.402 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 208/314; 69 tile intersects processed; 10 tiles in 0.084 S; 119.05 tiles/S; total: 6327 tiles in 837.567 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 209/314; 37 tile intersects processed; 10 tiles in 0.041 S; 243.9 tiles/S; total: 6337 tiles in 837.722 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 210/314; 58 tile intersects processed; 10 tiles in 0.037 S; 270.27 tiles/S; total: 6347 tiles in 837.827 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 211/314; 57 tile intersects processed; 10 tiles in 0.038 S; 263.16 tiles/S; total: 6357 tiles in 837.926 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 212/314; 69 tile intersects processed; 10 tiles in 0.115 S; 86.96 tiles/S; total: 6367 tiles in 838.106 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 213/314; 38 tile intersects processed; 10 tiles in 0.032 S; 312.5 tiles/S; total: 6377 tiles in 838.28 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 214/314; 59 tile intersects processed; 10 tiles in 0.033 S; 303.03 tiles/S; total: 6387 tiles in 838.369 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 215/314; 74 tile intersects processed; 10 tiles in 0.057 S; 175.44 tiles/S; total: 6397 tiles in 838.487 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 216/314; 65 tile intersects processed; 10 tiles in 0.123 S; 81.3 tiles/S; total: 6407 tiles in 838.693 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 217/314; 32 tile intersects processed; 10 tiles in 0.043 S; 232.56 tiles/S; total: 6417 tiles in 838.882 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 218/314; 68 tile intersects processed; 10 tiles in 0.036 S; 277.78 tiles/S; total: 6427 tiles in 838.984 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 219/314; 58 tile intersects processed; 10 tiles in 0.044 S; 227.27 tiles/S; total: 6437 tiles in 839.092 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 220/314; 54 tile intersects processed; 10 tiles in 0.097 S; 103.09 tiles/S; total: 6447 tiles in 839.261 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 221/314; 45 tile intersects processed; 10 tiles in 0.06 S; 166.67 tiles/S; total: 6457 tiles in 839.466 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 222/314; 69 tile intersects processed; 10 tiles in 0.039 S; 256.41 tiles/S; total: 6467 tiles in 839.57 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 223/314; 68 tile intersects processed; 10 tiles in 0.072 S; 138.89 tiles/S; total: 6477 tiles in 839.705 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 224/314; 44 tile intersects processed; 10 tiles in 0.061 S; 163.93 tiles/S; total: 6487 tiles in 839.871 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 225/314; 70 tile intersects processed; 10 tiles in 0.039 S; 256.41 tiles/S; total: 6497 tiles in 840.008 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 226/314; 69 tile intersects processed; 10 tiles in 0.044 S; 227.27 tiles/S; total: 6507 tiles in 840.126 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 227/314; 67 tile intersects processed; 10 tiles in 0.087 S; 114.94 tiles/S; total: 6517 tiles in 840.284 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 228/314; 43 tile intersects processed; 10 tiles in 0.043 S; 232.56 tiles/S; total: 6527 tiles in 840.454 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 229/314; 69 tile intersects processed; 10 tiles in 0.038 S; 263.16 tiles/S; total: 6537 tiles in 840.551 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 230/314; 71 tile intersects processed; 10 tiles in 0.066 S; 151.52 tiles/S; total: 6547 tiles in 840.686 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 231/314; 39 tile intersects processed; 10 tiles in 0.058 S; 172.41 tiles/S; total: 6557 tiles in 840.841 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 232/314; 60 tile intersects processed; 10 tiles in 0.035 S; 285.71 tiles/S; total: 6567 tiles in 840.975 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 233/314; 68 tile intersects processed; 10 tiles in 0.044 S; 227.27 tiles/S; total: 6577 tiles in 841.085 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 234/314; 75 tile intersects processed; 10 tiles in 0.106 S; 94.34 tiles/S; total: 6587 tiles in 841.265 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 235/314; 31 tile intersects processed; 10 tiles in 0.049 S; 204.08 tiles/S; total: 6597 tiles in 841.47 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 236/314; 68 tile intersects processed; 10 tiles in 0.05 S; 200 tiles/S; total: 6607 tiles in 841.582 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 237/314; 66 tile intersects processed; 10 tiles in 0.054 S; 185.19 tiles/S; total: 6617 tiles in 841.711 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 238/314; 58 tile intersects processed; 10 tiles in 0.076 S; 131.58 tiles/S; total: 6627 tiles in 841.875 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 239/314; 51 tile intersects processed; 10 tiles in 0.04 S; 250 tiles/S; total: 6637 tiles in 842.034 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 240/314; 68 tile intersects processed; 10 tiles in 0.057 S; 175.44 tiles/S; total: 6647 tiles in 842.175 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 241/314; 79 tile intersects processed; 10 tiles in 0.09 S; 111.11 tiles/S; total: 6657 tiles in 842.349 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 242/314; 41 tile intersects processed; 10 tiles in 0.064 S; 156.25 tiles/S; total: 6667 tiles in 842.528 S; size: 1.25GB
Geolevel: 3; zooomlevel: 9; block: 243/314; 61 tile intersects processed; 10 tiles in 0.042 S; 238.1 tiles/S; total: 6677 tiles in 842.653 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 244/314; 70 tile intersects processed; 10 tiles in 0.062 S; 161.29 tiles/S; total: 6687 tiles in 842.783 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 245/314; 59 tile intersects processed; 10 tiles in 0.06 S; 166.67 tiles/S; total: 6697 tiles in 842.936 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 246/314; 49 tile intersects processed; 10 tiles in 0.033 S; 303.03 tiles/S; total: 6707 tiles in 843.078 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 247/314; 77 tile intersects processed; 10 tiles in 0.048 S; 208.33 tiles/S; total: 6717 tiles in 843.195 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 248/314; 80 tile intersects processed; 10 tiles in 0.05 S; 200 tiles/S; total: 6727 tiles in 843.319 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 249/314; 31 tile intersects processed; 10 tiles in 0.036 S; 277.78 tiles/S; total: 6737 tiles in 843.435 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 250/314; 63 tile intersects processed; 10 tiles in 0.056 S; 178.57 tiles/S; total: 6747 tiles in 843.553 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 251/314; 97 tile intersects processed; 10 tiles in 0.067 S; 149.25 tiles/S; total: 6757 tiles in 843.697 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 252/314; 47 tile intersects processed; 10 tiles in 0.048 S; 208.33 tiles/S; total: 6767 tiles in 843.854 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 253/314; 40 tile intersects processed; 10 tiles in 0.046 S; 217.39 tiles/S; total: 6777 tiles in 843.979 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 254/314; 74 tile intersects processed; 10 tiles in 0.073 S; 136.99 tiles/S; total: 6787 tiles in 844.134 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 255/314; 58 tile intersects processed; 10 tiles in 0.071 S; 140.85 tiles/S; total: 6797 tiles in 844.315 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 256/314; 31 tile intersects processed; 10 tiles in 0.042 S; 238.1 tiles/S; total: 6807 tiles in 844.454 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 257/314; 85 tile intersects processed; 10 tiles in 0.078 S; 128.21 tiles/S; total: 6817 tiles in 844.59 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 258/314; 58 tile intersects processed; 10 tiles in 0.066 S; 151.52 tiles/S; total: 6827 tiles in 844.769 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 259/314; 44 tile intersects processed; 10 tiles in 0.033 S; 303.03 tiles/S; total: 6837 tiles in 844.914 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 260/314; 92 tile intersects processed; 10 tiles in 0.072 S; 138.89 tiles/S; total: 6847 tiles in 845.054 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 261/314; 68 tile intersects processed; 10 tiles in 0.07 S; 142.86 tiles/S; total: 6857 tiles in 845.23 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 262/314; 53 tile intersects processed; 10 tiles in 0.044 S; 227.27 tiles/S; total: 6867 tiles in 845.374 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 263/314; 98 tile intersects processed; 10 tiles in 0.079 S; 126.58 tiles/S; total: 6877 tiles in 845.538 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 264/314; 86 tile intersects processed; 10 tiles in 0.077 S; 129.87 tiles/S; total: 6887 tiles in 845.727 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 265/314; 40 tile intersects processed; 10 tiles in 0.043 S; 232.56 tiles/S; total: 6897 tiles in 845.876 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 266/314; 97 tile intersects processed; 10 tiles in 0.066 S; 151.52 tiles/S; total: 6907 tiles in 846.023 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 267/314; 107 tile intersects processed; 10 tiles in 0.089 S; 112.36 tiles/S; total: 6917 tiles in 846.217 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 268/314; 60 tile intersects processed; 10 tiles in 0.059 S; 169.49 tiles/S; total: 6927 tiles in 846.406 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 269/314; 89 tile intersects processed; 10 tiles in 0.064 S; 156.25 tiles/S; total: 6937 tiles in 846.575 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 270/314; 115 tile intersects processed; 10 tiles in 0.122 S; 81.97 tiles/S; total: 6947 tiles in 846.801 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 271/314; 42 tile intersects processed; 10 tiles in 0.05 S; 200 tiles/S; total: 6957 tiles in 847.062 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 272/314; 67 tile intersects processed; 10 tiles in 0.052 S; 192.31 tiles/S; total: 6967 tiles in 847.21 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 273/314; 113 tile intersects processed; 10 tiles in 0.146 S; 68.49 tiles/S; total: 6977 tiles in 847.433 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 274/314; 59 tile intersects processed; 10 tiles in 0.061 S; 163.93 tiles/S; total: 6987 tiles in 847.694 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 275/314; 85 tile intersects processed; 10 tiles in 0.095 S; 105.26 tiles/S; total: 6997 tiles in 847.89 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 276/314; 103 tile intersects processed; 10 tiles in 0.134 S; 74.63 tiles/S; total: 7007 tiles in 848.146 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 277/314; 45 tile intersects processed; 10 tiles in 0.071 S; 140.85 tiles/S; total: 7017 tiles in 848.399 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 278/314; 89 tile intersects processed; 10 tiles in 0.098 S; 102.04 tiles/S; total: 7027 tiles in 848.591 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 279/314; 89 tile intersects processed; 10 tiles in 0.118 S; 84.75 tiles/S; total: 7037 tiles in 848.84 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 280/314; 48 tile intersects processed; 10 tiles in 0.059 S; 169.49 tiles/S; total: 7047 tiles in 849.053 S; size: 1.26GB
Geolevel: 3; zooomlevel: 9; block: 281/314; 90 tile intersects processed; 10 tiles in 0.105 S; 95.24 tiles/S; total: 7057 tiles in 849.259 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 282/314; 66 tile intersects processed; 10 tiles in 0.097 S; 103.09 tiles/S; total: 7067 tiles in 849.494 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 283/314; 45 tile intersects processed; 10 tiles in 0.066 S; 151.52 tiles/S; total: 7077 tiles in 849.701 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 284/314; 93 tile intersects processed; 10 tiles in 0.085 S; 117.65 tiles/S; total: 7087 tiles in 849.892 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 285/314; 56 tile intersects processed; 10 tiles in 0.101 S; 99.01 tiles/S; total: 7097 tiles in 850.121 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 286/314; 42 tile intersects processed; 10 tiles in 0.061 S; 163.93 tiles/S; total: 7107 tiles in 850.321 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 287/314; 84 tile intersects processed; 10 tiles in 0.107 S; 93.46 tiles/S; total: 7117 tiles in 850.524 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 288/314; 32 tile intersects processed; 10 tiles in 0.05 S; 200 tiles/S; total: 7127 tiles in 850.728 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 289/314; 78 tile intersects processed; 10 tiles in 0.1 S; 100 tiles/S; total: 7137 tiles in 850.919 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 290/314; 62 tile intersects processed; 10 tiles in 0.105 S; 95.24 tiles/S; total: 7147 tiles in 851.158 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 291/314; 86 tile intersects processed; 10 tiles in 0.1 S; 100 tiles/S; total: 7157 tiles in 851.4 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 292/314; 61 tile intersects processed; 10 tiles in 0.065 S; 153.85 tiles/S; total: 7167 tiles in 851.599 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 293/314; 99 tile intersects processed; 10 tiles in 0.117 S; 85.47 tiles/S; total: 7177 tiles in 851.81 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 294/314; 63 tile intersects processed; 10 tiles in 0.06 S; 166.67 tiles/S; total: 7187 tiles in 852.037 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 295/314; 96 tile intersects processed; 10 tiles in 0.13 S; 76.92 tiles/S; total: 7197 tiles in 852.274 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 296/314; 55 tile intersects processed; 10 tiles in 0.051 S; 196.08 tiles/S; total: 7207 tiles in 852.512 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 297/314; 67 tile intersects processed; 10 tiles in 0.103 S; 97.09 tiles/S; total: 7217 tiles in 852.704 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 298/314; 61 tile intersects processed; 10 tiles in 0.064 S; 156.25 tiles/S; total: 7227 tiles in 852.914 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 299/314; 39 tile intersects processed; 10 tiles in 0.06 S; 166.67 tiles/S; total: 7237 tiles in 853.08 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 300/314; 65 tile intersects processed; 10 tiles in 0.074 S; 135.14 tiles/S; total: 7247 tiles in 853.237 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 301/314; 44 tile intersects processed; 10 tiles in 0.04 S; 250 tiles/S; total: 7257 tiles in 853.392 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 302/314; 58 tile intersects processed; 10 tiles in 0.056 S; 178.57 tiles/S; total: 7267 tiles in 853.521 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 303/314; 58 tile intersects processed; 10 tiles in 0.055 S; 181.82 tiles/S; total: 7277 tiles in 853.66 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 304/314; 55 tile intersects processed; 10 tiles in 0.058 S; 172.41 tiles/S; total: 7287 tiles in 853.813 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 305/314; 47 tile intersects processed; 10 tiles in 0.07 S; 142.86 tiles/S; total: 7297 tiles in 853.974 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 306/314; 35 tile intersects processed; 10 tiles in 0.055 S; 181.82 tiles/S; total: 7307 tiles in 854.117 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 307/314; 28 tile intersects processed; 10 tiles in 0.052 S; 192.31 tiles/S; total: 7317 tiles in 854.258 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 308/314; 22 tile intersects processed; 10 tiles in 0.036 S; 277.78 tiles/S; total: 7327 tiles in 854.387 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 309/314; 22 tile intersects processed; 10 tiles in 0.046 S; 217.39 tiles/S; total: 7337 tiles in 854.489 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 310/314; 39 tile intersects processed; 10 tiles in 0.094 S; 106.38 tiles/S; total: 7347 tiles in 854.656 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 311/314; 88 tile intersects processed; 10 tiles in 0.071 S; 140.85 tiles/S; total: 7357 tiles in 854.847 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 312/314; 10 tile intersects processed; 10 tiles in 0.023 S; 434.78 tiles/S; total: 7367 tiles in 854.976 S; size: 1.27GB
Geolevel: 3; zooomlevel: 9; block: 313/314; 11 tile intersects processed; 10 tiles in 0.285 S; 35.09 tiles/S; total: 7377 tiles in 855.31 S; size: 1.28GB
Geolevel: 3; zooomlevel: 9; block: 314/314; 8 tile intersects processed; 8 tiles in 0.335 S; 23.88 tiles/S; total: 7385 tiles in 856.002 S; size: 1.28GB
Created dataLoader XML config [xml]: C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/usa_2014_geography_metadata.xml >>>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<geography_meta_data>
  <file_path>C:\Users\phamb\Documents\Local Data Loading\Tile maker USA/usa_2014_geography_metadata.xml</file_path>
  <geographies>
    <geography>
      <name>USA_2014</name>
      <geographical_resolution_level>
        <order>1</order>
        <display_name>The nation at a scale of 1:5,000,000</display_name>
        <database_field_name>CB_2014_US_NATION_5M</database_field_name>
      </geographical_resolution_level>
      <geographical_resolution_level>
        <order>2</order>
        <display_name>The State at a scale of 1:500,000</display_name>
        <database_field_name>CB_2014_US_STATE_500K</database_field_name>
      </geographical_resolution_level>
      <geographical_resolution_level>
        <order>3</order>
        <display_name>The County at a scale of 1:500,000</display_name>
        <database_field_name>CB_2014_US_COUNTY_500K</database_field_name>
      </geographical_resolution_level>
    </geography>
  </geographies>
</geography_meta_data>
<<< end of XML config [xml]
All 6 tests passed, none failed
Zoomlevel and geolevel report (null tiles/total tiles)
           zoomlevel           geolevel_1           geolevel_2           geolevel_3 
-------------------- -------------------- -------------------- -------------------- 
                   0                  0/1                  0/1                  0/1 
                   1                                       0/3                  0/3 
                   2                                       0/5                  0/5 
                   3                                      0/10                 0/10 
                   4                                      0/22                 0/22 
                   5                                      0/47                 0/49 
                   6                                     0/111                0/119 
                   7                                     0/281                0/333 
                   8                                     0/665                0/992 
                   9                                    0/1568               0/3137 

endTransaction(): COMMIT;
mssqlTileMaker.js exit: OK;  took: 856.669s; 0 error(s); 781 messages(s)
```