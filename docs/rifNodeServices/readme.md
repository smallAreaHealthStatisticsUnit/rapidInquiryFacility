# ![RIF logo](http://www.envhealthatlas.co.uk/homepage/images/sahsu_logo.png) Node Web Services API for RIF 4.0

### Table of Contents

**[toTopojson web service](#toTopojson-web-service)**  
**[simplify web service](#simplify-web-service)**  
**[Install rifNodeServices](#Installing-rifNodeServices)**   
**[Testing rifNodeServices](#Testing-rifNodeServices)**  

## Workflow 

The RIF web front end uses leaflet, which requires map tiles; these are squares that follow certain Google Maps conventions:
* Tiles are 256x256 pixels;
* At the outer most zoom level, 0, the entire world can be rendered in a single map tile;
* Each zoom level doubles in both dimensions, so a single tile is replaced by 4 tiles when zooming in. This means that about 22 zoom levels are sufficient
  for most practical purposes; this RIF uses 0-11;
* The Web Mercator (WGS84, as used by GPS) projection is used, with latitude limits of around 85 degrees. Can needs to be taken during simplification with 
  Northern latitudes in the USA because of the distortions in the Mercator projection (e.g. the size of Greenland); with each pixel being much shorter in
  length than in latitudes near to the equator.
  
The de facto OpenStreetMap standard, known as Slippy Map Tilenames or XYZ, follows these and adds more:
* An X and Y numbering scheme; with Z for the zoomlevels;
* PNG images for tiles. In this case Leaflet uses topoJSON.

Images are served through a REST API, with a URL like http://.../Z/X/Y.png, where Z is the zoom level, and X and Y identify the tile.

### Extract

The start of the map extraction is a meta data XML file so that the loading can do all processing without further input. The user has to 
define the area id and area id name fields, and their descriptions, per shapefile and its name. The field names are present in the .SHP.EA.ISO.XML file.

Extract and save geospatial data from two  or more shape files by file extension:
* .PRJ: projection information. The RIF should support all projections in PostGIS. In some cases, this may fail where the same projection is used by 
   multiple SRIDs (e.g. UK and EIRE) and is therefore dependent on the embedded projection name field being recognised. The workaround for this is to 
   manually embed the SRID in the .PRJ projection file;
* .SHP: geospatial data; normally one polygon per record. Areas with multiple polygons (e.g. Islands) are on multiple rows. Shapefile data is processed 
  record by record so that very large shapefiles can be processed; this causes both the extract and transform phases to be a series of nested loops to 
  minimise the memory consumption. Shapefile data is converted from the proprietary ESRI format to a geoJSON collection. Each area id is a JSON feature. 
  Leaflet (the JavaScript map display library) works with data projected in WGS84 (GPS) and the geoJSON is therefore projected at this point and then saved in blocks of area ids;
* .DBF: area id names and area ids, other data in DBF file (e.g. total males, total females, average income) The DBF field data are embedded within each geoJSON feature;
* .SHP.EA.ISO.XML: meta-data related to the shape file (field names, shapefile description). If this file is not present, the information must be supplied by the user;
* Meta-data extracted from the shape file (the number of areas, the bounding box co-ordinates of the mapped area, resolution order of the shapefiles – lowest is 1).

At the end of the extract workflow step there are two types of files:
* One geoJSON feature collection per shapefile containing:
  * The bounding box co-ordinates of the mapped area
  * An array of features, containing:
    * area id name and area id, other data from the DBF file;
    * Multi polygon features;
* Meta data stored as an XML file, containing:
  * Projection information:
    * SRID of the shapefile (e.g. 27700 for the UK);
    * PROJ4 projection information for the geoJSON data (i.e. SRID 4326 – WGS84);
  * Shapefiles, field names, shapefile description;
  * Meta-data extracted from the shape files.
	 
The geoJSON is also converted to well-known text and saved in the native geospatial datatype in the database (MS SQL Server or Postgres).
The multi polygons are created as the geoJSON is being assembled feature by feature (i.e. unioned together) and ensure that the area ids are unique. 
In addition, each polygon will be checked to ensure the start co-ordinate = the end co-ordinate (i.e. it is not a line string). Extract of necessity therefore performs low level conversions because of the need to conserve memory. A 1.5Gbyes shapefile requires 9-10 Gbytes of memory when represented as JSON.

### Transform

Transform is a series of nested loops that clean and convert two of more shapefiles into the following deliverables per shape file:

* The geoJSON is simplified and converted to topoJSON. This is to that the information displayed in leaflet at each map zoomlevel is the optimised 
  to the pixel size of the (largest) screen. As a minimum zoomelevels 6,8 and 11 will be supported. A key test is that the whole world (zoomlevel 1, 
  two tiles) for the USA must display in a second or so. This may result in more zoomlevels being required;
* The topoJSON is converted back to geoJSON and thence to well-known text and then saved in the native geospatial datatype in the database (MS SQL Server or Postgres);
* The shapefiles are ordered by resolution using the total areas in a shapefile;
* The shapefiles are geometrically intersected to create a table using the geospatial database. This tells the RIF who contains what e.g. for a 
  census block group; which tract county and state is it in;
* In the database maptiles are generated for each zoomlevels; these are then converted to well-known text, geoJSON and finally topoJSON and saved as 
  files and in the database. In the RIF they are only stored in the database.

#### TopoJSON Conversion

TopoJSON conversion is the process of identifying the shared boundaries between the different areas in geospatial data; these are called arcs.
To allow this to reliably take place the points must be reduced to a known precision. This process is called pre-quantisation. A typical example would be 1x106 for the United States at zoomlevel 11. E.g. (see: http://wiki.openstreetmap.org/wiki/Zoom_levels); at zoomlevel 11; 1:250,000 scale, each pixel on the screen is 76m on the ground, so each the smallest X or Y co-ordinate must be no more than 1 millionth of the largest.

##### Simplification

A good example is at: https://bost.ocks.org/mike/simplify/. The topoJSON conversion is set to use Visvalingam’s algorithm which progressively removes points with the least-perceptible change. This then optimises the map for each of the higher zoomlevels.
This method removes the possibility of slivers in a map layer; it will only work across all shapefiles if they have the same initial quantisation (i.e. scale). Often they do not (the US being a good example) so when the shapefiles are overlaid in Leaflet slivers occur between the shapefiles (or layers). The RIF only ever display one shapefile layer (or geolevel) at a time so this is not a problem in practice.   

The choice of the simplification parameter is either in Steradians or as a percentage of the previous simplification. For zoomlevel 11 the area at the equator is 19.568 x 19.437 = 380.3 square km and a pixel is 76 x 76 = 0.005776 square km; in steradians this is (0.005776 / (510,072,000 * 12.56637) [area of earth in steradians] = 9.01x10-13 steradians. From zoomlevel 11 to 10, the simplification percentage is 25% as each level 10 tile contains 4 level 11 tiles.
This can be checked from the topoJSON output for US counties. In this case the X and Y resolution is less than 76m:
```
bounds: -179.148909 -14.548699000000001 179.77847 71.36516200000001 (spherical)
pre-quantization: 39.9m (0.000359°) 9.55m (0.0000859°)
topology: 11154 arcs, 631792 points
```

In the below example Seattle at Nation (in black), State (in blue) and County (in read) level, state and county are mapped at the same scale (1:500,000) and overlap perfectly (in purple) but Nation is at 1:5 million and is slivered with respect to the state and county levels. The nation level (in back) also shows signs of over simplification as it is only suitable for zoomlevel 8 or 9 at best.
 
![Seattle simplification example]({{ site.baseurl }}/rifNodeServices/Seattle.png) 

This does need some more work; in practice we will probably not simplify that aggressively and may not simplify zoomlevel 11 at all

### Checks

The following checks are carried out on the data at the extract phase:
* A minimum of two shapefiles;
* The .DNF and .SHP files are present;
* Some polygons are missing the final point (which should be the same as the first); i.e. are line strings; forcibly polygonise as long as the points are less than (to be specified: likely 20-100m) distance apart;
* All projection files are the same;
* The projection file is valid and is supported;
* Co-ordinates are valid for the projection;
* Bounding box is valid for the projection.

The last two points are effectively tested by the re-projections are can be re-tested in the database during the transform phase.

The following checks are carried out on the data at the transform phase:
* Check that the database geometry (multi polygon) is valid (using STInvalid()), try to fix using ST_MakeValid();
* All shapefiles in the set have the same bounding box;
* Areal (total area) mismatch between shapefiles;
* Perimeter lengths between zoomlevels indicate more simplification than expected (4x).

The change audit trail will be provided: Unions, linestring to polygon conversions, geometry (i.e. multi polygon) validators;

Ideally we should warn if the scale of map <= 1:250,000; but this information is not present in a parseable form in the .SHP.EA.ISO.XML file. Resolution checks therefore need to be done visually (in Leaflet) and by hand (from the meta data), see the above examples.

## toTopojson web service

The toTopojson service converts GeoJSON files upto 100MB in size to TopoJSON:

* Only POST requests are processed; 
* Expects a vaild geojson as an input file;
* Gzip and lz77 (not ZIP) files are supported;
* Mutliple input files supported;  
* Topojson output is returned as a [Response JSON](#Response-JSON) object;
* Topojson is quantized based on map tile zoom level; 
* Processing is controlled by form fields (see next section);

The toTopojson service uses [Mike Bostock's TopoJSON node package](https://github.com/mbostock/topojson).

### Field Processing

Processing is controlled by form fields:

Form fields specific processing:

* zoomLevel: 	Set quantization field and Topojson.Topology() option using local function getQuantization()
  				i.e. Set the maximum number of differentiable values along each dimension) by zoomLevel
				
|Zoomlevel|Quantization|
|---------|------------|
|<=6	  |	400		   |
|7		  |	700		   |
|8		  |	1500	   |
|9		  |	3000	   |
|10		  |	5000	   |
|>10	  |	10000	   |
							
* projection: 	Set projection field and Topojson.Topology() option. E.g. to convert spherical input geometry 
				to Cartesian coordinates via a D3 geographic projection. For example, a projection of 'd3.geo.albersUsa()' 
 				will project geometry using a composite Albers equal-area conic projection suitable for the contiguous 
 				United States, Alaska and Hawaii. **DO NOT SET UNLESS YOU KNOW WHAT YOU ARE DOING!**
* verbose: 		Set Topojson.Topology() option if true. Produces debug returned as part of reponse.message
* id:			Name of feature property to promote to geometry id; default is ID. Value must exist in data.
 				Creates myId() function and registers it with Topojson.Topology() via the id option
* property-transform-fields:
				JSON array of additional fields in GeoJSON to add to output topoJSON. Uses the Topojson.Topology()
  				property-transform option. Value must be parseable by `JSON.parse()`. Value must exist in data.
 				Creates myPropertyTransform() function and registers it with Topojson.Topology() via the 
 				property-transform option
 
All other fields have no special processing. Fields are returned in the response.fields JSON array. Any field processing errors 
either during processing or in the id and property-transform Topojson.Topology() callback functions will cause processing to fail.
 
#### JSON injection protection in form fields 

The form field processing function `req.busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated)` 
does NOT use eval() as this is source of potential injection

e.g.
```Node
var rval=eval("d.properties." + ofields[fieldname]);
```
Instead the function tests for the field name directly:
```Node
if (!d.properties[ofields[fieldname]]) { 
	response.field_errors++;
	var msg="FIELD PROCESSING ERROR! Invalid id field: d.properties." + ofields[fieldname] + " does not exist in geoJSON";
	if (options.id) {
		rifLog.rifLog2(__file, __line, "req.busboy.on('field')", msg, req);	
		options.id = undefined; // Prevent this section running again!	
		response.message = response.message + "\n" + msg;
	}
}
```					
So setting formData (see test\request.js test 18) to:
```Node
formData["property-transform-fields"]='["eval(console.error(JSON.stringify(req, null, 4)))"]';
```
Will cause an error:
```
Field: property-transform-fields[["eval(console.error(JSON.stringify(req, null, 4)))"]];
myPropertyTransform() function id fields set to: ["eval(console.error(JSON.stringify(req, null, 4)))"]; 1 field(s)
FIELD PROCESSING ERROR! Invalid property-transform field: d.properties.eval(console.error(JSON.stringify(req, null, 4))) does not exist in geoJSON;
```

### Response JSON

Response object - no errors:
                    
* no_files: 		Number of files    
* field_errors: 	Number of errors in processing fields
* file_errors: 		Number of errors in processing fields
* file_list: 		Array file objects:

  * file_name: File name
  * topojson: TopoJSON created from file geoJSON,
  * topojson_stderr: Debug from TopoJSON module,
  * topojson_runtime: Time to convert geoJSON to topoJSON (S),
  * file_size: Transferred file size in bytes,
  * transfer_time: Time to transfer file (S),
  * uncompress_time: Time to uncompress file (S) or undefined if file not compressed,
  * uncompress_size: Size of uncompressed file in bytes or undefined if file not compressed
* message: 			Processing messages, including debug from topoJSON               
* fields: 			Array of fields; includes all from request plus any additional fields set as a result of processing  

Response object - errors:
   
* error: 			Error message (if present)
* no_files: 		Numeric, number of files
* field_errors: 	Number of errors in processing fields
* file_errors: 		Number of errors in processing fields
* file_list: 		Array file objects:

  * file_name: File name
* message: 			Error message
* diagnostic:		Diagnotic message             
* fields: 			Array of fields; includes all from request plus any additional fields set as a result of processing 
 
E.g. Posting a ZIP file:

```JSON
{
	"error": "Unexpected token P",
	"no_files": 1,
	"field_errors": 0,
	"file_errors": 0,
	"file_list": [{
		"file_name": "test_6_sahsu_4_level4_0_0_0.zip"
	}],
	"message": "Your input file 1: test_6_sahsu_4_level4_0_0_0.zip; size: 831358; does not seem to contain valid JSON: \nDebug message:\n\nField: verbose[true]; verbose mode enabled\nProcessing File [1]: test_6_sahsu_4_level4_0_0_0.zip\n\n\nData:\n504b0304140000000800527e2f48cd802badbcae0c005d163a001e000000746573745f365f73616873755f345f6c6576656c345f305f305f302e6a73a49dc98a6d4d\n",
	"fields": {
		"my_reference": "",
		"zoomLevel": 0,
		"verbose": "true",
		"quantization": 10000,
		"projection": "4326"
	}
}
```
More info on Node Topojson options (e.g. quantization) is available here: https://github.com/mbostock/topojson/wiki/Command-Line-Reference

## simplify web service

COPY / UNZIP / SIMPLIFY FIRST SHAPEFILE FOUND IN ZIPPED FOLDER 
* Only POST requests are processed
* Expects a shapefile as input
* Simplified shapefile as output
NOTE: ADM-ZIP is blocking 

## Installing rifNodeServices

To be added

The API make use of the following modules downloadable via npm:

* Express
* ADM-ZIP
* Topojson
* Connect-Busboy
* Mapshaper
* Magic-globals
* Helmet

## Testing rifNodeServices

To test using the request scripts the following are also needed:

* Request
* Form-Data
* Request-debug

```
P:\Github\rapidInquiryFacility\rifNodeServices>node expressServer.js
Listening on port 3000...
```

### Test script test\request.js

The test script test\request.js has a single parameter, the <test number>. Without is all tests are run. npm test and 
make test will also run the comnplete test set.

#### Tests

The test harness uses the fields *my_reference* (test number) and *expected_to_pass* ("true"/"false") for internal control.

1. Defaults;
2. Verbose;
3. ZoomLevel=0;
4. Projection: d3.geo.mercator();
5. gzip geoJSON file;
6. gzip geoJSON file: wrong Content-Type [will work];
7. gzip geoJSON multiple files;
8. TopoJSON id support: use gid as the [unique] id field;
9. TopoJSON id support: invalid id [intentional failure];
10. TopoJSON conversion: invalid geoJSON;
11. Uncompress: invalid lz77 [intentional failure];
12. Invalid zip file (not supported) [intentional failure];
13. Zero sized file [intentional failure];
14. TopoJSON property-transform test

    Add ["name","area_id","gid"] fields to topoJSON;
	
15. TopoJSON property-transform support: invalid property-transform field [intentional failure];
16. TopoJSON property-transform support: invalid property-transform array [intentional failure];
17. TopoJSON id and property-transform test;
18. TopoJSON property-transform support: JSON injection tests (field does not exist) [intentional failure]

    Attempt to dump the req object to the console:
```JSON
["{eval(console.error(JSON.stringify(req.headers, null, 4)))};"]
```
19. TopoJSON property-transform support: JSON injection tests (invalid array exception)

    Attempt to dump the req object to the console:
```JSON
["invalid"+`{eval(console.error(JSON.stringify(req.headers, null, 4)));}`]
```

20: TopoJSON conversion: invalid geoJSON - overload transport with 2G file [intentional failure]
21: gzip geoJSON multiple files limit (100) [intentional failure]

#### Tests Example - Test 17

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices>node test\request.js 17
Using arg[2] nRequests: 17
Sending ./data/test_6_sahsu_4_level4_0_0_0.js request:17; length: 3806813
REQUEST { url: 'http://127.0.0.1:3000/toTopojson',
  headers: { 'Content-Type': 'application/json' },
  formData:
   { my_test: 'TopoJSON id and property-transform test',
     my_reference: '17',
     attachments: [ [Object] ],
     expected_to_pass: 'true',
     verbose: 'true',
     zoomLevel: 0,
     'property-transform-fields': '["name","area_id","gid"]',
     id: 'gid' },
  'content-length': 3806813,
  callback: [Function: optionalCallback],
  method: 'POST' }
REQUEST make request http://127.0.0.1:3000/toTopojson
Request debug: request;
headers{
    "Content-Type": "multipart/form-data; boundary=--------------------------541474924622301608947157",
    "host": "127.0.0.1:300;
body{
    "debugId": 1,
    "uri": "http://127.0.0.1:3000/toTopojson",
    "method": "POST",
    "headers": {
        "Content-Type": "m
REQUEST onRequestResponse http://127.0.0.1:3000/toTopojson 200 { 'x-content-type-options': 'nosniff',
  'x-frame-options': 'SAMEORIGIN',
  'x-download-options': 'noopen',
  'x-xss-protection': '1; mode=block',
  'content-type': 'text/plain',
  date: 'Thu, 11 Feb 2016 12:17:02 GMT',
  connection: 'close',
  'transfer-encoding': 'chunked' }
REQUEST reading response's body
REQUEST finish init function http://127.0.0.1:3000/toTopojson
REQUEST response end http://127.0.0.1:3000/toTopojson 200 { 'x-content-type-options': 'nosniff',
  'x-frame-options': 'SAMEORIGIN',
  'x-download-options': 'noopen',
  'x-xss-protection': '1; mode=block',
  'content-type': 'text/plain',
  date: 'Thu, 11 Feb 2016 12:17:02 GMT',
  connection: 'close',
  'transfer-encoding': 'chunked' }
REQUEST end event http://127.0.0.1:3000/toTopojson
REQUEST has body http://127.0.0.1:3000/toTopojson 355958
REQUEST emitting complete http://127.0.0.1:3000/toTopojson
Request debug: response;
headers{
    "x-content-type-options": "nosniff",
    "x-frame-options": "SAMEORIGIN",
    "x-download-options": "noopen",
    "x-xss-prote;
body{
    "debugId": 1,
    "headers": {
        "x-content-type-options": "nosniff",
        "x-frame-options": "SAMEORIGIN",
        "

Upload #17
Server debug >>>
Field: my_test[17: TopoJSON id and property-transform test];
Field: my_reference[17];
Field: expected_to_pass[true];
Field: verbose[true]; verbose mode enabled
Field: zoomLevel[0]; Quantization set to: 400
Field: property-transform-fields[["name","area_id","gid"]];
myPropertyTransform() function id fields set to: ["name","area_id","gid"]; 3 field(s)
Field: id[gid];
myId() function id field set to: gid
File received OK [1]: test_6_sahsu_4_level4_0_0_0.js; encoding: ; uncompressed data: 3806813
Processing File [1]: test_6_sahsu_4_level4_0_0_0.js
File [1]: test_6_sahsu_4_level4_0_0_0.js; runtime: ; topoJSON length: 354583] OK:
TopoJson.topology() stderr >>>
bounds: -7.58829438 52.68753577 -4.88653786 55.5268098 (spherical)
pre-quantization: 753m (0.00677°) 791m (0.00712°)
topology: 3986 arcs, 18197 points
<<< TopoJson.topology() stderr
<<< End of server debug

files processed: 1; fields: {
    "my_reference": "17",
    "zoomLevel": 0,
    "verbose": "true",
    "quantization": 400,
    "my_test": "17: TopoJSON id and property-transform test",
    "expected_to_pass": "true",
    "property-transform-fields": "[\"name\",\"area_id\",\"gid\"]",
    "id": "gid"
}
File [1:test_6_sahsu_4_level4_0_0_0.js]
topoJSON length: 354583; file size: 3806813; Topology() runtime: 0.193 S; transfer time: 1.202 S;
uncompress time: (Not compressed) S; uncompress file size: (Not compressed); JSON compression: 9%

First 600 characters of formatted topoJSON >>>
{
  "type": "Topology",
  "objects": {
    "collection": {
      "type": "GeometryCollection",
      "geometries": [
        {
          "type": "Polygon",
          "properties": {
            "name": "Abellan LEVEL4(01.001.000100.1)",
            "area_id": "01.001.000100.1",
            "gid": 1
          },
          "id": 1,
          "arcs": [
            [
              0,
              1,
              2,
              3,
              4,
              5,
              -6,
              6,
              7,
              8,
              -8,
              9,
              10,


<<< formatted topoJSON


End of upload #17

All tests passed: 1/1
```
