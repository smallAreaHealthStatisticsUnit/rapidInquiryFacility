# RIF40 Postgres dastbase build from Github

## Database development environment

Current two databases are used: sahsuland and sahsuland_dev. Sahsuland is kept stable for long periods, 
sahsuland_dev is the working database. Both sahsuland and sahsuland_dev are built with the completed alter 
scripts. Instructions to build the sahsuland_dev database to include the alter scripts under development are included.

After the move from the SAHSU private network to the main 
Imperial network the database structure is only modified by alter scripts so that the SAHSU Private network 
and Microsoft SQL Server Ports can be kept up to date. Alter scripts are desgined to be run more than once without 
side effects, although they must be run in numeric order.

Once the RIF is in production alter scripts will only be able to complete successfully once and cannot then 
be re-run.

The directory structure is rifDatabase\Postgres\:

* conf				- Exmaples of varaious configuration files
* etc				- Postgres psql logon script (.psqlrc)
* example_data		- Example extracts and results creating as part of SAHSUland build
* logs				- Logs from psql_scripts runs
* PLpgsql			- PL/pgsql scripts
* psql_scripts		- PSQL scripts; **Postgres database build directory**
* sahsuland			- SAHSUland creation psql scripts
* sahsuland\data	- SAHSUland data
* shapefiles		- Postgres psql SQL scripts derived from shapefiles, creation scripts
* shapefiles\data	- Shapefiles

The databases sahsuland and sahsuland_dev are built using make and Node.js. 

## Tool chain 

On Windows:

* Install MinGW with the full development environment and add *c:\MinGW\msys\1.0\bin* to path
* Install Python 2.7 **not Python 3.n**
* Install a C/C++ compiler, suitable for use by Node.js. I used Microsoft Visual Studio 2012 
  under an Academic licence. The dependency on Node.js will be reduced by replacing the current 
  program with Node.js web services available for use by the wider RIF community.

On Windows, Linux and MacOS

* Install Postgres 9.3.5+ or 9.4.1+ and PostGIS 2.1.3+. Building will fail on Postgres 9.2 or 
  below as PL/pgsql GET STACKED DIAGNOSTICS is used; PostGIS before 2.1.3 has bugs that will cause 
  shapefile simplification to fail.
* Install 64 bit Node.js. The 32 bit node will run into memory limitation problems will vary large 
  shapefiles.
* Install R; integrate R into Postgres [optional]

### Pre build tests

Run up a command tool, *not* in the Postgres build directory (psql_scripts):

* Type *make*. Check make works correctly:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres>make
make: *** No targets specified and no makefile found.  Stop.
```
* Check logon to psql with a password for:
--* postgres
* Check Node.js is correctly installed; *cd GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node*; *make install topojson*
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node>make install topojson
Debug level set to default: 0
npm install pg pg-native topojson
\


> libpq@1.5.1 install C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq
> node-gyp rebuild


C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq>node "c:\Program Files\nodejs\node_modules\npm\bin\node-gyp-bin\\..\..\node_modules\node-gyp\bin\node-gyp.js" rebuild
Building the projects in this solution one at a time. To enable parallel build, please add the "/m" switch.
  connection.cc
  connect-async-worker.cc
  addon.cc
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(156): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(156): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(156): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(165): warning C4267: '=' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(165): warning C4267: '=' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(165): warning C4267: '=' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(183): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(183): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(183): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
..\src\connection.cc(691): warning C4267: 'initializing' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
C:\Users\Peter\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
C:\Users\Peter\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
C:\Users\Peter\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
     Creating library C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\Release\addon.lib and object C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\Release\addon.exp
  Generating code
  Finished generating code
  addon.vcxproj -> C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\Release\\addon.node
pg@4.3.0 node_modules\pg
+-- packet-reader@0.2.0
+-- pg-connection-string@0.1.3
+-- buffer-writer@1.0.0
+-- generic-pool@2.1.1
+-- pg-types@1.7.0
+-- semver@4.3.1
+-- pgpass@0.0.3 (split@0.3.3)

topojson@1.6.18 node_modules\topojson
+-- queue-async@1.0.7
+-- rw@0.1.4
+-- d3@3.5.5
+-- optimist@0.3.7 (wordwrap@0.0.2)
+-- shapefile@0.3.0 (iconv-lite@0.2.11)
+-- d3-geo-projection@0.2.14 (brfs@1.4.0)

pg-native@1.8.0 node_modules\pg-native
+-- pg-types@1.6.0
+-- readable-stream@1.0.31 (isarray@0.0.1, string_decoder@0.10.31, core-util-is@1.0.1, inherits@2.0.1)
+-- libpq@1.5.1 (bindings@1.2.1, nan@1.5.0)
node_modules\topojson\bin\topojson --version
1.6.18
node_modules\topojson\bin\topojson -q 1e6 -o test_6_geojson_test_01.json ../psql_scripts/test_scripts/data/test_6_geojson_test_01.json
bounds: -6.68852598 54.6456466 -6.32507059 55.01219818 (spherical)
pre-quantization: 0.0404m (3.63e-7°) 0.0408m (3.67e-7°)
topology: 160 arcs, 3502 points
prune: retained 160 / 160 arcs (100%)
node_modules\topojson\bin\topojson -q 1e6 -o test_6_sahsu_4_level4_0_0_0.json ../psql_scripts/test_scripts/data/test_6_sahsu_4_level4_0_0_0.json
bounds: -7.58829438 52.68753577 -4.886537859999999 55.5268098 (spherical)
pre-quantization: 0.300m (0.00000270°) 0.316m (0.00000284°)
topology: 3299 arcs, 70897 points
prune: retained 3299 / 3299 arcs (100%)

```
---**If the toposjon node.js does not run correctly then node is not properly installed**. 
* Check R is integrated into Postgres as PLR (See the PLR build instructions).

