# Node install dialog

Installs:

* pg
* pg-native
* topojson

Uses pg_native (i.e. .psqlrc) to connect to database. Finally tests topojon.

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node>make install topojson
Debug level set to default: 0
npm install pg pg-native topojson
\


> libpq@1.5.1 install C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq
> node-gyp rebuild

|
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq>node "c:\Program Files\nodejs\node_modules\npm\bin\node-gyp-bin\\..\..\node_modules\node-gyp\bin\node-gyp.js" rebuild
Building the projects in this solution one at a time. To enable parallel build, please add the "/m" switch.
  connection.cc
  connect-async-worker.cc
  addon.cc
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(156): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(165): warning C4267: '=' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(156): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(165): warning C4267: '=' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(183): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(183): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(156): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(165): warning C4267: '=' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\node_modules\nan\nan_implementation_pre_12_inl.h(183): warning C4267: 'argument' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxp
roj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
..\src\connection.cc(691): warning C4267: 'initializing' : conversion from 'size_t' to 'int', possible loss of data [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
c:\users\peter\documents\github\rapidinquiryfacility\rifdatabase\postgres\node\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifDatabase\Postgres\Node\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
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
+-- pg-types@1.7.0
+-- buffer-writer@1.0.0
+-- generic-pool@2.1.1
+-- semver@4.3.1
+-- pgpass@0.0.3 (split@0.3.3)

pg-native@1.8.0 node_modules\pg-native
+-- pg-types@1.6.0
+-- readable-stream@1.0.31 (string_decoder@0.10.31, isarray@0.0.1, inherits@2.0.1, core-util-is@1.0.1)
+-- libpq@1.5.1 (bindings@1.2.1, nan@1.5.0)

topojson@1.6.18 node_modules\topojson
+-- queue-async@1.0.7
+-- rw@0.1.4
+-- optimist@0.3.7 (wordwrap@0.0.2)
+-- shapefile@0.3.0 (iconv-lite@0.2.11)
+-- d3@3.5.5
+-- d3-geo-projection@0.2.14 (brfs@1.4.0)
node_modules\topojson\bin\topojson --version
1.6.18
node_modules\topojson\bin\topojson -q 1e6 -o test_6_geojson_test_01.json ../psql_scripts/test_scripts/data/test_6_geojson_test_01.json
bounds: -6.68852598 54.6456466 -6.32507059 55.01219818 (spherical)
pre-quantization: 0.0404m (3.63e-7�) 0.0408m (3.67e-7�)

```
