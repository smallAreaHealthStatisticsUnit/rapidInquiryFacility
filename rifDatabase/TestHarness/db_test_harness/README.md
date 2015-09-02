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

npm install -g topojson

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

# Targets

* make modules - builds required modules, updates dependencies in package.json
* db_test_harness - Runs test harness

# Usage

``` node
node db_test_harness.js --help
Usage: test_harness [options] -- [test run class]

Version: 0.1

RIF 4.0 Database test harness.

Options:
  -d, --debug     RIF database PL/pgsql debug level      [default: 0]
  -D, --database  name of Postgres database              [default: "sahsuland_dev"]
  -U, --username  Postgres database username             [default: "pch"]
  -P, --port      Postgres database port                 [default: 5432]
  -H, --hostname  hostname of Postgres database          [default: "wpea-rif1"]
  -F, --failed    re-run failed tests                    [default: false]
  --help          display this helpful message and exit  [default: false]
```

* Example

``` node
node db_test_harness.js -H wpea-rif1 -D sahsuland_dev -U pch -d 1
```

Peter Hambly, 2nd September 2015 