### Build control using make

The RIF database is built using GNU make for ease of portability. The RIF is built from the directory 
*GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts*.

#### Principal targets

* Create sahsuland_dev and sahusland:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node> make db_setup
```
---**Note that this does not apply the alter scripts under development**
* To build or rebuild sahsuland_dev:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node> make clean sahsuland_dev
```
---**Note that this does not apply the alter scripts under development**
* To build or rebuild sahsuland_dev:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node> make clean sahsuland_dev
```
#### Configuring make

**Do not edit the Makefile directly; subsequent git changes will cause conflicts and you may loose 
your changes.** Instead copy Makefile.local.example to Makefile.local and edit Makefile.local. Beware 
of your choice of editor on especially Windows. The RIF is developed on Windows and Linux at the same time so 
you will have files with both Linux <CR> and Windows <CRLF> semantics. Windows Notepad in particular 
does not understand Linux files.



#### Porting limitations

Makefiles have the following limitations:

* Full dependency tracking for SQL scripts has not yet been implemented; you are advised to do a *make clean* 
  or *make devclean* before building as in the below examples or nothing much may happen.
* A fully working version of Node.js that can compile is required or you ewill not be able to generate the 
  topoJSON tiles data.

#### Help

The Makefile has help:

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>make help
Debug level set to default: 0
findstr "#-" Makefile
#-
#- Rapid Enquiry Facility (RIF) - Makefile for \\GitHub\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts
#-
#- DO NOT RUN THE SUBDIRECTORY MAKEFILES DIRECTLY; THEY REQUIRE ENVIRONMENT SETUP TO WORK CORRECTLY
#-
        HELP=findstr "\#-" Makefile
        HELP=grep "\#-" Makefile
#-
#- PL/pgsql debug levels (DEBUG_LEVEL);
#-
#- 0 - Suppressed, INFO only
#- 1 - Major function calls
#- 2 - Major function calls, data
#- 3 - Reserved for future used
#- 4 - Reserved for future used
#-
#- PSQL verbosity (VERBOSITY):
#-
#- verbose      - Messages/errors with full context
#- terse        - Just the error or message
#-
#- PSQL echo (ECHO)
#-
#- all:                 - All SQL
#- none:                - No SQL
#-
#- Targets
#-
#- 1. patching
#-
#- all: Run all completed alter scripts and test [DEFAULT]
#- patch: Run all completed alter scripts on both sahsuland_dev and sahusland
#- dev: Run all alter scripts in development
#-
#- 2. build
#-
#- sahsuland_dev_no_alter: Rebuild sahsuland_dev, test [State of SAHSULAND at port to SQL server], finally VACUUM ANALYSE
#- sahsuland_dev: Rebuild sahsuland_dev, test, then patch dev only, retest, finally VACUUM ANALYZE
#- Does not run all alter scripts in development
#- topojson_convert: GeoJSON to topoJSON converter
#-
#- 3. installers
#-
#- sahsuland_dump: Dump sahsuland database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#- sahsuland_dev_dump: Dump sahsuland_dev database to plain SQL, excluding UK91, EW01 shapefile data from non dev dumps
#-                     Used to create sahsuland
#-
#- 4. test
#-
#- test: Run all test scripts [Non verbose, no debug]
#- test_no_alter: Run test scripts able to be run before the alter scripts [Non verbose, no debug]
#- test: Run all test scripts [debug_level=1]
#- test: Run all test scripts [Verbose, debug_level=2, echo=all]
#-
#- 5. cleanup
#-
#- clean: Remove logs so completed scripts can be re-run
#- devclean: Remove logs so alter scripts in development can be r-run
#-           Not normally needed as they abort.
#-
#- 7. Database setup. Needs to be able to connect to postgresDB as postgres
#-
#- db_setup: Create sahusland, sahsuland_dev
#- db_install: Create sahsuland, sahsuland_dev from production pg_dump
#-
#- 7. miscellaneous
#-
#- v4_0_vacuum_analyse_dev: VACUUM ANALYZE sahsuland dev database
#- help: Display this help
#- recurse: Recursive make target: make recurse <recursive target>
#-          e.g. make recurse alter_1.rpt
#-

```
