# README for RIF node.js database test harness

* node.js is required to build to geoJSON to topoJSON converter by Mike Bostock at: https://github.com/mbostock/topojson/wiki/Installation

* node.js is available from: http://nodejs.org/

## Node Installation

* On Windows install MS Visual Studio; e.g. from Dreamspark
* [Install GDAL if QGis is not installed]
* Install Python (2.7 or later) from https://www.python.org/downloads/ (NOT 3.x.x series!)]
* Install node.js

## Install topojson

[Not required at present]

Then install topojson through npm:

npm install topojson

Test:

make 

```topojson
C:\Users\pch\AppData\Roaming\npm\topojson.cmd -q 1e6 -o test_6_geojson_test_01.json ..\psql_scripts\test_scripts\data\test_6_geojson_test_01.json
bounds: -6.68852598 54.6456466 -6.32507059 55.01219818 (spherical)
pre-quantization: 0.0404m (3.63e-7°) 0.0408m (3.67e-7°)
topology: 160 arcs, 3502 points
prune: retained 160 / 160 arcs (100%)
```

## Install Postgres connectors pg and pg-native

Checks: 

* Type: pg_config to test if Postgres extensibility is installed, pg-native requires MS Visual Studio.
* check you can connect to psql without a password (i.e. using pgass/Kerberos). pg-native must be able to connect to the database to install!

```npm
P:\Github\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>npm install pg pg-native
> libpq@1.6.4 install P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq
> node-gyp rebuild


P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq>node "c:\Program Files\nodejs\node_modules\npm\bin\node-gyp-bin\\..
\..\node_modules\node-gyp\bin\node-gyp.js" rebuild
Building the projects in this solution one at a time. To enable parallel build, please add the "/m" switch.
  connection.cc
  connect-async-worker.cc
  addon.cc
p:\github\rapidinquiryfacility\rifdatabase\testharness\db_test_harness\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [P:\Gi
thub\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
p:\github\rapidinquiryfacility\rifdatabase\testharness\db_test_harness\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [P:\Gi
thub\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
p:\github\rapidinquiryfacility\rifdatabase\testharness\db_test_harness\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [P:\Gi
thub\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
..\src\connection.cc(691): warning C4267: 'initializing' : conversion from 'size_t' to 'int', possible loss of data [P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test
_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
C:\Users\pch\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [P:\Github\rap
idInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
C:\Users\pch\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [P:\Github\rap
idInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
C:\Users\pch\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [P:\Github\rap
idInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
     Creating library P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\Release\addon.lib and object P:\Github
  \rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\Release\addon.exp
  Generating code
  Finished generating code
  addon.vcxproj -> P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\Release\\addon.node
pg@4.2.0 node_modules\pg
+-- packet-reader@0.2.0
+-- pg-connection-string@0.1.3
+-- buffer-writer@1.0.0
+-- generic-pool@2.1.1
+-- pg-types@1.6.0
+-- semver@4.2.0
+-- pgpass@0.0.3 (split@0.3.3)
```

## NPM (Node package manager) Make integration

# Make targets

The Makefile will building the requiored Node.js modules

* all: Build modules, run the complete database test harness
* modules: Build required Node.js modules using npm install --save to update dependencies in package.json
* update: Update required Node.js modules using npm install --save to update dependencies in package.json 
* db_test_harness: Run the complete database test harness      (same as target test)
* rif40_create_disease_mapping_example: Run rif40_create_disease_mapping_example test (standard test 1)
* test: Run the complete database test harness
* retest: Re-run the failed database test harness tests (-F flag)
* help: Display makefile help, db_test_harness.js help

# Usage

``` node
node db_test_harness.js --help
Usage: test_harness [options]

Version: 0.1

RIF 4.0 Database test harness.

Options:
  -d, --debug_level  RIF database PL/pgsql debug level      [default: 0]
  -D, --database     name of Postgres database              [default: "sahsuland_dev"]
  -U, --username     Postgres database username             [default: "pch"]
  -P, --port         Postgres database port                 [default: 5432]
  -H, --hostname     hostname of Postgres database          [default: "wpea-rif1"]
  -F, --failed       re-run failed tests                    [default: false]
  -C, --class        Test run class                         [default: null]
  --help             display this helpful message and exit  [default: false]
```

