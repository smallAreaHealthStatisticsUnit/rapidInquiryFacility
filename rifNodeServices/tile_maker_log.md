Tile Maker log
==============

```
* LOG START *********************************************************************

Fri Jun 01 2018 16:12:48 GMT+0100 (GMT Daylight Time)
[nodeGeoSpatialServicesCommon:239; function: responseProcessing();
Url: /shpConvert; ip: ::ffff:127.0.0.1]
Diagnostics >>>

+0 S addStatus: [nodeGeoSpatialServices:290] Initial state: INIT; code: 200
No status file to re-create and no addStatusCallback, missing fields from response: []
Field: batchMode[true]; batch mode enabled
Field: quantization[1000000]; Initial pre and post quantization: 1000000
Field: simplificationFactor[0.75]; Simplification factor: 0.75
Field: max_zoomlevel[11]; 
Field: diagnostics[true]; verbose mode enabled
Field: geographyName[SAHSULAND]; 
Field: geographyDesc[SAHSU Example geography]; 
Field: sahsu_grd_level1_desc[Level 1 (top level)]; 
Field: sahsu_grd_level1_areaID[LEVEL1]; 
Field: sahsu_grd_level1_areaID_desc[Level 1 name]; 
Field: sahsu_grd_level1_areaName[LEVEL1]; 
Field: sahsu_grd_level1_areaName_desc[Level 1 ID]; 
Field: sahsu_grd_level2_desc[Level 2]; 
Field: sahsu_grd_level2_areaID[LEVEL2]; 
Field: sahsu_grd_level2_areaID_desc[Level 2]; 
Field: sahsu_grd_level2_areaName[NAME]; 
Field: sahsu_grd_level2_areaName_desc[Level 2 name]; 
Field: sahsu_grd_level3_desc[Level 3]; 
Field: sahsu_grd_level3_areaID[LEVEL3]; 
Field: sahsu_grd_level3_areaID_desc[Level 3]; 
Field: sahsu_grd_level3_areaName[LEVEL3]; 
Field: sahsu_grd_level3_areaName_desc[Level 3 ID]; 
Field: sahsu_grd_level4_desc[Level 4]; 
Field: sahsu_grd_level4_areaID[LEVEL4]; 
Field: sahsu_grd_level4_areaID_desc[Level 4]; 
Field: sahsu_grd_level4_areaName[LEVEL4]; 
Field: sahsu_grd_level4_areaName_desc[Level 4 ID]; 
Field: verbose[true]; 
mkdir: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa
[nodeGeoSpatialServices:561; function: fieldProcessing()] Creating diagnostics file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/diagnostics.log
[66d8a532-bb2c-4304-8e2b-ffde330b88fa] Creating status file: status.json
Field: uuidV1[66d8a532-bb2c-4304-8e2b-ffde330b88fa]; uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa
Field: sahsu_grd_level1_ID[ID]; 
Field: sahsu_grd_level1_LEVEL1[Level 1]; 
Field: sahsu_grd_level1_AREA[Area]; 
Field: sahsu_grd_level2_LEVEL2[Level 2]; 
Field: sahsu_grd_level2_AREA[Area]; 
Field: sahsu_grd_level2_LEVEL1[Level 1]; 
Field: sahsu_grd_level2_NAME[Level 2 name]; 
Field: sahsu_grd_level3_LEVEL2[Level 2]; 
Field: sahsu_grd_level3_LEVEL1[Level 1]; 
Field: sahsu_grd_level3_LEVEL3[Level 3]; 
Field: sahsu_grd_level4_PERIMETER[Perimeter]; 
Field: sahsu_grd_level4_LEVEL4[Level 4]; 
Field: sahsu_grd_level4_LEVEL2[Level 2]; 
Field: sahsu_grd_level4_LEVEL1[Level 1]; 
Field: sahsu_grd_level4_LEVEL3[Level 3]; 
File received OK [1]: SAHSULAND.zip; encoding: zip; uncompressed data: 7052628 bytes
+0.15 S addStatus: [nodeGeoSpatialServices:977:onBusboyFinish()] +0.15S new state: All form data and fields loaded; running completion processing; code: 200
Re-creating status file: status.json
Initial default min zooomlevel: 6
Batch mode: true; returning just before file compression processing.
Disable the diagnostic file write timer
+0.154 S addStatus: [nodeGeoSpatialServicesCommon:328:responseProcessing()] +0.154S new state: BATCH_START; code: 200
Re-creating status file: status.json
+0.229 S addStatus: [nodeGeoSpatialServices:750:fileCompressionProcessing()] +0.229S new state: Processing zip file [1]: SAHSULAND.zip; size: 6.73MB; code: 200
Re-creating status file: status.json
+0.279 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.279S new state: Expanded 1.2: SAHSULAND.zip//:geoDataLoader.xml as file 21 to list; size: 17.71KB; code: 200
Re-creating status file: status.json
Created reponse, size: 12106; saving to file: response.json.1
<<< End of diagnostics

No errors

* LOG END ***********************************************************************


* LOG START *********************************************************************

Fri Jun 01 2018 16:12:48 GMT+0100 (GMT Daylight Time)
[nodeGeoSpatialServicesCommon:262; function: writeResponseFileCallback();
Url: /shpConvert; ip: ::ffff:127.0.0.1]
Response sent; size: 12106 bytes

No errors

* LOG END ***********************************************************************

getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865968570(9mS); size: 11198; calls: 1; index: -1; statii: 8
C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.shp; shapefileData["mySrs"].proj4: {
  "input": "PROJCS[\"OSGB_1936_British_National_Grid\",GEOGCS[\"GCS_OSGB 1936\",DATUM[\"OSGB_1936\",SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.9996012717],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000],UNIT[\"Meter\",1]]",
  "name": "OSGB_1936_British_National_Grid",
  "proj4": "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +datum=OSGB36 +units=m +no_defs",
  "pretty_wkt": "PROJCS[\"OSGB_1936_British_National_Grid\",\n    GEOGCS[\"GCS_OSGB 1936\",\n        DATUM[\"OSGB_1936\",\n            SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],\n        PRIMEM[\"Greenwich\",0],\n        UNIT[\"Degree\",0.017453292519943295]],\n    PROJECTION[\"Transverse_Mercator\"],\n    PARAMETER[\"latitude_of_origin\",49],\n    PARAMETER[\"central_meridian\",-2],\n    PARAMETER[\"scale_factor\",0.9996012717],\n    PARAMETER[\"false_easting\",400000],\n    PARAMETER[\"false_northing\",-100000],\n    UNIT[\"Meter\",1]]",
  "esri": false,
  "valid": true,
  "is_geographic": false
}
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865969592(214mS); size: 55655; calls: 2; index: 8; statii: 38
C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.shp; shapefileData["mySrs"].proj4: {
  "input": "PROJCS[\"OSGB_1936_British_National_Grid\",GEOGCS[\"GCS_OSGB 1936\",DATUM[\"OSGB_1936\",SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.9996012717],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000],UNIT[\"Meter\",1]]",
  "name": "OSGB_1936_British_National_Grid",
  "proj4": "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +datum=OSGB36 +units=m +no_defs",
  "pretty_wkt": "PROJCS[\"OSGB_1936_British_National_Grid\",\n    GEOGCS[\"GCS_OSGB 1936\",\n        DATUM[\"OSGB_1936\",\n            SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],\n        PRIMEM[\"Greenwich\",0],\n        UNIT[\"Degree\",0.017453292519943295]],\n    PROJECTION[\"Transverse_Mercator\"],\n    PARAMETER[\"latitude_of_origin\",49],\n    PARAMETER[\"central_meridian\",-2],\n    PARAMETER[\"scale_factor\",0.9996012717],\n    PARAMETER[\"false_easting\",400000],\n    PARAMETER[\"false_northing\",-100000],\n    UNIT[\"Meter\",1]]",
  "esri": false,
  "valid": true,
  "is_geographic": false
}
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865970907(7mS); size: 78575; calls: 3; index: 38; statii: 59
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865971951(81mS); size: 92022; calls: 4; index: 59; statii: 71
C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.shp; shapefileData["mySrs"].proj4: {
  "input": "PROJCS[\"OSGB_1936_British_National_Grid\",GEOGCS[\"GCS_OSGB 1936\",DATUM[\"OSGB_1936\",SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.9996012717],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000],UNIT[\"Meter\",1]]",
  "name": "OSGB_1936_British_National_Grid",
  "proj4": "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +datum=OSGB36 +units=m +no_defs",
  "pretty_wkt": "PROJCS[\"OSGB_1936_British_National_Grid\",\n    GEOGCS[\"GCS_OSGB 1936\",\n        DATUM[\"OSGB_1936\",\n            SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],\n        PRIMEM[\"Greenwich\",0],\n        UNIT[\"Degree\",0.017453292519943295]],\n    PROJECTION[\"Transverse_Mercator\"],\n    PARAMETER[\"latitude_of_origin\",49],\n    PARAMETER[\"central_meridian\",-2],\n    PARAMETER[\"scale_factor\",0.9996012717],\n    PARAMETER[\"false_easting\",400000],\n    PARAMETER[\"false_northing\",-100000],\n    UNIT[\"Meter\",1]]",
  "esri": false,
  "valid": true,
  "is_geographic": false
}
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865973042(8mS); size: 102045; calls: 5; index: 71; statii: 81
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865974064(195mS); size: 110954; calls: 6; index: 81; statii: 88
C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.shp; shapefileData["mySrs"].proj4: {
  "input": "PROJCS[\"OSGB_1936_British_National_Grid\",GEOGCS[\"GCS_OSGB 1936\",DATUM[\"OSGB_1936\",SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.9996012717],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000],UNIT[\"Meter\",1]]",
  "name": "OSGB_1936_British_National_Grid",
  "proj4": "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +datum=OSGB36 +units=m +no_defs",
  "pretty_wkt": "PROJCS[\"OSGB_1936_British_National_Grid\",\n    GEOGCS[\"GCS_OSGB 1936\",\n        DATUM[\"OSGB_1936\",\n            SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],\n        PRIMEM[\"Greenwich\",0],\n        UNIT[\"Degree\",0.017453292519943295]],\n    PROJECTION[\"Transverse_Mercator\"],\n    PARAMETER[\"latitude_of_origin\",49],\n    PARAMETER[\"central_meridian\",-2],\n    PARAMETER[\"scale_factor\",0.9996012717],\n    PARAMETER[\"false_easting\",400000],\n    PARAMETER[\"false_northing\",-100000],\n    UNIT[\"Meter\",1]]",
  "esri": false,
  "valid": true,
  "is_geographic": false
}
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865975487(66mS); size: 124454; calls: 7; index: 88; statii: 101
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865976569(3mS); size: 125696; calls: 8; index: 101; statii: 102
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865977585(76mS); size: 129003; calls: 9; index: 102; statii: 105
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865978670(108mS); size: 132513; calls: 10; index: 105; statii: 108
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865982222(62mS); size: 169601; calls: 13; index: 119; statii: 137
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865983672(24mS); size: 179299; calls: 14; index: 137; statii: 143
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865984869(3mS); size: 191273; calls: 15; index: 143; statii: 153

* LOG START *********************************************************************

Fri Jun 01 2018 16:13:05 GMT+0100 (GMT Daylight Time)
[nodeGeoSpatialServicesCommon:303; function: responseProcessing();
Url: /shpConvert; ip: ::ffff:127.0.0.1]
Batch end diagnostics >>>
Zip file[1]: directory: SAHSULAND/
Zip file[2]: geoDataLoader.xml; relativePath: SAHSULAND/geoDataLoader.xml; date: Tue Jun 13 2017 07:56:30 GMT+0100 (GMT Daylight Time)
Decompress from: 2332 to: 18130; size: 18130 bytes
Zip file[3]: directory: SAHSULAND/SAHSULAND Shapefiles/
Zip file[4]: SAHSU_GRD_Level1.csv; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level1.csv; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 194223 to: 466397; size: 466397 bytes
Zip file[5]: SAHSU_GRD_Level1.dbf; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level1.dbf; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 95 to: 161; size: 161 bytes
Zip file[6]: SAHSU_GRD_Level1.prj; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level1.prj; date: Wed Jan 18 2017 15:38:12 GMT+0000 (GMT Standard Time)
Decompress from: 281 to: 417; size: 417 bytes
Zip file[7]: SAHSU_GRD_Level1.sbn; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level1.sbn; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 88 to: 132; size: 132 bytes
Zip file[8]: SAHSU_GRD_Level1.sbx; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level1.sbx; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 85 to: 116; size: 116 bytes
Zip file[9]: SAHSU_GRD_Level1.shp; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level1.shp; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 168561 to: 197664; size: 197664 bytes
Zip file[10]: SAHSU_GRD_Level1.shp.xml; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level1.shp.xml; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 500 to: 1009; size: 1009 bytes
Zip file[11]: SAHSU_GRD_Level1.shx; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level1.shx; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 82 to: 108; size: 108 bytes
Zip file[12]: SAHSU_GRD_Level2.csv; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level2.csv; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 618307 to: 1571365; size: 1571365 bytes
Zip file[13]: SAHSU_GRD_Level2.dbf; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level2.dbf; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 418 to: 1029; size: 1029 bytes
Zip file[14]: SAHSU_GRD_Level2.prj; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level2.prj; date: Wed Jan 18 2017 15:38:24 GMT+0000 (GMT Standard Time)
Decompress from: 281 to: 417; size: 417 bytes
Zip file[15]: SAHSU_GRD_Level2.sbn; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level2.sbn; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 213 to: 292; size: 292 bytes
Zip file[16]: SAHSU_GRD_Level2.sbx; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level2.sbx; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 94 to: 132; size: 132 bytes
Zip file[17]: SAHSU_GRD_Level2.shp; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level2.shp; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 562752 to: 666112; size: 666112 bytes
Zip file[18]: SAHSU_GRD_Level2.shp.xml; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level2.shp.xml; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 500 to: 1009; size: 1009 bytes
Zip file[19]: SAHSU_GRD_Level2.shx; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level2.shx; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 181 to: 236; size: 236 bytes
Zip file[20]: SAHSU_GRD_Level3.csv; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level3.csv; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 1218538 to: 3227928; size: 3227928 bytes
Zip file[21]: SAHSU_GRD_Level3.dbf; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level3.dbf; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 505 to: 5930; size: 5930 bytes
Zip file[22]: SAHSU_GRD_Level3.prj; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level3.prj; date: Wed Jan 18 2017 15:38:30 GMT+0000 (GMT Standard Time)
Decompress from: 281 to: 417; size: 417 bytes
Zip file[23]: SAHSU_GRD_Level3.sbn; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level3.sbn; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 1355 to: 2092; size: 2092 bytes
Zip file[24]: SAHSU_GRD_Level3.sbx; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level3.sbx; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 157 to: 244; size: 244 bytes
Zip file[25]: SAHSU_GRD_Level3.shp; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level3.shp; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 1110650 to: 1375272; size: 1375272 bytes
Zip file[26]: SAHSU_GRD_Level3.shp.xml; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level3.shp.xml; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 499 to: 1009; size: 1009 bytes
Zip file[27]: SAHSU_GRD_Level3.shx; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level3.shx; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 1153 to: 1700; size: 1700 bytes
Zip file[28]: SAHSU_GRD_Level4.csv; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level4.csv; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 1687027 to: 5219614; size: 5219614 bytes
Zip file[29]: SAHSU_GRD_Level4.dbf; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level4.dbf; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 14841 to: 75224; size: 75224 bytes
Zip file[30]: SAHSU_GRD_Level4.prj; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level4.prj; date: Wed Jan 18 2017 15:38:06 GMT+0000 (GMT Standard Time)
Decompress from: 281 to: 417; size: 417 bytes
Zip file[31]: SAHSU_GRD_Level4.sbn; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level4.sbn; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 6646 to: 12316; size: 12316 bytes
Zip file[32]: SAHSU_GRD_Level4.sbx; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level4.sbx; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 305 to: 540; size: 540 bytes
Zip file[33]: SAHSU_GRD_Level4.shp; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level4.shp; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 1445723 to: 2241992; size: 2241992 bytes
Zip file[34]: SAHSU_GRD_Level4.shp.xml; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level4.shp.xml; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 2634 to: 8344; size: 8344 bytes
Zip file[35]: SAHSU_GRD_Level4.shx; relativePath: SAHSULAND/SAHSULAND Shapefiles/SAHSU_GRD_Level4.shx; date: Sat Jan 09 2016 00:11:00 GMT+0000 (GMT Standard Time)
Decompress from: 5666 to: 9940; size: 9940 bytes
Processed Zipfile [1]: SAHSULAND.zip; extension: zip; number of files: 35; Uncompressed size: 15107705

+0 S addStatus: [nodeGeoSpatialServices:290] Initial state: INIT; code: 200
No status file to re-create and no addStatusCallback, missing fields from response: []
Field: batchMode[true]; batch mode enabled
Field: quantization[1000000]; Initial pre and post quantization: 1000000
Field: simplificationFactor[0.75]; Simplification factor: 0.75
Field: max_zoomlevel[11]; 
Field: diagnostics[true]; verbose mode enabled
Field: geographyName[SAHSULAND]; 
Field: geographyDesc[SAHSU Example geography]; 
Field: sahsu_grd_level1_desc[Level 1 (top level)]; 
Field: sahsu_grd_level1_areaID[LEVEL1]; 
Field: sahsu_grd_level1_areaID_desc[Level 1 name]; 
Field: sahsu_grd_level1_areaName[LEVEL1]; 
Field: sahsu_grd_level1_areaName_desc[Level 1 ID]; 
Field: sahsu_grd_level2_desc[Level 2]; 
Field: sahsu_grd_level2_areaID[LEVEL2]; 
Field: sahsu_grd_level2_areaID_desc[Level 2]; 
Field: sahsu_grd_level2_areaName[NAME]; 
Field: sahsu_grd_level2_areaName_desc[Level 2 name]; 
Field: sahsu_grd_level3_desc[Level 3]; 
Field: sahsu_grd_level3_areaID[LEVEL3]; 
Field: sahsu_grd_level3_areaID_desc[Level 3]; 
Field: sahsu_grd_level3_areaName[LEVEL3]; 
Field: sahsu_grd_level3_areaName_desc[Level 3 ID]; 
Field: sahsu_grd_level4_desc[Level 4]; 
Field: sahsu_grd_level4_areaID[LEVEL4]; 
Field: sahsu_grd_level4_areaID_desc[Level 4]; 
Field: sahsu_grd_level4_areaName[LEVEL4]; 
Field: sahsu_grd_level4_areaName_desc[Level 4 ID]; 
Field: verbose[true]; 
mkdir: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa
[nodeGeoSpatialServices:561; function: fieldProcessing()] Creating diagnostics file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/diagnostics.log
[66d8a532-bb2c-4304-8e2b-ffde330b88fa] Creating status file: status.json
Field: uuidV1[66d8a532-bb2c-4304-8e2b-ffde330b88fa]; uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa
Field: sahsu_grd_level1_ID[ID]; 
Field: sahsu_grd_level1_LEVEL1[Level 1]; 
Field: sahsu_grd_level1_AREA[Area]; 
Field: sahsu_grd_level2_LEVEL2[Level 2]; 
Field: sahsu_grd_level2_AREA[Area]; 
Field: sahsu_grd_level2_LEVEL1[Level 1]; 
Field: sahsu_grd_level2_NAME[Level 2 name]; 
Field: sahsu_grd_level3_LEVEL2[Level 2]; 
Field: sahsu_grd_level3_LEVEL1[Level 1]; 
Field: sahsu_grd_level3_LEVEL3[Level 3]; 
Field: sahsu_grd_level4_PERIMETER[Perimeter]; 
Field: sahsu_grd_level4_LEVEL4[Level 4]; 
Field: sahsu_grd_level4_LEVEL2[Level 2]; 
Field: sahsu_grd_level4_LEVEL1[Level 1]; 
Field: sahsu_grd_level4_LEVEL3[Level 3]; 
File received OK [1]: SAHSULAND.zip; encoding: zip; uncompressed data: 7052628 bytes
+0.15 S addStatus: [nodeGeoSpatialServices:977:onBusboyFinish()] +0.15S new state: All form data and fields loaded; running completion processing; code: 200
Re-creating status file: status.json
Initial default min zooomlevel: 6
Batch mode: true; returning just before file compression processing.
Disable the diagnostic file write timer
+0.154 S addStatus: [nodeGeoSpatialServicesCommon:328:responseProcessing()] +0.154S new state: BATCH_START; code: 200
Re-creating status file: status.json
+0.229 S addStatus: [nodeGeoSpatialServices:750:fileCompressionProcessing()] +0.229S new state: Processing zip file [1]: SAHSULAND.zip; size: 6.73MB; code: 200
Re-creating status file: status.json
+0.279 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.279S new state: Expanded 1.2: SAHSULAND.zip//:geoDataLoader.xml as file 21 to list; size: 17.71KB; code: 200
Re-creating status file: status.json
Created reponse, size: 12106; saving to file: response.json.1
+0.378 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.378S new state: Expanded 1.4: SAHSULAND.zip//:SAHSU_GRD_Level1.csv as file 32 to list; size: 455.47KB; code: 200
Re-creating status file: status.json
+0.383 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.383S new state: Expanded 1.5: SAHSULAND.zip//:SAHSU_GRD_Level1.dbf as file 43 to list; size: 0.16KB; code: 200
Re-creating status file: status.json
+0.391 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.391S new state: Expanded 1.6: SAHSULAND.zip//:SAHSU_GRD_Level1.prj as file 54 to list; size: 0.41KB; code: 200
Re-creating status file: status.json
+0.397 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.397S new state: Expanded 1.7: SAHSULAND.zip//:SAHSU_GRD_Level1.sbn as file 65 to list; size: 0.13KB; code: 200
Re-creating status file: status.json
+0.41 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.41S new state: Expanded 1.8: SAHSULAND.zip//:SAHSU_GRD_Level1.sbx as file 76 to list; size: 0.11KB; code: 200
Re-creating status file: status.json
+0.433 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.433S new state: Expanded 1.9: SAHSULAND.zip//:SAHSU_GRD_Level1.shp as file 87 to list; size: 193.03KB; code: 200
Re-creating status file: status.json
+0.437 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.437S new state: Expanded 1.10: SAHSULAND.zip//:SAHSU_GRD_Level1.shp.xml as file 98 to list; size: 0.99KB; code: 200
Re-creating status file: status.json
+0.44 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.44S new state: Expanded 1.11: SAHSULAND.zip//:SAHSU_GRD_Level1.shx as file 109 to list; size: 0.11KB; code: 200
Re-creating status file: status.json
+0.509 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.509S new state: Expanded 1.12: SAHSULAND.zip//:SAHSU_GRD_Level2.csv as file 1110 to list; size: 1.5MB; code: 200
Re-creating status file: status.json
+0.519 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.519S new state: Expanded 1.13: SAHSULAND.zip//:SAHSU_GRD_Level2.dbf as file 1211 to list; size: 1KB; code: 200
Re-creating status file: status.json
+0.522 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.522S new state: Expanded 1.14: SAHSULAND.zip//:SAHSU_GRD_Level2.prj as file 1312 to list; size: 0.41KB; code: 200
Re-creating status file: status.json
+0.525 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.525S new state: Expanded 1.15: SAHSULAND.zip//:SAHSU_GRD_Level2.sbn as file 1413 to list; size: 0.29KB; code: 200
Re-creating status file: status.json
+0.529 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.529S new state: Expanded 1.16: SAHSULAND.zip//:SAHSU_GRD_Level2.sbx as file 1514 to list; size: 0.13KB; code: 200
Re-creating status file: status.json
+0.548 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.548S new state: Expanded 1.17: SAHSULAND.zip//:SAHSU_GRD_Level2.shp as file 1615 to list; size: 650.5KB; code: 200
Re-creating status file: status.json
+0.55 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.55S new state: Expanded 1.18: SAHSULAND.zip//:SAHSU_GRD_Level2.shp.xml as file 1716 to list; size: 0.99KB; code: 200
Re-creating status file: status.json
+0.553 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.553S new state: Expanded 1.19: SAHSULAND.zip//:SAHSU_GRD_Level2.shx as file 1817 to list; size: 0.23KB; code: 200
Re-creating status file: status.json
+0.607 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.607S new state: Expanded 1.20: SAHSULAND.zip//:SAHSU_GRD_Level3.csv as file 1918 to list; size: 3.08MB; code: 200
Re-creating status file: status.json
+0.611 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.611S new state: Expanded 1.21: SAHSULAND.zip//:SAHSU_GRD_Level3.dbf as file 2019 to list; size: 5.79KB; code: 200
Re-creating status file: status.json
+0.615 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.615S new state: Expanded 1.22: SAHSULAND.zip//:SAHSU_GRD_Level3.prj as file 2120 to list; size: 0.41KB; code: 200
Re-creating status file: status.json
+0.619 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.619S new state: Expanded 1.23: SAHSULAND.zip//:SAHSU_GRD_Level3.sbn as file 2221 to list; size: 2.04KB; code: 200
Re-creating status file: status.json
+0.623 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.623S new state: Expanded 1.24: SAHSULAND.zip//:SAHSU_GRD_Level3.sbx as file 2322 to list; size: 0.24KB; code: 200
Re-creating status file: status.json
+0.662 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.662S new state: Expanded 1.25: SAHSULAND.zip//:SAHSU_GRD_Level3.shp as file 2423 to list; size: 1.31MB; code: 200
Re-creating status file: status.json
+0.667 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.667S new state: Expanded 1.26: SAHSULAND.zip//:SAHSU_GRD_Level3.shp.xml as file 2524 to list; size: 0.99KB; code: 200
Re-creating status file: status.json
+0.671 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.671S new state: Expanded 1.27: SAHSULAND.zip//:SAHSU_GRD_Level3.shx as file 2625 to list; size: 1.66KB; code: 200
Re-creating status file: status.json
+0.764 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.764S new state: Expanded 1.28: SAHSULAND.zip//:SAHSU_GRD_Level4.csv as file 2726 to list; size: 4.98MB; code: 200
Re-creating status file: status.json
+0.769 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.769S new state: Expanded 1.29: SAHSULAND.zip//:SAHSU_GRD_Level4.dbf as file 2827 to list; size: 73.46KB; code: 200
Re-creating status file: status.json
+0.773 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.773S new state: Expanded 1.30: SAHSULAND.zip//:SAHSU_GRD_Level4.prj as file 2928 to list; size: 0.41KB; code: 200
Re-creating status file: status.json
+0.777 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.777S new state: Expanded 1.31: SAHSULAND.zip//:SAHSU_GRD_Level4.sbn as file 3029 to list; size: 12.03KB; code: 200
Re-creating status file: status.json
+0.78 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.78S new state: Expanded 1.32: SAHSULAND.zip//:SAHSU_GRD_Level4.sbx as file 3130 to list; size: 0.53KB; code: 200
Re-creating status file: status.json
+0.842 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.842S new state: Expanded 1.33: SAHSULAND.zip//:SAHSU_GRD_Level4.shp as file 3231 to list; size: 2.14MB; code: 200
Re-creating status file: status.json
+0.845 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.845S new state: Expanded 1.34: SAHSULAND.zip//:SAHSU_GRD_Level4.shp.xml as file 3332 to list; size: 8.15KB; code: 200
Re-creating status file: status.json
+0.849 S addStatus: [nodeGeoSpatialServices:836:zipProcessingSeries()] +0.849S new state: Expanded 1.35: SAHSULAND.zip//:SAHSU_GRD_Level4.shx as file 3433 to list; size: 9.71KB; code: 200
Re-creating status file: status.json
+0.853 S addStatus: [nodeGeoSpatialServices:880:zipProcessingSeriesEnd()] +0.853S new state: Processed zip file 1: SAHSULAND.zip; size: 6.73MB; added: 33 file(s); code: 200
Re-creating status file: status.json
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[0]: SAHSULAND.zip
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[1]: geoDataLoader.xml
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[2]: SAHSU_GRD_Level1.csv
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[3]: SAHSU_GRD_Level1.dbf
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[4]: SAHSU_GRD_Level1.prj
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[5]: SAHSU_GRD_Level1.sbn
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[6]: SAHSU_GRD_Level1.sbx
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[7]: SAHSU_GRD_Level1.shp
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[8]: SAHSU_GRD_Level1.shp.xml
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[9]: SAHSU_GRD_Level1.shx
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[10]: SAHSU_GRD_Level2.csv
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[11]: SAHSU_GRD_Level2.dbf
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[12]: SAHSU_GRD_Level2.prj
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[13]: SAHSU_GRD_Level2.sbn
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[14]: SAHSU_GRD_Level2.sbx
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[15]: SAHSU_GRD_Level2.shp
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[16]: SAHSU_GRD_Level2.shp.xml
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[17]: SAHSU_GRD_Level2.shx
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[18]: SAHSU_GRD_Level3.csv
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[19]: SAHSU_GRD_Level3.dbf
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[20]: SAHSU_GRD_Level3.prj
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[21]: SAHSU_GRD_Level3.sbn
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[22]: SAHSU_GRD_Level3.sbx
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[23]: SAHSU_GRD_Level3.shp
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[24]: SAHSU_GRD_Level3.shp.xml
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[25]: SAHSU_GRD_Level3.shx
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[26]: SAHSU_GRD_Level4.csv
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[27]: SAHSU_GRD_Level4.dbf
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[28]: SAHSU_GRD_Level4.prj
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[29]: SAHSU_GRD_Level4.sbn
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[30]: SAHSU_GRD_Level4.sbx
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[31]: SAHSU_GRD_Level4.shp
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[32]: SAHSU_GRD_Level4.shp.xml
Queued (shapeFileComponentQueue) file for shpConvertFileProcessor[33]: SAHSU_GRD_Level4.shx
Ignore zip file; process contents (loaded as individual files when zip file unpacked): SAHSULAND.zip
mkdir: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSULAND
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSULAND/SAHSULAND.zip; with callback(function): shapeFileComponentQueueCallbackFunc
[0.002s] Wrote 1048576 bytes to file: SAHSULAND.zip; recurse [1] new pos: 1048576; len: 1048576; data.length: 7052628; ok: false

[0.003s] Stream drained: 1 for file: SAHSULAND.zip [1] pos: 1048576; len: 1048576; data.length: 7052628
[0.004s] Stream drained: 2 for file: SAHSULAND.zip [2] pos: 2097152; len: 1048576; data.length: 7052628
[0.005s] Stream drained: 3 for file: SAHSULAND.zip [3] pos: 3145728; len: 1048576; data.length: 7052628
[0.006s] Stream drained: 5 for file: SAHSULAND.zip [5] pos: 5242880; len: 1048576; data.length: 7052628
[0.006s] End write file: SAHSULAND.zip; [7] new pos: 7340032; len: 1048576; data.length: 7052628; lastPiece: true; ok: false; no callback defined
Found embedded dataloader XML configuration file: geoDataLoader.xml
Did not save dataloader XML configuration file: geoDataLoader.xml to C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSULAND
Ignore extension: .csv for file: SAHSU_GRD_Level1.csv
mkdir: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.csv; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level1.csv; [1] new pos: 1048576; len: 1048576; data.length: 466397; lastPiece: true; ok: false; no callback defined
[0.007s] OK Saved file: SAHSULAND.zip; size: 7052628 bytes; 960.84 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasDbf for file: SAHSU_GRD_Level1.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.dbf; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level1.dbf; [1] new pos: 1048576; len: 1048576; data.length: 161; lastPiece: true; ok: true; no callback defined
[0.002s] OK Saved file: SAHSU_GRD_Level1.csv; size: 466397 bytes; 222.4 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasPrj for file: SAHSU_GRD_Level1.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.prj; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level1.prj; [1] new pos: 1048576; len: 1048576; data.length: 417; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level1.dbf; size: 161 bytes; 0.15 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .sbn for file: SAHSU_GRD_Level1.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.sbn; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level1.sbn; [1] new pos: 1048576; len: 1048576; data.length: 132; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level1.prj; size: 417 bytes; 0.4 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .sbx for file: SAHSU_GRD_Level1.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.sbx; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level1.sbx; [1] new pos: 1048576; len: 1048576; data.length: 116; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level1.sbn; size: 132 bytes; 0.13 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasShp for file: SAHSU_GRD_Level1.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.shp; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level1.shp; [1] new pos: 1048576; len: 1048576; data.length: 197664; lastPiece: true; ok: false; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level1.sbx; size: 116 bytes; 0.11 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .shp.xml for file: SAHSU_GRD_Level1.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.shp.xml; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level1.shp.xml; [1] new pos: 1048576; len: 1048576; data.length: 1009; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level1.shp; size: 197664 bytes; 188.51 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .shx for file: SAHSU_GRD_Level1.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.shx; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level1.shx; [1] new pos: 1048576; len: 1048576; data.length: 108; lastPiece: true; ok: true; no callback defined
[0.002s] OK Saved file: SAHSU_GRD_Level1.shp.xml; size: 1009 bytes; 0.48 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .csv for file: SAHSU_GRD_Level2.csv
mkdir: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.csv; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] Wrote 1048576 bytes to file: SAHSU_GRD_Level2.csv; recurse [1] new pos: 1048576; len: 1048576; data.length: 1571365; ok: false
[0.001s] OK Saved file: SAHSU_GRD_Level1.shx; size: 108 bytes; 0.1 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc

[0.002s] Stream drained: 1 for file: SAHSU_GRD_Level2.csv [1] pos: 1048576; len: 1048576; data.length: 1571365
[0.002s] End write file: SAHSU_GRD_Level2.csv; [2] new pos: 2097152; len: 1048576; data.length: 1571365; lastPiece: true; ok: false; no callback defined
hasDbf for file: SAHSU_GRD_Level2.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.dbf; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level2.dbf; [1] new pos: 1048576; len: 1048576; data.length: 1029; lastPiece: true; ok: true; no callback defined
[0.002s] OK Saved file: SAHSU_GRD_Level2.csv; size: 1571365 bytes; 749.29 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasPrj for file: SAHSU_GRD_Level2.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.prj; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level2.prj; [1] new pos: 1048576; len: 1048576; data.length: 417; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level2.dbf; size: 1029 bytes; 0.98 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .sbn for file: SAHSU_GRD_Level2.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.sbn; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level2.sbn; [1] new pos: 1048576; len: 1048576; data.length: 292; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level2.prj; size: 417 bytes; 0.4 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .sbx for file: SAHSU_GRD_Level2.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.sbx; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level2.sbx; [1] new pos: 1048576; len: 1048576; data.length: 132; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level2.sbn; size: 292 bytes; 0.28 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasShp for file: SAHSU_GRD_Level2.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.shp; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level2.shp; [1] new pos: 1048576; len: 1048576; data.length: 666112; lastPiece: true; ok: false; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level2.sbx; size: 132 bytes; 0.13 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .shp.xml for file: SAHSU_GRD_Level2.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.shp.xml; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level2.shp.xml; [1] new pos: 1048576; len: 1048576; data.length: 1009; lastPiece: true; ok: true; no callback defined
[0.002s] OK Saved file: SAHSU_GRD_Level2.shp; size: 666112 bytes; 317.63 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .shx for file: SAHSU_GRD_Level2.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.shx; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level2.shx; [1] new pos: 1048576; len: 1048576; data.length: 236; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level2.shp.xml; size: 1009 bytes; 0.96 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .csv for file: SAHSU_GRD_Level3.csv
mkdir: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.csv; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] Wrote 1048576 bytes to file: SAHSU_GRD_Level3.csv; recurse [1] new pos: 1048576; len: 1048576; data.length: 3227928; ok: false
[0.001s] OK Saved file: SAHSU_GRD_Level2.shx; size: 236 bytes; 0.23 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc

[0.001s] Stream drained: 1 for file: SAHSU_GRD_Level3.csv [1] pos: 1048576; len: 1048576; data.length: 3227928
[0.002s] Stream drained: 3 for file: SAHSU_GRD_Level3.csv [3] pos: 3145728; len: 1048576; data.length: 3227928
[0.002s] End write file: SAHSU_GRD_Level3.csv; [4] new pos: 4194304; len: 1048576; data.length: 3227928; lastPiece: true; ok: false; no callback defined
hasDbf for file: SAHSU_GRD_Level3.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.dbf; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level3.dbf; [1] new pos: 1048576; len: 1048576; data.length: 5930; lastPiece: true; ok: true; no callback defined
[0.002s] OK Saved file: SAHSU_GRD_Level3.csv; size: 3227928 bytes; 1539.2 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasPrj for file: SAHSU_GRD_Level3.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.prj; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level3.prj; [1] new pos: 1048576; len: 1048576; data.length: 417; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level3.dbf; size: 5930 bytes; 5.66 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .sbn for file: SAHSU_GRD_Level3.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.sbn; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level3.sbn; [1] new pos: 1048576; len: 1048576; data.length: 2092; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level3.prj; size: 417 bytes; 0.4 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .sbx for file: SAHSU_GRD_Level3.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.sbx; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level3.sbx; [1] new pos: 1048576; len: 1048576; data.length: 244; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level3.sbn; size: 2092 bytes; 2 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasShp for file: SAHSU_GRD_Level3.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.shp; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] Wrote 1048576 bytes to file: SAHSU_GRD_Level3.shp; recurse [1] new pos: 1048576; len: 1048576; data.length: 1375272; ok: false
[0.001s] OK Saved file: SAHSU_GRD_Level3.sbx; size: 244 bytes; 0.23 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc

[0.001s] Stream drained: 1 for file: SAHSU_GRD_Level3.shp [1] pos: 1048576; len: 1048576; data.length: 1375272
[0.001s] End write file: SAHSU_GRD_Level3.shp; [2] new pos: 2097152; len: 1048576; data.length: 1375272; lastPiece: true; ok: false; no callback defined
Ignore extension: .shp.xml for file: SAHSU_GRD_Level3.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.shp.xml; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level3.shp.xml; [1] new pos: 1048576; len: 1048576; data.length: 1009; lastPiece: true; ok: true; no callback defined
[0.002s] OK Saved file: SAHSU_GRD_Level3.shp; size: 1375272 bytes; 655.78 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .shx for file: SAHSU_GRD_Level3.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.shx; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level3.shx; [1] new pos: 1048576; len: 1048576; data.length: 1700; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level3.shp.xml; size: 1009 bytes; 0.96 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .csv for file: SAHSU_GRD_Level4.csv
mkdir: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.csv; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] Wrote 1048576 bytes to file: SAHSU_GRD_Level4.csv; recurse [1] new pos: 1048576; len: 1048576; data.length: 5219614; ok: false
[0.001s] OK Saved file: SAHSU_GRD_Level3.shx; size: 1700 bytes; 1.62 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc

[0.001s] Stream drained: 1 for file: SAHSU_GRD_Level4.csv [1] pos: 1048576; len: 1048576; data.length: 5219614
[0.002s] Stream drained: 2 for file: SAHSU_GRD_Level4.csv [2] pos: 2097152; len: 1048576; data.length: 5219614
[0.003s] Stream drained: 4 for file: SAHSU_GRD_Level4.csv [4] pos: 4194304; len: 1048576; data.length: 5219614
[0.003s] End write file: SAHSU_GRD_Level4.csv; [5] new pos: 5242880; len: 1048576; data.length: 5219614; lastPiece: true; ok: false; no callback defined
hasDbf for file: SAHSU_GRD_Level4.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.dbf; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level4.dbf; [1] new pos: 1048576; len: 1048576; data.length: 75224; lastPiece: true; ok: false; no callback defined
[0.004s] OK Saved file: SAHSU_GRD_Level4.csv; size: 5219614 bytes; 1244.45 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasPrj for file: SAHSU_GRD_Level4.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.prj; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level4.prj; [1] new pos: 1048576; len: 1048576; data.length: 417; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level4.dbf; size: 75224 bytes; 71.74 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .sbn for file: SAHSU_GRD_Level4.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.sbn; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level4.sbn; [1] new pos: 1048576; len: 1048576; data.length: 12316; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level4.prj; size: 417 bytes; 0.4 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .sbx for file: SAHSU_GRD_Level4.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.sbx; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level4.sbx; [1] new pos: 1048576; len: 1048576; data.length: 540; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level4.sbn; size: 12316 bytes; 11.75 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
hasShp for file: SAHSU_GRD_Level4.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.shp; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] Wrote 1048576 bytes to file: SAHSU_GRD_Level4.shp; recurse [1] new pos: 1048576; len: 1048576; data.length: 2241992; ok: false
[0.001s] OK Saved file: SAHSU_GRD_Level4.sbx; size: 540 bytes; 0.51 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc

[0.001s] Stream drained: 1 for file: SAHSU_GRD_Level4.shp [1] pos: 1048576; len: 1048576; data.length: 2241992
[0.001s] End write file: SAHSU_GRD_Level4.shp; [3] new pos: 3145728; len: 1048576; data.length: 2241992; lastPiece: true; ok: false; no callback defined
Ignore extension: .shp.xml for file: SAHSU_GRD_Level4.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.shp.xml; with callback(function): shapeFileComponentQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level4.shp.xml; [1] new pos: 1048576; len: 1048576; data.length: 8344; lastPiece: true; ok: true; no callback defined
[0.002s] OK Saved file: SAHSU_GRD_Level4.shp; size: 2241992 bytes; 1069.07 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Ignore extension: .shx for file: SAHSU_GRD_Level4.csv
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.shx; with callback(function): shapeFileComponentQueueCallbackFunc
[0s] End write file: SAHSU_GRD_Level4.shx; [1] new pos: 1048576; len: 1048576; data.length: 9940; lastPiece: true; ok: true; no callback defined
[0.001s] OK Saved file: SAHSU_GRD_Level4.shp.xml; size: 8344 bytes; 7.96 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
Processing shapefile [1]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.shp
File [0]; key: desc; areaField: sahsu_grd_level1_desc=Level 1 (top level)
File [0]; key: areaID; areaField: sahsu_grd_level1_areaID=LEVEL1
File [0]; key: areaName; areaField: sahsu_grd_level1_areaName=LEVEL1
File [0]; key: areaID_desc; areaField: sahsu_grd_level1_areaID_desc=Level 1 name
File [0]; key: areaName_desc; areaField: sahsu_grd_level1_areaName_desc=Level 1 ID
shapefileData["areaName"]: LEVEL1
Processing shapefile [2]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.shp
File [1]; key: desc; areaField: sahsu_grd_level2_desc=Level 2
File [1]; key: areaID; areaField: sahsu_grd_level2_areaID=LEVEL2
File [1]; key: areaName; areaField: sahsu_grd_level2_areaName=NAME
File [1]; key: areaID_desc; areaField: sahsu_grd_level2_areaID_desc=Level 2
File [1]; key: areaName_desc; areaField: sahsu_grd_level2_areaName_desc=Level 2 name
shapefileData["areaName"]: NAME
Processing shapefile [3]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.shp
File [2]; key: desc; areaField: sahsu_grd_level3_desc=Level 3
File [2]; key: areaID; areaField: sahsu_grd_level3_areaID=LEVEL3
File [2]; key: areaName; areaField: sahsu_grd_level3_areaName=LEVEL3
File [2]; key: areaID_desc; areaField: sahsu_grd_level3_areaID_desc=Level 3
File [2]; key: areaName_desc; areaField: sahsu_grd_level3_areaName_desc=Level 3 ID
shapefileData["areaName"]: LEVEL3
Processing shapefile [4]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.shp
File [3]; key: desc; areaField: sahsu_grd_level4_desc=Level 4
File [3]; key: areaID; areaField: sahsu_grd_level4_areaID=LEVEL4
File [3]; key: areaName; areaField: sahsu_grd_level4_areaName=LEVEL4
File [3]; key: areaID_desc; areaField: sahsu_grd_level4_areaID_desc=Level 4
File [3]; key: areaName_desc; areaField: sahsu_grd_level4_areaName_desc=Level 4 ID
shapefileData["areaName"]: LEVEL4

[0.001s] OK Saved file: SAHSU_GRD_Level4.shx; size: 9940 bytes; 9.48 MB/S;
Run callback(function): shapeFileComponentQueueCallbackFunc
async.queue() for write shapefile [1]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.shp
shapefile read header for: SAHSU_GRD_Level1.shp
+0.265S; Reading shapefile record 1 from: SAHSU_GRD_Level1.shp; current size: 465.58KB
+0.346S; Read shapefile: SAHSU_GRD_Level1.shp; size: 465.58KB; 1 records
+1.835 S addStatus: [shpConvert:921:readerClose()] +1.835S new state: Read shapefile: SAHSU_GRD_Level1.shp; size: 465.58KB; 1 records; code: 200
Re-creating status file: status.json
File: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.shp; mixedCase fields: 2
Total time to process shapefile: 0.346 S
Bounding box [xmin: -7.546294213762522, ymin: 52.66328217367452, xmax: -5.036247065967251, ymax: 55.56628680300059];
AreaName: LEVEL1
properties[0]: {"LEVEL1":"01","GID":1,"AREAID":"01","AREANAME":"01","AREA_KM2":32747.361189961535,"GEOGRAPHIC_CENTROID_WKT":"POINT (-6.300970153620721 54.180312508254346)","ID":0,"AREA":32857211853.1}
Projection name: OSGB_1936_British_National_Grid; srid: 27700; proj4: +proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +datum=OSGB36 +units=m +no_defs
8 fields: ["ID","LEVEL1","AREA","GID","AREAID","AREANAME","AREA_KM2","GEOGRAPHIC_CENTROID_WKT"]; areas: 1; processed: 3 records/S
Completed processing shapefile[1]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.shp
Write header for: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.json
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.json; (no callback)
Write final feature at index: 0/1; feature length: 484661
+1.852 S addStatus: [shpConvert:1160:writeGeoJsonbyFeatureSeries()] +1.852S new state: Saved JSON feature: 1/1; code: 200
Re-creating status file: status.json;
Run callback(function): seriesCallbackFunc
Write footer for: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.json
[0.017s] End write file: SAHSU_GRD_Level1.json; [1] new pos: 1048576; len: 1048576; data.length: 2; lastPiece: true; ok: false;
Run callback(function): topoFunctionSaved JSON file: SAHSU_GRD_Level1.json; took: 0.017S
+1.857 S addStatus: [shpConvert:1288:topoFunction()] +1.857S new state: Saved JSON file: SAHSU_GRD_Level1.json; code: 200
Re-creating status file: status.json
[0.017s] OK Saved file: SAHSU_GRD_Level1.json; size: 484786 bytes; 27.2 MB/S; 59 records/S; (no callback)
Zoomlevel: 11; default topoJSON options: {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000
}; property-transform enabled: undefined
Created topojson for zoomlevel: 11; size: 348374; took: 0.028 S;  diagnostics
bounds: -7.5882943759077435 52.687535768620904 -4.8865378605813286 55.526809797636744 (spherical)
pre-quantization: 0.300m (0.00000270°) 0.316m (0.00000284°)
topology: 2 arcs, 12344 points

SAHSU_GRD_Level1: simplified topojson for zoomlevel 11 took: 0.028S
+1.9 S addStatus: [simplifyGeoJSON:335:toTopoJSON()] +1.9S new state: SAHSU_GRD_Level1: simplified topojson for zoomlevel 11; code: 200
Re-creating status file: status.json
Zoomlevel: 10; using simplification factor: 0.75; property-transform enabled; topoJSON options pre transform: {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000,
    "coordinate-system": "spherical",
    "retain-proportion": "0.75"
}
+0 S; SAHSU_GRD_Level1: created geojson for zoomlevel 10 from zoomlevel topojson: 11
Created geojson (1 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level1; took: 0.02S
+1.927 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +1.927S new state: Created geojson (1 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
+0.02 S; SAHSU_GRD_Level1: clone topojson : 11
Created geojson (1 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level1; took: 0.087S
+1.994 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +1.994S new state: Created geojson (1 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[10] geojson size: 348148 created from zoomlevel: 11
TopoJSON options post transform for zoomlevel[10] {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000,
    "coordinate-system": "spherical",
    "retain-proportion": "0.75",
    "minimum-area": 6.297212007258241e-12
}
Created topojson for zoomlevel[10]; size: 257508; took: 0.214S;  diagnostics
simplification: effective minimum area 6.30e-12
simplification: retained 9259 / 12344 points (75%)

Topojson has 1 features with 8 properties
SAHSU_GRD_Level1: simplified topojson for zoomlevel: 10; took: 0.214S
+2.128 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +2.128S new state: SAHSU_GRD_Level1: simplified topojson for zoomlevel: 10; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level1: created geojson for zoomlevel 9 from zoomlevel topojson: 10
Created geojson (1 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level1; took: 0.008S
+2.14 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +2.14S new state: Created geojson (1 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
+0.008 S; SAHSU_GRD_Level1: clone topojson : 10
Created geojson (1 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level1; took: 0.063S
+2.194 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +2.194S new state: Created geojson (1 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[9] geojson size: 257282 created from zoomlevel: 10
Created topojson for zoomlevel[9]; size: 192740; took: 0.136S;  diagnostics
simplification: effective minimum area 2.05e-11
simplification: retained 6945 / 9259 points (75%)

Topojson has 1 features with 8 properties
SAHSU_GRD_Level1: simplified topojson for zoomlevel: 9; took: 0.136S
+2.273 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +2.273S new state: SAHSU_GRD_Level1: simplified topojson for zoomlevel: 9; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level1: created geojson for zoomlevel 8 from zoomlevel topojson: 9
Created geojson (1 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level1; took: 0.006S
+2.282 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +2.282S new state: Created geojson (1 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
+0.006 S; SAHSU_GRD_Level1: clone topojson : 9
Created geojson (1 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level1; took: 0.042S
+2.318 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +2.318S new state: Created geojson (1 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[8] geojson size: 192514 created from zoomlevel: 9
Created topojson for zoomlevel[8]; size: 144168; took: 0.098S;  diagnostics
simplification: effective minimum area 5.04e-11
simplification: retained 5209 / 6945 points (75%)

Topojson has 1 features with 8 properties
SAHSU_GRD_Level1: simplified topojson for zoomlevel: 8; took: 0.098S
+2.379 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +2.379S new state: SAHSU_GRD_Level1: simplified topojson for zoomlevel: 8; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level1: created geojson for zoomlevel 7 from zoomlevel topojson: 8
Created geojson (1 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level1; took: 0.005S
+2.386 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +2.386S new state: Created geojson (1 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
+0.005 S; SAHSU_GRD_Level1: clone topojson : 8
Created geojson (1 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level1; took: 0.032S
+2.413 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +2.413S new state: Created geojson (1 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[7] geojson size: 143942 created from zoomlevel: 8
Created topojson for zoomlevel[7]; size: 107736; took: 0.074S;  diagnostics
simplification: effective minimum area 1.16e-10
simplification: retained 3907 / 5209 points (75%)

Topojson has 1 features with 8 properties
SAHSU_GRD_Level1: simplified topojson for zoomlevel: 7; took: 0.074S
+2.459 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +2.459S new state: SAHSU_GRD_Level1: simplified topojson for zoomlevel: 7; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level1: created geojson for zoomlevel 6 from zoomlevel topojson: 7
Created geojson (1 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level1; took: 0.003S
+2.466 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +2.466S new state: Created geojson (1 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
+0.003 S; SAHSU_GRD_Level1: clone topojson : 7
Created geojson (1 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level1; took: 0.023S
+2.486 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +2.486S new state: Created geojson (1 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level1; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[6] geojson size: 107510 created from zoomlevel: 7
Created topojson for zoomlevel[6]; size: 80416; took: 0.051S;  diagnostics
simplification: effective minimum area 2.60e-10
simplification: retained 2931 / 3907 points (75%)

Topojson has 1 features with 8 properties
SAHSU_GRD_Level1: simplified topojson for zoomlevel: 6; took: 0.051S
+2.515 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +2.515S new state: SAHSU_GRD_Level1: simplified topojson for zoomlevel: 6; code: 200
Re-creating status file: status.json
+0.001 S; SAHSU_GRD_Level1: created geojson for zoomlevel 6 from zoomlevel topojson: 6
SAHSU_GRD_Level1: simplified topojson for zoomlevels 6 to 11; took: 0.614S
+2.519 S addStatus: [simplifyGeoJSON:729:seriesEndCallbackFunc()] +2.519S new state: SAHSU_GRD_Level1: simplified topojson for zoomlevels 6 to 11; code: 200
Re-creating status file: status.json
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level1/SAHSU_GRD_Level1.topojson; with callback(function): shapeFileQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level1.topojson; [1] new pos: 1048576; len: 1048576; data.length: 128371; lastPiece: true; ok: false; no callback definedTopoJSON creation and save: SAHSU_GRD_Level1.topojson; took: 0.69S
+2.53 S addStatus: [shpConvert:1263:shapeFileQueueCallbackFunc()] +2.53S new state: TopoJSON creation and save: SAHSU_GRD_Level1.topojson; code: 200
Re-creating status file: status.json
[0.001s] OK Saved file: SAHSU_GRD_Level1.topojson; size: 128371 bytes; 122.42 MB/S; 1000 records/S;
Run callback(function): shapeFileQueueCallbackFunc;
Run shapeFileQueueCallback callback()
async.queue() for write shapefile [2]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.shp
shapefile read header for: SAHSU_GRD_Level2.shp
+0.121S; Reading shapefile record 1 from: SAHSU_GRD_Level2.shp; current size: 79.79KB
+0.308S; Read shapefile: SAHSU_GRD_Level2.shp; size: 1.54MB; 17 records
+2.841 S addStatus: [shpConvert:921:readerClose()] +2.841S new state: Read shapefile: SAHSU_GRD_Level2.shp; size: 1.54MB; 17 records; code: 200
Re-creating status file: status.json
File: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.shp; mixedCase fields: 2
Total time to process shapefile: 0.308 S
Bounding box [xmin: -7.54629421388166, ymin: 52.663282173668904, xmax: -5.03624706583906, ymax: 55.56628680300376];
AreaName: NAME
properties[0]: {"LEVEL2":"01.001","LEVEL1":"01","GID":1,"AREAID":"01.001","AREANAME":"Abellan","AREA_KM2":1792.5530769748484,"GEOGRAPHIC_CENTROID_WKT":"POINT (-6.364478031888486 55.184610891371605)","AREA":1798764570.1,"NAME":"Abellan"}
Projection name: OSGB_1936_British_National_Grid; srid: 27700; proj4: +proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +datum=OSGB36 +units=m +no_defs
9 fields: ["LEVEL2","AREA","LEVEL1","NAME","GID","AREAID","AREANAME","AREA_KM2","GEOGRAPHIC_CENTROID_WKT"]; areas: 17; processed: 55 records/S
Completed processing shapefile[2]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.shp
Write header for: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.json
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.json; (no callback)
Write final feature at index: 16/17; feature length: 1635598
+2.883 S addStatus: [shpConvert:1160:writeGeoJsonbyFeatureSeries()] +2.883S new state: Saved JSON feature: 17/17; code: 200
Re-creating status file: status.json
[0.034s] Wrote 1048576 bytes to file: SAHSU_GRD_Level2.json; recurse [1] new pos: 1048576; len: 1048576; data.length: 1635598; ok: false

[0.034s] Stream drained: 1 for file: SAHSU_GRD_Level2.json [1] pos: 1048576; len: 1048576; data.length: 1635598;
Run callback(function): seriesCallbackFunc
Write footer for: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.json
[0.035s] End write file: SAHSU_GRD_Level2.json; [1] new pos: 1048576; len: 1048576; data.length: 2; lastPiece: true; ok: false;
Run callback(function): topoFunctionSaved JSON file: SAHSU_GRD_Level2.json; took: 0.035S
+2.891 S addStatus: [shpConvert:1288:topoFunction()] +2.891S new state: Saved JSON file: SAHSU_GRD_Level2.json; code: 200
Re-creating status file: status.json
[0.036s] OK Saved file: SAHSU_GRD_Level2.json; size: 1635722 bytes; 43.33 MB/S; 472 records/S; (no callback)
Zoomlevel: 11; default topoJSON options: {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000
}; property-transform enabled: undefined
Created topojson for zoomlevel: 11; size: 705692; took: 0.09 S;  diagnostics
bounds: -7.588294376046162 52.68753576860153 -4.886537860470895 55.526809797680684 (spherical)
pre-quantization: 0.300m (0.00000270°) 0.316m (0.00000284°)
topology: 52 arcs, 26999 points

SAHSU_GRD_Level2: simplified topojson for zoomlevel 11 took: 0.09S
+3.014 S addStatus: [simplifyGeoJSON:335:toTopoJSON()] +3.014S new state: SAHSU_GRD_Level2: simplified topojson for zoomlevel 11; code: 200
Re-creating status file: status.json
Zoomlevel: 10; using simplification factor: 0.75; property-transform enabled; topoJSON options pre transform: {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000,
    "coordinate-system": "spherical",
    "retain-proportion": "0.75"
}
+0 S; SAHSU_GRD_Level2: created geojson for zoomlevel 10 from zoomlevel topojson: 11
Created geojson (17 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level2; took: 0.039S
+3.057 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +3.057S new state: Created geojson (17 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
+0.039 S; SAHSU_GRD_Level2: clone topojson : 11
Created geojson (17 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level2; took: 0.183S
+3.201 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +3.201S new state: Created geojson (17 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[10] geojson size: 1132890 created from zoomlevel: 11
TopoJSON options post transform for zoomlevel[10] {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000,
    "coordinate-system": "spherical",
    "retain-proportion": "0.75",
    "minimum-area": 4.523415051688943e-12
}
Created topojson for zoomlevel[10]; size: 527004; took: 0.33S;  diagnostics
simplification: effective minimum area 4.52e-12
simplification: retained 20250 / 26999 points (75%)

Topojson has 17 features with 9 properties
SAHSU_GRD_Level2: simplified topojson for zoomlevel: 10; took: 0.33S
+3.365 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +3.365S new state: SAHSU_GRD_Level2: simplified topojson for zoomlevel: 10; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level2: created geojson for zoomlevel 9 from zoomlevel topojson: 10
Created geojson (17 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level2; took: 0.026S
+3.395 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +3.395S new state: Created geojson (17 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
+0.026 S; SAHSU_GRD_Level2: clone topojson : 10
Created geojson (17 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level2; took: 0.134S
+3.502 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +3.502S new state: Created geojson (17 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[9] geojson size: 827612 created from zoomlevel: 10
Created topojson for zoomlevel[9]; size: 393406; took: 0.248S;  diagnostics
simplification: effective minimum area 1.62e-11
simplification: retained 15188 / 20250 points (75%)

Topojson has 17 features with 9 properties
SAHSU_GRD_Level2: simplified topojson for zoomlevel: 9; took: 0.248S
+3.633 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +3.633S new state: SAHSU_GRD_Level2: simplified topojson for zoomlevel: 9; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level2: created geojson for zoomlevel 8 from zoomlevel topojson: 9
Created geojson (17 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level2; took: 0.031S
+3.667 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +3.667S new state: Created geojson (17 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
+0.031 S; SAHSU_GRD_Level2: clone topojson : 9
Created geojson (17 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level2; took: 0.127S
+3.763 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +3.763S new state: Created geojson (17 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[8] geojson size: 610456 created from zoomlevel: 9
Created topojson for zoomlevel[8]; size: 294934; took: 0.212S;  diagnostics
simplification: effective minimum area 4.04e-11
simplification: retained 11392 / 15188 points (75%)

Topojson has 17 features with 9 properties
SAHSU_GRD_Level2: simplified topojson for zoomlevel: 8; took: 0.212S
+3.857 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +3.857S new state: SAHSU_GRD_Level2: simplified topojson for zoomlevel: 8; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level2: created geojson for zoomlevel 7 from zoomlevel topojson: 8
Created geojson (17 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level2; took: 0.016S
+3.881 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +3.881S new state: Created geojson (17 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
+0.016 S; SAHSU_GRD_Level2: clone topojson : 8
Created geojson (17 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level2; took: 0.076S
+3.941 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +3.941S new state: Created geojson (17 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[7] geojson size: 452178 created from zoomlevel: 8
Created topojson for zoomlevel[7]; size: 221252; took: 0.142S;  diagnostics
simplification: effective minimum area 9.33e-11
simplification: retained 8545 / 11392 points (75%)

Topojson has 17 features with 9 properties
SAHSU_GRD_Level2: simplified topojson for zoomlevel: 7; took: 0.142S
+4.015 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +4.015S new state: SAHSU_GRD_Level2: simplified topojson for zoomlevel: 7; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level2: created geojson for zoomlevel 6 from zoomlevel topojson: 7
Created geojson (17 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level2; took: 0.014S
+4.033 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +4.033S new state: Created geojson (17 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
+0.014 S; SAHSU_GRD_Level2: clone topojson : 7
Created geojson (17 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level2; took: 0.059S
+4.079 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +4.079S new state: Created geojson (17 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level2; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[6] geojson size: 336062 created from zoomlevel: 7
Created topojson for zoomlevel[6]; size: 166150; took: 0.107S;  diagnostics
simplification: effective minimum area 2.06e-10
simplification: retained 6409 / 8545 points (75%)

Topojson has 17 features with 9 properties
SAHSU_GRD_Level2: simplified topojson for zoomlevel: 6; took: 0.107S
+4.131 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +4.131S new state: SAHSU_GRD_Level2: simplified topojson for zoomlevel: 6; code: 200
Re-creating status file: status.json
+0.002 S; SAHSU_GRD_Level2: created geojson for zoomlevel 6 from zoomlevel topojson: 6
SAHSU_GRD_Level2: simplified topojson for zoomlevels 6 to 11; took: 1.118S
+4.136 S addStatus: [simplifyGeoJSON:729:seriesEndCallbackFunc()] +4.136S new state: SAHSU_GRD_Level2: simplified topojson for zoomlevels 6 to 11; code: 200
Re-creating status file: status.json
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level2/SAHSU_GRD_Level2.topojson; with callback(function): shapeFileQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level2.topojson; [1] new pos: 1048576; len: 1048576; data.length: 287261; lastPiece: true; ok: false; no callback definedTopoJSON creation and save: SAHSU_GRD_Level2.topojson; took: 1.297S
+4.152 S addStatus: [shpConvert:1263:shapeFileQueueCallbackFunc()] +4.152S new state: TopoJSON creation and save: SAHSU_GRD_Level2.topojson; code: 200
Re-creating status file: status.json
[0.002s] OK Saved file: SAHSU_GRD_Level2.topojson; size: 287261 bytes; 136.98 MB/S; 8500 records/S;
Run callback(function): shapeFileQueueCallbackFunc;
Run shapeFileQueueCallback callback()
async.queue() for write shapefile [3]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.shp
shapefile read header for: SAHSU_GRD_Level3.shp
+0.103S; Reading shapefile record 1 from: SAHSU_GRD_Level3.shp; current size: 40.11KB
+0.636S; Read shapefile: SAHSU_GRD_Level3.shp; size: 3.18MB; 200 records
+4.792 S addStatus: [shpConvert:921:readerClose()] +4.792S new state: Read shapefile: SAHSU_GRD_Level3.shp; size: 3.18MB; 200 records; code: 200
Re-creating status file: status.json
File: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.shp; mixedCase fields: 0
Total time to process shapefile: 0.636 S
Bounding box [xmin: -7.546294213997579, ymin: 52.66328217366346, xmax: -5.036247066220161, ymax: 55.56628680299431];
AreaName: LEVEL3
properties[0]: {"LEVEL2":"01.001","LEVEL1":"01","LEVEL3":"01.001.000100","GID":1,"AREAID":"01.001.000100","AREANAME":"01.001.000100","AREA_KM2":263.0064047533162,"GEOGRAPHIC_CENTROID_WKT":"POINT (-6.556249861423061 55.136075859836474)"}
Projection name: OSGB_1936_British_National_Grid; srid: 27700; proj4: +proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +datum=OSGB36 +units=m +no_defs
8 fields: ["LEVEL2","LEVEL1","LEVEL3","GID","AREAID","AREANAME","AREA_KM2","GEOGRAPHIC_CENTROID_WKT"]; areas: 200; processed: 314 records/S
Completed processing shapefile[3]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.shp
Write header for: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.json
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.json; (no callback)
Write final feature at index: 199/200; feature length: 3404139
+4.874 S addStatus: [shpConvert:1160:writeGeoJsonbyFeatureSeries()] +4.874S new state: Saved JSON feature: 200/200; code: 200
Re-creating status file: status.json
[0.101s] Wrote 1048576 bytes to file: SAHSU_GRD_Level3.json; recurse [1] new pos: 1048576; len: 1048576; data.length: 3404139; ok: false

[0.102s] Stream drained: 1 for file: SAHSU_GRD_Level3.json [1] pos: 1048576; len: 1048576; data.length: 3404139
[0.103s] Stream drained: 2 for file: SAHSU_GRD_Level3.json [2] pos: 2097152; len: 1048576; data.length: 3404139
[0.105s] Stream drained: 3 for file: SAHSU_GRD_Level3.json [3] pos: 3145728; len: 1048576; data.length: 3404139;
Run callback(function): seriesCallbackFunc
Write footer for: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.json
[0.106s] End write file: SAHSU_GRD_Level3.json; [1] new pos: 1048576; len: 1048576; data.length: 2; lastPiece: true; ok: false;
Run callback(function): topoFunctionSaved JSON file: SAHSU_GRD_Level3.json; took: 0.106S
+4.913 S addStatus: [shpConvert:1288:topoFunction()] +4.913S new state: Saved JSON file: SAHSU_GRD_Level3.json; code: 200
Re-creating status file: status.json
[0.107s] OK Saved file: SAHSU_GRD_Level3.json; size: 3404264 bytes; 30.34 MB/S; 1869 records/S; (no callback)
Zoomlevel: 11; default topoJSON options: {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000
}; property-transform enabled: undefined
Created topojson for zoomlevel: 11; size: 1317756; took: 0.127 S;  diagnostics
bounds: -7.5882943761445585 52.687535768610935 -4.886537860821815 55.52680979762778 (spherical)
pre-quantization: 0.300m (0.00000270°) 0.316m (0.00000284°)
topology: 609 arcs, 49313 points

SAHSU_GRD_Level3: simplified topojson for zoomlevel 11 took: 0.127S
+5.09 S addStatus: [simplifyGeoJSON:335:toTopoJSON()] +5.09S new state: SAHSU_GRD_Level3: simplified topojson for zoomlevel 11; code: 200
Re-creating status file: status.json
Zoomlevel: 10; using simplification factor: 0.75; property-transform enabled; topoJSON options pre transform: {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000,
    "coordinate-system": "spherical",
    "retain-proportion": "0.75"
}
+0 S; SAHSU_GRD_Level3: created geojson for zoomlevel 10 from zoomlevel topojson: 11
Created geojson (200 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level3; took: 0.104S
+5.197 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +5.197S new state: Created geojson (200 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
+0.104 S; SAHSU_GRD_Level3: clone topojson : 11
Created geojson (200 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level3; took: 0.409S
+5.502 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +5.502S new state: Created geojson (200 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[10] geojson size: 2302950 created from zoomlevel: 11
TopoJSON options post transform for zoomlevel[10] {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000,
    "coordinate-system": "spherical",
    "retain-proportion": "0.75",
    "minimum-area": 3.1637342663921945e-12
}
Created topojson for zoomlevel[10]; size: 1007322; took: 0.664S;  diagnostics
simplification: effective minimum area 3.16e-12
simplification: retained 36985 / 49313 points (75%)

Topojson has 200 features with 8 properties
SAHSU_GRD_Level3: simplified topojson for zoomlevel: 10; took: 0.664S
+5.788 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +5.788S new state: SAHSU_GRD_Level3: simplified topojson for zoomlevel: 10; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level3: created geojson for zoomlevel 9 from zoomlevel topojson: 10
Created geojson (200 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level3; took: 0.064S
+5.856 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +5.856S new state: Created geojson (200 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
+0.064 S; SAHSU_GRD_Level3: clone topojson : 10
Created geojson (200 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level3; took: 0.291S
+6.083 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +6.083S new state: Created geojson (200 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[9] geojson size: 1706180 created from zoomlevel: 10
Created topojson for zoomlevel[9]; size: 770900; took: 0.489S;  diagnostics
simplification: effective minimum area 1.45e-11
simplification: retained 27739 / 36985 points (75%)

Topojson has 200 features with 8 properties
SAHSU_GRD_Level3: simplified topojson for zoomlevel: 9; took: 0.489S
+6.304 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +6.304S new state: SAHSU_GRD_Level3: simplified topojson for zoomlevel: 9; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level3: created geojson for zoomlevel 8 from zoomlevel topojson: 9
Created geojson (200 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level3; took: 0.063S
+6.374 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +6.374S new state: Created geojson (200 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
+0.063 S; SAHSU_GRD_Level3: clone topojson : 9
Created geojson (200 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level3; took: 0.244S
+6.556 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +6.556S new state: Created geojson (200 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[8] geojson size: 1278878 created from zoomlevel: 9
Created topojson for zoomlevel[8]; size: 595788; took: 0.384S;  diagnostics
simplification: effective minimum area 3.79e-11
simplification: retained 20805 / 27739 points (75%)

Topojson has 200 features with 8 properties
SAHSU_GRD_Level3: simplified topojson for zoomlevel: 8; took: 0.384S
+6.714 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +6.714S new state: SAHSU_GRD_Level3: simplified topojson for zoomlevel: 8; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level3: created geojson for zoomlevel 7 from zoomlevel topojson: 8
Created geojson (200 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level3; took: 0.034S
+6.754 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +6.754S new state: Created geojson (200 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
+0.034 S; SAHSU_GRD_Level3: clone topojson : 8
Created geojson (200 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level3; took: 0.174S
+6.894 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +6.894S new state: Created geojson (200 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[7] geojson size: 967176 created from zoomlevel: 8
Created topojson for zoomlevel[7]; size: 466154; took: 0.276S;  diagnostics
simplification: effective minimum area 9.08e-11
simplification: retained 15604 / 20805 points (75%)

Topojson has 200 features with 8 properties
SAHSU_GRD_Level3: simplified topojson for zoomlevel: 7; took: 0.276S
+7.01 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +7.01S new state: SAHSU_GRD_Level3: simplified topojson for zoomlevel: 7; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level3: created geojson for zoomlevel 6 from zoomlevel topojson: 7
Created geojson (200 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level3; took: 0.027S
+7.04 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +7.04S new state: Created geojson (200 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
+0.027 S; SAHSU_GRD_Level3: clone topojson : 7
Created geojson (200 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level3; took: 0.122S
+7.135 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +7.135S new state: Created geojson (200 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level3; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[6] geojson size: 735736 created from zoomlevel: 7
Created topojson for zoomlevel[6]; size: 370010; took: 0.201S;  diagnostics
simplification: effective minimum area 2.07e-10
simplification: retained 11704 / 15604 points (75%)

Topojson has 200 features with 8 properties
SAHSU_GRD_Level3: simplified topojson for zoomlevel: 6; took: 0.201S
+7.225 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +7.225S new state: SAHSU_GRD_Level3: simplified topojson for zoomlevel: 6; code: 200
Re-creating status file: status.json
+0.004 S; SAHSU_GRD_Level3: created geojson for zoomlevel 6 from zoomlevel topojson: 6
SAHSU_GRD_Level3: simplified topojson for zoomlevels 6 to 11; took: 2.139S
+7.233 S addStatus: [simplifyGeoJSON:729:seriesEndCallbackFunc()] +7.233S new state: SAHSU_GRD_Level3: simplified topojson for zoomlevels 6 to 11; code: 200
Re-creating status file: status.json
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level3/SAHSU_GRD_Level3.topojson; with callback(function): shapeFileQueueCallbackFunc
[0.001s] End write file: SAHSU_GRD_Level3.topojson; [1] new pos: 1048576; len: 1048576; data.length: 578826; lastPiece: true; ok: false; no callback definedTopoJSON creation and save: SAHSU_GRD_Level3.topojson; took: 2.452S
+7.26 S addStatus: [shpConvert:1263:shapeFileQueueCallbackFunc()] +7.26S new state: TopoJSON creation and save: SAHSU_GRD_Level3.topojson; code: 200
Re-creating status file: status.json
[0.002s] OK Saved file: SAHSU_GRD_Level3.topojson; size: 578826 bytes; 276.01 MB/S; 100000 records/S;
Run callback(function): shapeFileQueueCallbackFunc;
Run shapeFileQueueCallback callback()
async.queue() for write shapefile [4]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.shp
shapefile read header for: SAHSU_GRD_Level4.shp
+0.117S; Reading shapefile record 1 from: SAHSU_GRD_Level4.shp; current size: 41.36KB
+8.385 S addStatus: [shpConvert:757:shapefileDataReadNextRecord()] +8.385S new state: Reading shapefile record 819 from: SAHSU_GRD_Level4.shp; current size: 2.57MB; code: 200
Re-creating status file: status.json
+1.119S; Reading shapefile record 819 from: SAHSU_GRD_Level4.shp; current size: 2.57MB
+1.119S; Reading shapefile record 1000 from: SAHSU_GRD_Level4.shp; current size: 3.56MB
+1.802S; Read shapefile: SAHSU_GRD_Level4.shp; size: 5.24MB; 1230 records
+9.068 S addStatus: [shpConvert:921:readerClose()] +9.068S new state: Read shapefile: SAHSU_GRD_Level4.shp; size: 5.24MB; 1230 records; code: 200
Re-creating status file: status.json
File: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.shp; mixedCase fields: 0
Total time to process shapefile: 1.802 S
Bounding box [xmin: -7.546294213997579, ymin: 52.66328217366346, xmax: -5.036247066220161, ymax: 55.56628680299431];
AreaName: LEVEL4
properties[0]: {"PERIMETER":100711.236326,"LEVEL4":"01.001.000100.1","LEVEL2":"01.001","LEVEL1":"01","LEVEL3":"01.001.000100","GID":1,"AREAID":"01.001.000100.1","AREANAME":"01.001.000100.1","AREA_KM2":237.53581861435464,"GEOGRAPHIC_CENTROID_WKT":"POINT (-6.5577278088911575 55.133979832408436)"}
Projection name: OSGB_1936_British_National_Grid; srid: 27700; proj4: +proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +datum=OSGB36 +units=m +no_defs
10 fields: ["PERIMETER","LEVEL4","LEVEL2","LEVEL1","LEVEL3","GID","AREAID","AREANAME","AREA_KM2","GEOGRAPHIC_CENTROID_WKT"]; areas: 1230; processed: 683 records/S
Completed processing shapefile[4]: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.shp
Write header for: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.json
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.json; (no callback)
Write final feature at index: 1229/1230; feature length: 5770737
+9.197 S addStatus: [shpConvert:1160:writeGeoJsonbyFeatureSeries()] +9.197S new state: Saved JSON feature: 1230/1230; code: 200
Re-creating status file: status.json
[0.121s] Wrote 1048576 bytes to file: SAHSU_GRD_Level4.json; recurse [1] new pos: 1048576; len: 1048576; data.length: 5770737; ok: false

[0.121s] Stream drained: 1 for file: SAHSU_GRD_Level4.json [1] pos: 1048576; len: 1048576; data.length: 5770737
[0.123s] Stream drained: 2 for file: SAHSU_GRD_Level4.json [2] pos: 2097152; len: 1048576; data.length: 5770737
[0.125s] Stream drained: 3 for file: SAHSU_GRD_Level4.json [3] pos: 3145728; len: 1048576; data.length: 5770737
[0.127s] Stream drained: 4 for file: SAHSU_GRD_Level4.json [4] pos: 4194304; len: 1048576; data.length: 5770737
[0.129s] Stream drained: 5 for file: SAHSU_GRD_Level4.json [5] pos: 5242880; len: 1048576; data.length: 5770737;
Run callback(function): seriesCallbackFunc
Write footer for: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.json
[0.13s] End write file: SAHSU_GRD_Level4.json; [1] new pos: 1048576; len: 1048576; data.length: 2; lastPiece: true; ok: false;
Run callback(function): topoFunctionSaved JSON file: SAHSU_GRD_Level4.json; took: 0.13S
+9.218 S addStatus: [shpConvert:1288:topoFunction()] +9.218S new state: Saved JSON file: SAHSU_GRD_Level4.json; code: 200
Re-creating status file: status.json
[0.131s] OK Saved file: SAHSU_GRD_Level4.json; size: 5770862 bytes; 42.01 MB/S; 9389 records/S; (no callback)
Zoomlevel: 11; default topoJSON options: {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000
}; property-transform enabled: undefined
Created topojson for zoomlevel: 11; size: 2501360; took: 0.187 S;  diagnostics
bounds: -7.5882943761445585 52.687535768610935 -4.886537860821815 55.52680979762778 (spherical)
pre-quantization: 0.300m (0.00000270°) 0.316m (0.00000284°)
topology: 3277 arcs, 76737 points

SAHSU_GRD_Level4: simplified topojson for zoomlevel 11 took: 0.187S
+9.486 S addStatus: [simplifyGeoJSON:335:toTopoJSON()] +9.486S new state: SAHSU_GRD_Level4: simplified topojson for zoomlevel 11; code: 200
Re-creating status file: status.json
Zoomlevel: 10; using simplification factor: 0.75; property-transform enabled; topoJSON options pre transform: {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000,
    "coordinate-system": "spherical",
    "retain-proportion": "0.75"
}
+0 S; SAHSU_GRD_Level4: created geojson for zoomlevel 10 from zoomlevel topojson: 11
Created geojson (1230 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level4; took: 0.191S
+9.687 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +9.687S new state: Created geojson (1230 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
+0.191 S; SAHSU_GRD_Level4: clone topojson : 11
Created geojson (1230 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level4; took: 0.698S
+10.194 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +10.194S new state: Created geojson (1230 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[10] geojson size: 4042596 created from zoomlevel: 11
TopoJSON options post transform for zoomlevel[10] {
    "verbose": true,
    "pre-quantization": 1000000,
    "post-quantization": 1000000,
    "coordinate-system": "spherical",
    "retain-proportion": "0.75",
    "minimum-area": 2.3556467328624354e-12
}
Created topojson for zoomlevel[10]; size: 2038098; took: 1.052S;  diagnostics
simplification: effective minimum area 2.36e-12
simplification: retained 57553 / 76737 points (75%)

Topojson has 1230 features with 10 properties
SAHSU_GRD_Level4: simplified topojson for zoomlevel: 10; took: 1.052S
+10.603 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +10.603S new state: SAHSU_GRD_Level4: simplified topojson for zoomlevel: 10; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level4: created geojson for zoomlevel 9 from zoomlevel topojson: 10
Created geojson (1230 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level4; took: 0.132S
+10.744 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +10.744S new state: Created geojson (1230 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
+0.132 S; SAHSU_GRD_Level4: clone topojson : 10
Created geojson (1230 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level4; took: 0.525S
+11.137 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +11.137S new state: Created geojson (1230 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[9] geojson size: 3117632 created from zoomlevel: 10
Created topojson for zoomlevel[9]; size: 1680808; took: 0.8S;  diagnostics
simplification: effective minimum area 1.41e-11
simplification: retained 43165 / 57553 points (75%)

Topojson has 1230 features with 10 properties
SAHSU_GRD_Level4: simplified topojson for zoomlevel: 9; took: 0.8S
+11.454 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +11.454S new state: SAHSU_GRD_Level4: simplified topojson for zoomlevel: 9; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level4: created geojson for zoomlevel 8 from zoomlevel topojson: 9
Created geojson (1230 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level4; took: 0.086S
+11.544 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +11.544S new state: Created geojson (1230 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
+0.086 S; SAHSU_GRD_Level4: clone topojson : 9
Created geojson (1230 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level4; took: 0.384S
+11.841 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +11.841S new state: Created geojson (1230 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[8] geojson size: 2443032 created from zoomlevel: 9
Created topojson for zoomlevel[8]; size: 1417924; took: 0.58S;  diagnostics
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865979788(228mS); size: 137922; calls: 11; index: 108; statii: 113
simplification: effective minimum area 4.01e-11
simplification: retained 32374 / 43165 points (75%)

Topojson has 1230 features with 10 properties
SAHSU_GRD_Level4: simplified topojson for zoomlevel: 8; took: 0.58S
+12.07 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +12.07S new state: SAHSU_GRD_Level4: simplified topojson for zoomlevel: 8; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level4: created geojson for zoomlevel 7 from zoomlevel topojson: 8
Created geojson (1230 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level4; took: 0.074S
+12.147 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +12.147S new state: Created geojson (1230 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
+0.074 S; SAHSU_GRD_Level4: clone topojson : 8
Created geojson (1230 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level4; took: 0.387S
+12.46 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +12.46S new state: Created geojson (1230 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[7] geojson size: 1947192 created from zoomlevel: 8
Created topojson for zoomlevel[7]; size: 1222932; took: 0.654S;  diagnostics
simplification: effective minimum area 1.05e-10
simplification: retained 24281 / 32374 points (75%)

Topojson has 1230 features with 10 properties
SAHSU_GRD_Level4: simplified topojson for zoomlevel: 7; took: 0.654S
+12.756 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +12.756S new state: SAHSU_GRD_Level4: simplified topojson for zoomlevel: 7; code: 200
Re-creating status file: status.json
+0 S; SAHSU_GRD_Level4: created geojson for zoomlevel 6 from zoomlevel topojson: 7
Created geojson (1230 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level4; took: 0.06S
+12.82 S addStatus: [simplifyGeoJSON:453:toTopoJSONZoomlevel()] +12.82S new state: Created geojson (1230 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
+0.06 S; SAHSU_GRD_Level4: clone topojson : 7
Created geojson (1230 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level4; took: 0.277S
+13.037 S addStatus: [simplifyGeoJSON:531:cloneTopoJSON()] +13.037S new state: Created geojson (1230 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level4; code: 200
Re-creating status file: status.json
Create topojson zoomlevel[6] geojson size: 1579920 created from zoomlevel: 7
Created topojson for zoomlevel[6]; size: 1077370; took: 0.378S;  diagnostics
getStatus() uuidV1: 66d8a532-bb2c-4304-8e2b-ffde330b88fa; lstart: 1527865981026(186mS); size: 144201; calls: 12; index: 113; statii: 119
simplification: effective minimum area 2.89e-10
simplification: retained 18211 / 24281 points (75%)

Topojson has 1230 features with 10 properties
SAHSU_GRD_Level4: simplified topojson for zoomlevel: 6; took: 0.378S
+13.16 S addStatus: [simplifyGeoJSON:622:simplifyTopoJSON()] +13.16S new state: SAHSU_GRD_Level4: simplified topojson for zoomlevel: 6; code: 200
Re-creating status file: status.json
+0.011 S; SAHSU_GRD_Level4: created geojson for zoomlevel 6 from zoomlevel topojson: 6
SAHSU_GRD_Level4: simplified topojson for zoomlevels 6 to 11; took: 3.679S
+13.174 S addStatus: [simplifyGeoJSON:729:seriesEndCallbackFunc()] +13.174S new state: SAHSU_GRD_Level4: simplified topojson for zoomlevels 6 to 11; code: 200
Re-creating status file: status.json
Created stream for file: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/SAHSU_GRD_Level4/SAHSU_GRD_Level4.topojson; with callback(function): shapeFileQueueCallbackFunc
[0.002s] Wrote 1048576 bytes to file: SAHSU_GRD_Level4.topojson; recurse [1] new pos: 1048576; len: 1048576; data.length: 1256149; ok: false

[0.003s] Stream drained: 1 for file: SAHSU_GRD_Level4.topojson [1] pos: 1048576; len: 1048576; data.length: 1256149
[0.003s] End write file: SAHSU_GRD_Level4.topojson; [2] new pos: 2097152; len: 1048576; data.length: 1256149; lastPiece: true; ok: false; no callback definedTopoJSON creation and save: SAHSU_GRD_Level4.topojson; took: 4.138S
+13.227 S addStatus: [shpConvert:1263:shapeFileQueueCallbackFunc()] +13.227S new state: TopoJSON creation and save: SAHSU_GRD_Level4.topojson; code: 200
Re-creating status file: status.json
[0.003s] OK Saved file: SAHSU_GRD_Level4.topojson; size: 1256149 bytes; 399.32 MB/S; 410000 records/S;
Run callback(function): shapeFileQueueCallbackFunc;
Run shapeFileQueueCallback callback()
+13.301 S addStatus: [shpConvert:1958:shapeFileQueueDrain()] +13.301S new state: Cloned intermediate response; code: 200
Re-creating status file: status.json
All 4 shapefiles have been processed; errors: 0
All bounding boxes are the same
Shape file [0]: SAHSU_GRD_Level1.shp; areas: 1; points: 12346; geolevel: 1 [Default comparison area: SAHSU_GRD_LEVEL1]; topojson has 1 features with 8 properties
Processing topojsonGeometries: 1
File [0] SAHSU_GRD_Level1; topojson zoomlevels: 6; from: 6 to: 11
Shape file [1]: SAHSU_GRD_Level2.shp; areas: 17; points: 118998; geolevel: 2; topojson has 17 features with 9 properties
Processing topojsonGeometries: 17
File [1] SAHSU_GRD_Level2; topojson zoomlevels: 6; from: 6 to: 11
Shape file [2]: SAHSU_GRD_Level3.shp; areas: 200; points: 253631; geolevel: 3 [Default study area: SAHSU_GRD_LEVEL3]; topojson has 200 features with 8 properties
Processing topojsonGeometries: 200
File [2] SAHSU_GRD_Level3; topojson zoomlevels: 6; from: 6 to: 11
Shape file [3]: SAHSU_GRD_Level4.shp; areas: 1230; points: 407439; geolevel: 4; topojson has 1230 features with 10 properties
Processing topojsonGeometries: 1230
File [3] SAHSU_GRD_Level4; topojson zoomlevels: 6; from: 6 to: 11
shpConvertFieldProcessor().shapeFileQueue.drain() OK
+13.305 S addStatus: [shpConvert:1979:addStatusCloneCallback()] +13.305S new state: BATCH_INTERMEDIATE_END; code: 200
Re-creating status file: status.json
mkdir: C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data
Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; size: 471818; took: 0.026S
+13.6 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.6S new state: Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; size: 353982; took: 0.019S
+13.622 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.622S new state: Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; size: 265538; took: 0.015S
+13.641 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.641S new state: Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; size: 199183; took: 0.012S
+13.657 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.657S new state: Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; size: 149459; took: 0.006S
+13.667 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.667S new state: Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; size: 112109; took: 0.004S
+13.676 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.676S new state: Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; size: 1589314; took: 0.074S
+13.753 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.753S new state: Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; size: 1171586; took: 0.051S
+13.808 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.808S new state: Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; size: 873202; took: 0.043S
+13.855 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.855S new state: Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; size: 653809; took: 0.036S
+13.896 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.896S new state: Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; size: 488755; took: 0.027S
+13.926 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.926S new state: Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; size: 365205; took: 0.019S
+13.949 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +13.949S new state: Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; size: 3260985; took: 0.156S
+14.109 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +14.109S new state: Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; size: 2400055; took: 0.103S
+14.221 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +14.221S new state: Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; size: 1789195; took: 0.076S
+14.301 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +14.301S new state: Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; size: 1335052; took: 0.156S
+14.902 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +14.902S new state: Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; size: 992878; took: 0.124S
+15.031 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +15.031S new state: Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; size: 736619; took: 0.062S
+15.097 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +15.097S new state: Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; size: 5204030; took: 0.418S
+15.52 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +15.52S new state: Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; size: 3808132; took: 0.258S
+15.788 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +15.788S new state: Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; size: 2812473; took: 0.228S
+16.025 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +16.025S new state: Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; size: 2069628; took: 0.112S
+16.14 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +16.14S new state: Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; size: 1510635; took: 0.111S
+16.254 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +16.254S new state: Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; code: 200
Re-creating status file: status.json
Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; size: 1093562; took: 0.061S
+16.319 S addStatus: [geojsonToCSV:218:geoJSON2WKTEnd()] +16.319S new state: Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230; code: 200
Re-creating status file: status.json
Created wellknown text for 4 zoomlevels; 4 CSV files; took: 2.751S
+16.323 S addStatus: [geojsonToCSV:314:geoJSON2WKTFileEnd()] +16.323S new state: Created wellknown text for 4 zoomlevels; 4 CSV files; code: 200
Re-creating status file: status.json
CSV file [1]: SAHSU_GRD_Level1.shp; rows: 1; 14 keys: ID,LEVEL1,AREA,GID,AREAID,AREANAME,AREA_KM2,GEOGRAPHIC_CENTROID_WKT,WKT_11,WKT_10,WKT_9,WKT_8,WKT_7,WKT_6
svStreamClose(): C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data/SAHSU_GRD_Level1.csv
+16.342 S addStatus: [geojsonToCSV:420:geoJSON2WKTFileCSVEnd()] +16.342S new state: Wrote CSV file SAHSU_GRD_Level1.csv; size: 1.48MB; code: 200
Re-creating status file: status.json
CSV file [2]: SAHSU_GRD_Level2.shp; rows: 17; 15 keys: LEVEL2,AREA,LEVEL1,NAME,GID,AREAID,AREANAME,AREA_KM2,GEOGRAPHIC_CENTROID_WKT,WKT_11,WKT_10,WKT_9,WKT_8,WKT_7,WKT_6
svStreamClose(): C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data/SAHSU_GRD_Level2.csv
+16.391 S addStatus: [geojsonToCSV:420:geoJSON2WKTFileCSVEnd()] +16.391S new state: Wrote CSV file SAHSU_GRD_Level2.csv; size: 4.91MB; code: 200
Re-creating status file: status.json
CSV file [3]: SAHSU_GRD_Level3.shp; rows: 200; 14 keys: LEVEL2,LEVEL1,LEVEL3,GID,AREAID,AREANAME,AREA_KM2,GEOGRAPHIC_CENTROID_WKT,WKT_11,WKT_10,WKT_9,WKT_8,WKT_7,WKT_6
svStreamClose(): C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data/SAHSU_GRD_Level3.csv
+16.517 S addStatus: [geojsonToCSV:420:geoJSON2WKTFileCSVEnd()] +16.517S new state: Wrote CSV file SAHSU_GRD_Level3.csv; size: 10.06MB; code: 200
Re-creating status file: status.json
CSV file [4]: SAHSU_GRD_Level4.shp; rows: 1230; 16 keys: PERIMETER,LEVEL4,LEVEL2,LEVEL1,LEVEL3,GID,AREAID,AREANAME,AREA_KM2,GEOGRAPHIC_CENTROID_WKT,WKT_11,WKT_10,WKT_9,WKT_8,WKT_7,WKT_6
svStreamClose(): C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data/SAHSU_GRD_Level4.csv
+16.802 S addStatus: [geojsonToCSV:420:geoJSON2WKTFileCSVEnd()] +16.802S new state: Wrote CSV file SAHSU_GRD_Level4.csv; size: 15.96MB; code: 200
Re-creating status file: status.json
Detected hierarchy_sahsuland primary key: sahsu_grd_level4; file: 3; geolevel: 4
Detected hierarchy_sahsuland primary key: sahsu_grd_level4; file: 3; geolevel: 4
Created database load scripts: pg_SAHSULAND.sql and mssql_SAHSULAND.sql
Detected hierarchy_sahsuland primary key: sahsu_grd_level4; file: 3; geolevel: 4
Detected hierarchy_sahsuland primary key: sahsu_grd_level4; file: 3; geolevel: 4
PostGresstreamClose(): C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data/rif_pg_SAHSULAND.sql
MSSQLServerstreamClose(): C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data/rif_mssql_SAHSULAND.sql
PostGresstreamClose(): C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data/pg_SAHSULAND.sql
MSSQLServerstreamClose(): C:/Users/Peter/AppData/Local/Temp/shpConvert/66d8a532-bb2c-4304-8e2b-ffde330b88fa/data/mssql_SAHSULAND.sql
Created database load scripts: pg_SAHSULAND.sql and mssql_SAHSULAND.sql
+17.044 S addStatus: [dbLoad:2805:createSqlServerFmtFilesEnd()] +17.044S new state: Created database load scripts: pg_SAHSULAND.sql and mssql_SAHSULAND.sql; code: 200
Re-creating status file: status.json
+17.051 S addStatus: [nodeGeoSpatialServicesCommon:325:responseProcessing()] +17.051S new state: BATCH_END; code: 200
Re-creating status file: status.json
Status array
[0]: +0 S (200); INIT
[1]: +0.15 S (200); All form data and fields loaded; running completion processing
[2]: +0.154 S (200); BATCH_START
[3]: +0.229 S (200); Processing zip file [1]: SAHSULAND.zip; size: 6.73MB
[4]: +0.279 S (200); Expanded 1.2: SAHSULAND.zip//:geoDataLoader.xml as file 21 to list; size: 17.71KB
[5]: +0.378 S (200); Expanded 1.4: SAHSULAND.zip//:SAHSU_GRD_Level1.csv as file 32 to list; size: 455.47KB
[6]: +0.383 S (200); Expanded 1.5: SAHSULAND.zip//:SAHSU_GRD_Level1.dbf as file 43 to list; size: 0.16KB
[7]: +0.391 S (200); Expanded 1.6: SAHSULAND.zip//:SAHSU_GRD_Level1.prj as file 54 to list; size: 0.41KB
[8]: +0.397 S (200); Expanded 1.7: SAHSULAND.zip//:SAHSU_GRD_Level1.sbn as file 65 to list; size: 0.13KB
[9]: +0.41 S (200); Expanded 1.8: SAHSULAND.zip//:SAHSU_GRD_Level1.sbx as file 76 to list; size: 0.11KB
[10]: +0.433 S (200); Expanded 1.9: SAHSULAND.zip//:SAHSU_GRD_Level1.shp as file 87 to list; size: 193.03KB
[11]: +0.437 S (200); Expanded 1.10: SAHSULAND.zip//:SAHSU_GRD_Level1.shp.xml as file 98 to list; size: 0.99KB
[12]: +0.44 S (200); Expanded 1.11: SAHSULAND.zip//:SAHSU_GRD_Level1.shx as file 109 to list; size: 0.11KB
[13]: +0.509 S (200); Expanded 1.12: SAHSULAND.zip//:SAHSU_GRD_Level2.csv as file 1110 to list; size: 1.5MB
[14]: +0.519 S (200); Expanded 1.13: SAHSULAND.zip//:SAHSU_GRD_Level2.dbf as file 1211 to list; size: 1KB
[15]: +0.522 S (200); Expanded 1.14: SAHSULAND.zip//:SAHSU_GRD_Level2.prj as file 1312 to list; size: 0.41KB
[16]: +0.525 S (200); Expanded 1.15: SAHSULAND.zip//:SAHSU_GRD_Level2.sbn as file 1413 to list; size: 0.29KB
[17]: +0.529 S (200); Expanded 1.16: SAHSULAND.zip//:SAHSU_GRD_Level2.sbx as file 1514 to list; size: 0.13KB
[18]: +0.548 S (200); Expanded 1.17: SAHSULAND.zip//:SAHSU_GRD_Level2.shp as file 1615 to list; size: 650.5KB
[19]: +0.55 S (200); Expanded 1.18: SAHSULAND.zip//:SAHSU_GRD_Level2.shp.xml as file 1716 to list; size: 0.99KB
[20]: +0.553 S (200); Expanded 1.19: SAHSULAND.zip//:SAHSU_GRD_Level2.shx as file 1817 to list; size: 0.23KB
[21]: +0.607 S (200); Expanded 1.20: SAHSULAND.zip//:SAHSU_GRD_Level3.csv as file 1918 to list; size: 3.08MB
[22]: +0.611 S (200); Expanded 1.21: SAHSULAND.zip//:SAHSU_GRD_Level3.dbf as file 2019 to list; size: 5.79KB
[23]: +0.615 S (200); Expanded 1.22: SAHSULAND.zip//:SAHSU_GRD_Level3.prj as file 2120 to list; size: 0.41KB
[24]: +0.619 S (200); Expanded 1.23: SAHSULAND.zip//:SAHSU_GRD_Level3.sbn as file 2221 to list; size: 2.04KB
[25]: +0.623 S (200); Expanded 1.24: SAHSULAND.zip//:SAHSU_GRD_Level3.sbx as file 2322 to list; size: 0.24KB
[26]: +0.662 S (200); Expanded 1.25: SAHSULAND.zip//:SAHSU_GRD_Level3.shp as file 2423 to list; size: 1.31MB
[27]: +0.667 S (200); Expanded 1.26: SAHSULAND.zip//:SAHSU_GRD_Level3.shp.xml as file 2524 to list; size: 0.99KB
[28]: +0.671 S (200); Expanded 1.27: SAHSULAND.zip//:SAHSU_GRD_Level3.shx as file 2625 to list; size: 1.66KB
[29]: +0.764 S (200); Expanded 1.28: SAHSULAND.zip//:SAHSU_GRD_Level4.csv as file 2726 to list; size: 4.98MB
[30]: +0.769 S (200); Expanded 1.29: SAHSULAND.zip//:SAHSU_GRD_Level4.dbf as file 2827 to list; size: 73.46KB
[31]: +0.773 S (200); Expanded 1.30: SAHSULAND.zip//:SAHSU_GRD_Level4.prj as file 2928 to list; size: 0.41KB
[32]: +0.777 S (200); Expanded 1.31: SAHSULAND.zip//:SAHSU_GRD_Level4.sbn as file 3029 to list; size: 12.03KB
[33]: +0.78 S (200); Expanded 1.32: SAHSULAND.zip//:SAHSU_GRD_Level4.sbx as file 3130 to list; size: 0.53KB
[34]: +0.842 S (200); Expanded 1.33: SAHSULAND.zip//:SAHSU_GRD_Level4.shp as file 3231 to list; size: 2.14MB
[35]: +0.845 S (200); Expanded 1.34: SAHSULAND.zip//:SAHSU_GRD_Level4.shp.xml as file 3332 to list; size: 8.15KB
[36]: +0.849 S (200); Expanded 1.35: SAHSULAND.zip//:SAHSU_GRD_Level4.shx as file 3433 to list; size: 9.71KB
[37]: +0.853 S (200); Processed zip file 1: SAHSULAND.zip; size: 6.73MB; added: 33 file(s)
[38]: +1.835 S (200); Read shapefile: SAHSU_GRD_Level1.shp; size: 465.58KB; 1 records
[39]: +1.852 S (200); Saved JSON feature: 1/1
[40]: +1.857 S (200); Saved JSON file: SAHSU_GRD_Level1.json
[41]: +1.9 S (200); SAHSU_GRD_Level1: simplified topojson for zoomlevel 11
[42]: +1.927 S (200); Created geojson (1 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level1
[43]: +1.994 S (200); Created geojson (1 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level1
[44]: +2.128 S (200); SAHSU_GRD_Level1: simplified topojson for zoomlevel: 10
[45]: +2.14 S (200); Created geojson (1 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level1
[46]: +2.194 S (200); Created geojson (1 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level1
[47]: +2.273 S (200); SAHSU_GRD_Level1: simplified topojson for zoomlevel: 9
[48]: +2.282 S (200); Created geojson (1 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level1
[49]: +2.318 S (200); Created geojson (1 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level1
[50]: +2.379 S (200); SAHSU_GRD_Level1: simplified topojson for zoomlevel: 8
[51]: +2.386 S (200); Created geojson (1 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level1
[52]: +2.413 S (200); Created geojson (1 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level1
[53]: +2.459 S (200); SAHSU_GRD_Level1: simplified topojson for zoomlevel: 7
[54]: +2.466 S (200); Created geojson (1 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level1
[55]: +2.486 S (200); Created geojson (1 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level1
[56]: +2.515 S (200); SAHSU_GRD_Level1: simplified topojson for zoomlevel: 6
[57]: +2.519 S (200); SAHSU_GRD_Level1: simplified topojson for zoomlevels 6 to 11
[58]: +2.53 S (200); TopoJSON creation and save: SAHSU_GRD_Level1.topojson
[59]: +2.841 S (200); Read shapefile: SAHSU_GRD_Level2.shp; size: 1.54MB; 17 records
[60]: +2.883 S (200); Saved JSON feature: 17/17
[61]: +2.891 S (200); Saved JSON file: SAHSU_GRD_Level2.json
[62]: +3.014 S (200); SAHSU_GRD_Level2: simplified topojson for zoomlevel 11
[63]: +3.057 S (200); Created geojson (17 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level2
[64]: +3.201 S (200); Created geojson (17 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level2
[65]: +3.365 S (200); SAHSU_GRD_Level2: simplified topojson for zoomlevel: 10
[66]: +3.395 S (200); Created geojson (17 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level2
[67]: +3.502 S (200); Created geojson (17 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level2
[68]: +3.633 S (200); SAHSU_GRD_Level2: simplified topojson for zoomlevel: 9
[69]: +3.667 S (200); Created geojson (17 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level2
[70]: +3.763 S (200); Created geojson (17 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level2
[71]: +3.857 S (200); SAHSU_GRD_Level2: simplified topojson for zoomlevel: 8
[72]: +3.881 S (200); Created geojson (17 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level2
[73]: +3.941 S (200); Created geojson (17 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level2
[74]: +4.015 S (200); SAHSU_GRD_Level2: simplified topojson for zoomlevel: 7
[75]: +4.033 S (200); Created geojson (17 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level2
[76]: +4.079 S (200); Created geojson (17 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level2
[77]: +4.131 S (200); SAHSU_GRD_Level2: simplified topojson for zoomlevel: 6
[78]: +4.136 S (200); SAHSU_GRD_Level2: simplified topojson for zoomlevels 6 to 11
[79]: +4.152 S (200); TopoJSON creation and save: SAHSU_GRD_Level2.topojson
[80]: +4.792 S (200); Read shapefile: SAHSU_GRD_Level3.shp; size: 3.18MB; 200 records
[81]: +4.874 S (200); Saved JSON feature: 200/200
[82]: +4.913 S (200); Saved JSON file: SAHSU_GRD_Level3.json
[83]: +5.09 S (200); SAHSU_GRD_Level3: simplified topojson for zoomlevel 11
[84]: +5.197 S (200); Created geojson (200 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level3
[85]: +5.502 S (200); Created geojson (200 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level3
[86]: +5.788 S (200); SAHSU_GRD_Level3: simplified topojson for zoomlevel: 10
[87]: +5.856 S (200); Created geojson (200 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level3
[88]: +6.083 S (200); Created geojson (200 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level3
[89]: +6.304 S (200); SAHSU_GRD_Level3: simplified topojson for zoomlevel: 9
[90]: +6.374 S (200); Created geojson (200 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level3
[91]: +6.556 S (200); Created geojson (200 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level3
[92]: +6.714 S (200); SAHSU_GRD_Level3: simplified topojson for zoomlevel: 8
[93]: +6.754 S (200); Created geojson (200 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level3
[94]: +6.894 S (200); Created geojson (200 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level3
[95]: +7.01 S (200); SAHSU_GRD_Level3: simplified topojson for zoomlevel: 7
[96]: +7.04 S (200); Created geojson (200 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level3
[97]: +7.135 S (200); Created geojson (200 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level3
[98]: +7.225 S (200); SAHSU_GRD_Level3: simplified topojson for zoomlevel: 6
[99]: +7.233 S (200); SAHSU_GRD_Level3: simplified topojson for zoomlevels 6 to 11
[100]: +7.26 S (200); TopoJSON creation and save: SAHSU_GRD_Level3.topojson
[101]: +8.385 S (200); Reading shapefile record 819 from: SAHSU_GRD_Level4.shp; current size: 2.57MB
[102]: +9.068 S (200); Read shapefile: SAHSU_GRD_Level4.shp; size: 5.24MB; 1230 records
[103]: +9.197 S (200); Saved JSON feature: 1230/1230
[104]: +9.218 S (200); Saved JSON file: SAHSU_GRD_Level4.json
[105]: +9.486 S (200); SAHSU_GRD_Level4: simplified topojson for zoomlevel 11
[106]: +9.687 S (200); Created geojson (1230 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level4
[107]: +10.194 S (200); Created geojson (1230 areas) for zoomlevel 10 from zoomlevel 11 topojson: SAHSU_GRD_Level4
[108]: +10.603 S (200); SAHSU_GRD_Level4: simplified topojson for zoomlevel: 10
[109]: +10.744 S (200); Created geojson (1230 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level4
[110]: +11.137 S (200); Created geojson (1230 areas) for zoomlevel 9 from zoomlevel 10 topojson: SAHSU_GRD_Level4
[111]: +11.454 S (200); SAHSU_GRD_Level4: simplified topojson for zoomlevel: 9
[112]: +11.544 S (200); Created geojson (1230 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level4
[113]: +11.841 S (200); Created geojson (1230 areas) for zoomlevel 8 from zoomlevel 9 topojson: SAHSU_GRD_Level4
[114]: +12.07 S (200); SAHSU_GRD_Level4: simplified topojson for zoomlevel: 8
[115]: +12.147 S (200); Created geojson (1230 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level4
[116]: +12.46 S (200); Created geojson (1230 areas) for zoomlevel 7 from zoomlevel 8 topojson: SAHSU_GRD_Level4
[117]: +12.756 S (200); SAHSU_GRD_Level4: simplified topojson for zoomlevel: 7
[118]: +12.82 S (200); Created geojson (1230 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level4
[119]: +13.037 S (200); Created geojson (1230 areas) for zoomlevel 6 from zoomlevel 7 topojson: SAHSU_GRD_Level4
[120]: +13.16 S (200); SAHSU_GRD_Level4: simplified topojson for zoomlevel: 6
[121]: +13.174 S (200); SAHSU_GRD_Level4: simplified topojson for zoomlevels 6 to 11
[122]: +13.227 S (200); TopoJSON creation and save: SAHSU_GRD_Level4.topojson
[123]: +13.301 S (200); Cloned intermediate response
[124]: +13.305 S (200); BATCH_INTERMEDIATE_END
[125]: +13.6 S (200); Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1
[126]: +13.622 S (200); Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1
[127]: +13.641 S (200); Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1
[128]: +13.657 S (200); Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1
[129]: +13.667 S (200); Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1
[130]: +13.676 S (200); Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level1.shp; rows: 1
[131]: +13.753 S (200); Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17
[132]: +13.808 S (200); Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17
[133]: +13.855 S (200); Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17
[134]: +13.896 S (200); Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17
[135]: +13.926 S (200); Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17
[136]: +13.949 S (200); Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level2.shp; rows: 17
[137]: +14.109 S (200); Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200
[138]: +14.221 S (200); Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200
[139]: +14.301 S (200); Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200
[140]: +14.902 S (200); Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200
[141]: +15.031 S (200); Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200
[142]: +15.097 S (200); Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level3.shp; rows: 200
[143]: +15.52 S (200); Created wellknown text for zoomlevel 11 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230
[144]: +15.788 S (200); Created wellknown text for zoomlevel 10 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230
[145]: +16.025 S (200); Created wellknown text for zoomlevel 9 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230
[146]: +16.14 S (200); Created wellknown text for zoomlevel 8 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230
[147]: +16.254 S (200); Created wellknown text for zoomlevel 7 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230
[148]: +16.319 S (200); Created wellknown text for zoomlevel 6 from geoJSON: SAHSU_GRD_Level4.shp; rows: 1230
[149]: +16.323 S (200); Created wellknown text for 4 zoomlevels; 4 CSV files
[150]: +16.342 S (200); Wrote CSV file SAHSU_GRD_Level1.csv; size: 1.48MB
[151]: +16.391 S (200); Wrote CSV file SAHSU_GRD_Level2.csv; size: 4.91MB
[152]: +16.517 S (200); Wrote CSV file SAHSU_GRD_Level3.csv; size: 10.06MB
[153]: +16.802 S (200); Wrote CSV file SAHSU_GRD_Level4.csv; size: 15.96MB
[154]: +17.044 S (200); Created database load scripts: pg_SAHSULAND.sql and mssql_SAHSULAND.sql
[155]: +17.051 S (200); BATCH_END

Created response, size: 9415594
<<< End of diagnostics

No errors

* LOG END ***********************************************************************
```