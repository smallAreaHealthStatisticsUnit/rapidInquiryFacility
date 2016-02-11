# Node Web Services API for RIF 4.0
## toTopojson
CONVERTS GEOJSON(MAX 1000MB) TO TOPOJSON:
* Only POST requests are processed 
* Expects a vaild geojson as an input file. Gzip and lz77 (not ZIP) files are supported. 
* Topojson output is returned as a reponse JSON object
* Topojson is quantized based on map tile zoom level 
* Processing is controlled by form fields

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
 				United States, Alaska and Hawaii. DO NOT SET UNLESS YOU KNOW WHAT YOU ARE DOING!
* verbose: 		Set Topojson.Topology() option if true. Produces debug returned as part of reponse.message
* id:			Name of feature property to promote to geometry id; default is ID. Value must exist in data.
 				Creates myId() function and registers it with Topojson.Topology() via the id option
* property-transform-fields:
				JSON array of additional fields in GeoJSON to add to output topoJSON. Uses the Topojson.Topology()
  				property-transform option. Value must be parseable by JSON.parse(). Value must exist in data.
 				Creates myPropertyTransform() function and registers it with Topojson.Topology() via the 
 				property-transform option
 
All other fields have no special processing. Fields are returned in the response.fields JSON array. Any field processing errors 
either during processing or in the id and property-transform Topojson.Topology() callback functions will cause processing to fail.
 
JSON injection protection. This function does NOT use eval() as it is source of potential injection
e.g.
```Node
var rval=eval("d.properties." + ofields[fieldname]);
```
Instead it tests for the field name directly:
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

### Response (returned) JSON

Response object - no errors:
                    
* no_files: 		Numeric, number of files    
* field_errors: 	Number of errors in processing fields
* file_list: 		Array file objects:

  * file_name: File name
  * topojson: TopoJSON created from file geoJSON,
  * topojson_stderr: Debug from TopoJSON module,
  * topojson_runtime: Time to convert geoJSON to topoJSON (S),
  * file_size: Transferred file size in bytes,
  * transfer_time: Time to transfer file (S),
  * uncompress_time: Time to uncompress file (S)/undefined if file not compressed,
  * uncompress_size: Size of uncompressed file in bytes
* message: 			Processing messages, including debug from topoJSON               
* fields: 			Array of fields; includes all from request plus any additional fields set as a result of processing  

Response object - errors:
   
* error: 			Error message (if present) 
* no_files: 		Numeric, number of files    
* field_errors: 	Number of errors in processing fields
* file_list: 		Array file objects:

  * file_name: File name
* message: 		Processing messages, including debug from topoJSON               
* fields: 			Array of fields; includes all from request plus any additional fields set as a result of processing 
 
More info on quantization here: https://github.com/mbostock/topojson/wiki/Command-Line-Reference

## simplify
COPY / UNZIP / SIMPLIFY FIRST SHAPEFILE FOUND IN ZIPPED FOLDER 
* Only POST requests are processed
* Expects a shapefile as input
* Simplified shapefile as output
NOTE: ADM-ZIP is blocking 

## The API make use of the following modules downloadable via npm:

* Express
* ADM-ZIP
* Topojson
* Connect-Busboy
* Mapshaper
* Magic-globals
* Helmet

## Install

To be added

## Testing

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

1. Defaults
2. Verbose
3. ZoomLevel=0
4. Projection: 27700
5. gzip geoJSON file
6. gzip geoJSON file; wrong Content-Type; will work
7. gzip geoJSON multiple files
8. TopoJSON id support
9. TopoJSON id support: invalid id
10. TopoJSON conversion: invalid geoJSON
11. Uncompress: invalid lz77
12. Invalid zip file (not supported)
13. Zero sized file
14. TopoJSON property-transform test
15. TopoJSON property-transform support: invalid property-transform field
16. TopoJSON property-transform support: invalid property-transform array
17. TopoJSON id and property-transform test
18. TopoJSON property-transform support: JSON injection tests

#### Example

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
Field: my_test[TopoJSON id and property-transform test];
Field: my_reference[17];
Field: expected_to_pass[true];
Field: verbose[true]; verbose mode enabled
Field: zoomLevel[0]; Quantization set to: 400
Field: property-transform-fields[["name","area_id","gid"]];
myPropertyTransform() function id fields set to: ["name","area_id","gid"]; 3 field(s)
Field: id[gid];
myId() function id field set to: gid
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
    "my_test": "TopoJSON id and property-transform test",
    "expected_to_pass": "true",
    "property-transform-fields": "[\"name\",\"area_id\",\"gid\"]",
    "id": "gid"
}
File [1:test_6_sahsu_4_level4_0_0_0.js]
topoJSON length: 354583; file size: 3806813; Topology() runtime: 0.128 S; transfer time: 0.019 S;
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