* Example

``` node
P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness>make modules rif40_create_disease_mapping_example
Debug level set to default: 1
make: Nothing to be done for `modules'.
node db_test_harness.js -H wpea-rif1 -D sahsuland_dev -U pch -d 1 -C rif40_create_disease_mapping_example
1: Connected to Postgres using: postgres://pch@wpea-rif1:5432/sahsuland_dev?application_name=db_test_harness
1: notice: rif40_log_setup() send DEBUG to INFO: off; debug function list: []
1: notice: rif40_startup(): SQL> SET search_path TO pch,rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions,rif_studies;
1: notice: rif40_startup(): Created temporary table: g_rif40_study_areas
1: notice: rif40_startup(): Created temporary table: g_rif40_comparison_areas
1: notice: Function postgis_topology_scripts_installed() not found. Is topology support enabled and topology.sql installed?
1: notice: rif40_startup(): PostGIS extension V2.1.3 (POSTGIS="2.1.3 r12547" GEOS="3.4.2-CAPI-1.8.2 r3924" PROJ="Rel. 4.8.0, 6 March 2012" GDAL="GDAL 1.10.0, released 2013/04/24" L
IBXML="2.7.8" LIBJSON="UNKNOWN" RASTER)
1: notice: rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
1: notice: rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
1: notice: rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: pch
1: notice: rif40_startup(): search_path: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions, rif_studies, reset: rif40, public, to
pology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
1: notice: rif40_startup(): Deleted 0, created 2 tables/views/foreign data wrapper tables
1: notice: rif40_log_setup() send DEBUG to INFO: off; debug function list: []
1: notice: _rif40_sql_test_log_setup(): [71402]: debug_level 1
1: notice: rif40_send_debug_to_info(true) SET send DEBUG to INFO: off; debug function list: [_rif40_sql_test_log_setup:DEBUG1]
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_delete_study
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_ddl
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: _rif40_sql_test
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: _rif40_test_sql_template
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: _rif40_sql_test_register
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_test_harness
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_startup
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: _rif40_sql_test_log_setup
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_remove_from_debug
1: Wait for client 2 initialisation; debug_level: 1
2: Connect to Postgres using: postgres://pch@wpea-rif1:5432/sahsuland_dev?application_name=db_test_harness
1: Client 2 initialised; debug_level: 1
2: COMMIT transaction;
Only test run class: rif40_create_disease_mapping_example
1: Class: rif40_create_disease_mapping_example Tests: 9
1: Total tests to run: 9
1: [1/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2: BEGIN transaction: [1/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:06 811] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_studies_checks
2[09:04:07 53] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 1 */ INTO rif40_studies (
                geography, project, study_name, study_type,
                comparison_geolevel_name, study_geolevel_name, denom_tab,
                year_start, year_stop, max_age_group, min_age_group,
                suppression_value, extract_permitted, transfer_permitted)
        VALUES (
                 'SAHSU'                                        /* geography */,
                 'TEST'                                                 /* project */,
                 'SAHSULAND test 4 study_id 1 example'                                  /* study_name */,
                 1                                                                      /* study_type [disease mapping] */,
                 'LEVEL2'       /* comparison_geolevel_name */,
                 'LEVEL4'                               /* study_geolevel_name */,
                 'SAHSULAND_POP'                                        /* denom_tab */,
                 1989                                   /* year_start */,
                 1996                                   /* year_stop */,
                 21     /* max_age_group */,
                 0      /* min_age_group */,
                 5 /* suppression_value */,
                 1              /* extract_permitted */,
                 1              /* transfer_permitted */);
2[09:04:07 162] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20200-5] T_RIF40_STUDIES study 302 CRUD checks OK
2[09:04:07 256] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20206-7] T_RIF40_STUDIES study 302 comparison area geolevel name: "LEVEL2" OK
2[09:04:07 349] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20209] T_RIF40_STUDIES study 302 study area geolevel name: "LEVEL4" OK
2[09:04:08 511] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20212] T_RIF40_STUDIES study 302 denominator accessible as: pop.SAHSULAND_POP
2[09:04:08 608] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20216-21] T_RIF40_STUDIES study 302 year/age bands checks OK against RIF40_TABLES
2[09:04:08 702] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20222] T_RIF40_STUDIES study 302 study area geolevel id (4/LEVEL4) >= comparision area (2/LEVEL2) [i.e study
 area has the same or higher resolution]
2[09:04:08 802] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20223] T_RIF40_STUDIES study 302 suppressed at: 5
2[09:04:08 904] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20224-5] T_RIF40_STUDIES study 302 may be extracted, study geolevel LEVEL4 is not restricted for user: pch
2[09:04:11 280] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20228] T_RIF40_STUDIES study 302 extract table: S302_EXTRACT cannot be accessed; state: C [IGNORED]
2[09:04:13 651] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20229] T_RIF40_STUDIES study 302 map table: S302_MAP cannot be accessed; state: C [IGNORED]
2[09:04:13 747] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20233] T_RIF40_STUDIES study: 302 denominator RIF40_TABLES age sex group field column: pop.SAHSULAND_POP.AGE
_SEX_GROUP found
2[09:04:13 838] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:06.797, proccessed 1 rows
2[09:04:13 842] notice: _rif40_sql_test(): [71267] PASSED: Test case 1: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 1/9] Test OK: [1/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 2
1: RECURSE level 1: [2/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:14 26] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_investigations_checks
2[09:04:14 260] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 2 */ INTO rif40_investigations(
        inv_name,
        inv_description,
        genders,
        numer_tab,
        year_start,
        year_stop,
        max_age_group,
        min_age_group
)
VALUES (
        'INV_1'                 /* inv_name */,
        'Lung cancer'   /* inv_description */,
        3                       /* genders [both] */,
        'SAHSULAND_CANCER'              /* numer_tab */,
        1989            /* year_start */,
        1996            /* year_stop */,
        21 /* max_age_group */,
        0 /* min_age_group */);
2[09:04:14 364] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20700-4] T_RIF40_INVESTIGATIONS study: 302 investigation: 99 CRUD checks OK
2[09:04:15 701] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20706] T_RIF40_INVESTIGATIONS study: 302 investigation: 99 numerator: SAHSULAND_CANCER accessible
2[09:04:15 802] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20707-17] T_RIF40_INVESTIGATIONS study: 302 investigation: 99 year/age checks OK
2[09:04:16 84] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20718] T_RIF40_INVESTIGATIONS study: 302 investigation: 99 numerator RIF40_TABLES total field column:
rif_data.SAHSULAND_CANCER.TOTAL found
2[09:04:16 186] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20721] T_RIF40_INVESTIGATIONS study: 302 investigation: 99 numerator RIF40_TABLES age sex group field
 column: rif_data.SAHSULAND_CANCER.AGE_SEX_GROUP found
2[09:04:16 291] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20722-5] T_RIF40_INVESTIGATIONS study: 302 investigation: 99 age_group IDs match; 1, same AGE_SEX_GRO
UP/AGE_GROUP/SEX_FIELD_NAMES used
2[09:04:16 380] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:02.109, proccessed 1 rows
2[09:04:16 385] notice: _rif40_sql_test(): [71267] PASSED: Test case 2: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 2/9] Test OK: [2/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 3
1: RECURSE level 2: [3/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:16 567] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_inv_conditions_checks
2[09:04:16 802] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 3 */ INTO rif40_inv_conditions(
                        outcome_group_name, min_condition, max_condition, predefined_group_name, line_number)
WITH data AS (
                                SELECT '{{SAHSULAND_ICD,C34,NULL,NULL},{SAHSULAND_ICD,162,1629,NULL}}'::Text[][] AS arr
                        ), b AS (
                                SELECT arr[i][1] AS outcome_group_name,
                                           arr[i][2] AS min_condition,
                                           arr[i][3] AS max_condition,
                                           arr[i][4] AS predefined_group_name,
                   ROW_NUMBER() OVER() AS line_number
                              FROM data, generate_subscripts((SELECT arr FROM data), 1) i
                        )
                SELECT outcome_group_name, min_condition, max_condition, predefined_group_name, line_number
                  FROM b
                RETURNING outcome_group_name, min_condition, max_condition, predefined_group_name;
2[09:04:16 901] notice: [DEBUG1] trigger_fct_t_rif40_inv_conditions_checks(): [20500-3] T_RIF40_INV_CONDITIONS study: 302 investigation: 99 line: 1 CRUD checks OK
2[09:04:17 5] notice: [DEBUG1] trigger_fct_t_rif40_inv_conditions_checks(): [20500-3] T_RIF40_INV_CONDITIONS study: 302 investigation: 99 line: 2 CRUD checks OK
2[09:04:17 182] notice: _rif40_sql_test(): [71267] PASSED: Test case 3: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 3/9] Test OK: [3/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 4
1: RECURSE level 3: [4/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:17 357] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_study_areas_checks
2[09:04:17 513] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_study_areas_checks2
2[09:04:17 779] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 4 */ INTO rif40_study_areas(area_id, band_id)
SELECT DISTINCT level4, ROW_NUMBER() OVER() AS band_id
          FROM sahsuland_geography
         WHERE level3 IN (
        SELECT unnest(
'{01.001.000100,01.001.000200,01.001.000300,01.002.000300,01.002.000400,01.002.000500,01.002.000600,01.002.000700,01.002.000800,01.002.000900,01.002.001000,01.002.001100,01.002.001
200,01.002.001300,01.002.001400,01.002.001500,01.002.001600,01.002.001700,01.002.001800,01.002.001900,01.002.002000,01.002.002100,01.002.002200,01.002.002300,01.003.003300,01.003.0
03400,01.004.011100,01.004.011200,01.004.011300,01.004.011400,01.004.011500,01.004.011600,01.004.011700,01.004.011800,01.004.011900,01.005.002400,01.006.002500,01.006.002600,01.006
.002700,01.007.012000,01.007.012100,01.007.012200,01.007.012300,01.007.012400,01.007.012500,01.008.001000,01.008.002900,01.008.003500,01.008.003600,01.008.003700,01.008.003800,01.0
08.003900,01.008.004000,01.008.004100,01.008.004200,01.008.004300,01.008.004400,01.008.004500,01.008.004600,01.008.004700,01.008.004800,01.008.004900,01.008.005000,01.008.005100,01
.008.005200,01.008.005300,01.008.005400,01.008.005500,01.008.005600,01.008.005700,01.008.005800,01.008.005900,01.008.006000,01.008.006100,01.008.006200,01.008.006300,01.008.006400,
01.008.006500,01.008.006600,01.008.006700,01.008.006800,01.008.006900,01.008.007000,01.008.007100,01.008.007200,01.008.007300,01.008.007400,01.008.007500,01.008.007600,01.008.00770
0,01.008.007800,01.008.007900,01.008.008000,01.008.008100,01.008.008200,01.008.008300,01.008.008400,01.008.008500,01.008.008600,01.008.008700,01.008.008800,01.008.008900,01.008.009
000,01.008.009100,01.008.009200,01.008.009300,01.008.009400,01.008.009500,01.008.009600,01.008.009700,01.008.009800,01.008.009900,01.008.010100,01.008.010200,01.008.010300,01.008.0
10400,01.008.010500,01.008.010600,01.008.010700,01.008.010800,01.008.010900,01.008.011000,01.009.002700,01.009.002800,01.009.002900,01.009.003000,01.009.003100,01.009.003200,01.011
.012600,01.011.012700,01.011.012800,01.011.012900,01.011.013000,01.011.013100,01.011.013200,01.011.013300,01.011.013400,01.011.013500,01.011.013600,01.011.013700,01.011.013800,01.0
11.013900,01.011.014000,01.011.014100,01.011.014200,01.011.014300,01.011.014400,01.011.014500,01.011.014600,01.011.014700,01.011.014800,01.011.014900,01.011.015000,01.011.015100,01
.011.015200,01.011.015300,01.011.015400,01.011.015500,01.011.015600,01.011.015700,01.011.015800,01.012.003000,01.012.015900,01.012.016000,01.013.016000,01.013.016100,01.013.016200,
01.013.016300,01.013.016400,01.013.016500,01.013.016600,01.013.016700,01.013.016800,01.014.017200,01.014.017300,01.014.017500,01.014.017600,01.014.017700,01.014.017800,01.014.01790
0,01.014.018000,01.014.018100,01.014.018200,01.014.018300,01.014.018400,01.014.018500,01.014.018600,01.014.018700,01.014.018800,01.015.016200,01.015.016900,01.016.017000,01.016.017
100,01.017.018900,01.017.019000,01.018.019100,01.018.019200,01.018.019300,01.018.019400,01.018.019500}'::Text[]) /* at Geolevel select */ AS study_area);
2[09:04:19 567] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:01.797, proccessed 1230 rows
2[09:04:19 572] notice: _rif40_sql_test(): [71267] PASSED: Test case 4: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 4/9] Test OK: [4/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 5
1: RECURSE level 4: [5/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:19 931] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_comp_areas_checks
2[09:04:20 88] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_comp_areas_checks2
2[09:04:20 353] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 5 */ INTO rif40_comparison_areas(area_id)
SELECT unnest(
'{01.001,01.002,01.003,01.004,01.005,01.006,01.007,01.008,01.009,01.011,01.012,01.013,01.014,01.015,01.016,01.017,01.018}'::Text[]) AS comparision_area;
2[09:04:23 979] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:03.625, proccessed 17 rows
2[09:04:23 983] notice: _rif40_sql_test(): [71267] PASSED: Test case 5: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 5/9] Test OK: [5/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 6
1: RECURSE level 5: [6/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:24 343] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_inv_covariates_checks
2[09:04:24 576] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 6 */ INTO rif40_inv_covariates(geography, covariate_name, study_geolevel_name, min, max)
WITH a AS (
        SELECT unnest(
         '{SES}'::Text[])::Text AS covariate_name,
              'LEVEL4'::Text AS study_geolevel_name,
              'SAHSU'::Text AS geography
)
SELECT a.geography, a.covariate_name, a.study_geolevel_name, b.min, b.max
  FROM a
        LEFT OUTER JOIN rif40_covariates b ON
                (a.covariate_name = b.covariate_name AND a.study_geolevel_name = b.geolevel_name AND a.geography = b.geography);
2[09:04:24 682] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20600-3] T_RIF40_INV_COVARIATES study: 302 investigation: 99 covariate: SES CRUD checks OK
2[09:04:24 779] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): T_RIF40_INV_COVARIATES study: 302 investigation: 99 covariate: SES study area geolevel name: LEVEL4 (i
d 4) same as geolevel in T_RIF40_STUDIES for study 302
2[09:04:24 878] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20266-71] T_RIF40_INV_COVARIATES study: 302 investigation: 99 covariate: SES max/in checks OK
2[09:04:26 76] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20272-3] T_RIF40_INV_COVARIATES study: 302 investigation: 99 covariate: SES covariate table column: ri
f_data.SAHSULAND_COVARIATES_LEVEL4.SES can be accessed
2[09:04:26 176] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20274] T_RIF40_INV_COVARIATES study: 302 investigation: 99 covariate: SES covariate table column: rif
_data.SAHSULAND_COVARIATES_LEVEL4.YEAR can be accessed
2[09:04:26 280] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20275] T_RIF40_INV_COVARIATES study: 302 investigation: 99 covariate: SES covariate table column: rif
_data.SAHSULAND_COVARIATES_LEVEL4.LEVEL4 can be accessed
2[09:04:26 373] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:01.797, proccessed 1 rows
2[09:04:26 380] notice: _rif40_sql_test(): [71267] PASSED: Test case 6: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 6/9] Test OK: [6/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 7
1: RECURSE level 6: [7/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:26 557] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_rif40_study_shares_checks
2[09:04:26 791] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 7 */ INTO rif40_study_shares(grantee_username) VALUES ('ffabbri');
2[09:04:26 889] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20325] RIF40_STUDY_SHARES study_id: 302 not owned by grantor: pch; owned by: pch; but grantor is a RIF_MA
NAGER
2[09:04:26 989] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20327] RIF40_STUDY_SHARES study_id: 302 grantee username: ffabbri is a RIF_USER/RIF_MANAGER
2[09:04:27 83] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20320-7] RIF40_STUDY_SHARES study: 302 CRUD checks OK
2[09:04:27 260] notice: _rif40_sql_test(): [71267] PASSED: Test case 7: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 7/9] Test OK: [7/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 8
1: RECURSE level 7: [8/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:27 432] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_rif40_study_shares_checks
2[09:04:27 666] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 7 */ INTO rif40_study_shares(grantee_username) VALUES ('mdouglas');
2[09:04:27 763] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20325] RIF40_STUDY_SHARES study_id: 302 not owned by grantor: pch; owned by: pch; but grantor is a RIF_MA
NAGER
2[09:04:27 864] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20327] RIF40_STUDY_SHARES study_id: 302 grantee username: mdouglas is a RIF_USER/RIF_MANAGER
2[09:04:27 959] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20320-7] RIF40_STUDY_SHARES study: 302 CRUD checks OK
2[09:04:28 136] notice: _rif40_sql_test(): [71267] PASSED: Test case 8: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 8/9] Test OK: [8/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 9
1: RECURSE level 8: [9/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[09:04:28 308] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_rif40_study_shares_checks
2[09:04:28 545] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 7 */ INTO rif40_study_shares(grantee_username) VALUES ('pch');
2[09:04:28 645] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20325] RIF40_STUDY_SHARES study_id: 302 not owned by grantor: pch; owned by: pch; but grantor is a RIF_MA
NAGER
2[09:04:28 739] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20327] RIF40_STUDY_SHARES study_id: 302 grantee username: pch is a RIF_USER/RIF_MANAGER
2[09:04:28 838] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20320-7] RIF40_STUDY_SHARES study: 302 CRUD checks OK
2[09:04:29 10] notice: _rif40_sql_test(): [71267] PASSED: Test case 9: SAHSULAND test 4 study_id 1 example no exceptions, no error expected
*****************************************************************************
*
* 2: [Recursive test 9/9] Test OK: [9/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

2: ROLLBACK transaction: [9/9]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
1: Test harness complete; 9 tests completed; passed: 9; none failed.
1: BEGIN transaction: results UPDATE
1: [1/9] [Recursive test 1/9] SAHSULAND test 4 study_id 1 example; id: 1; pass: true; time taken: 7.207 S
1: [2/9] [Recursive test 2/9] SAHSULAND test 4 study_id 1 example; id: 2; pass: true; time taken: 2.536 S
1: [3/9] [Recursive test 3/9] SAHSULAND test 4 study_id 1 example; id: 3; pass: true; time taken: 0.788 S
1: [4/9] [Recursive test 4/9] SAHSULAND test 4 study_id 1 example; id: 4; pass: true; time taken: 2.57 S
1: [5/9] [Recursive test 5/9] SAHSULAND test 4 study_id 1 example; id: 5; pass: true; time taken: 4.405 S
1: [6/9] [Recursive test 6/9] SAHSULAND test 4 study_id 1 example; id: 6; pass: true; time taken: 2.209 S
1: [7/9] [Recursive test 7/9] SAHSULAND test 4 study_id 1 example; id: 7; pass: true; time taken: 0.871 S
1: [8/9] [Recursive test 8/9] SAHSULAND test 4 study_id 1 example; id: 8; pass: true; time taken: 0.871 S
1: [9/9] [Recursive test 9/9] SAHSULAND test 4 study_id 1 example; id: 9; pass: true; time taken: 0.87 S
1: COMMIT transaction.
*****************************************************************************
*
* Test harness run had no error(s)
* Total time taken: 22.415 S
*
*****************************************************************************
```

Peter Hambly, 2nd September 2015 

