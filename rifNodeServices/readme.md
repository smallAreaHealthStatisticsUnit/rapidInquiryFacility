# Node Web Services API for RIF 4.0
## toTopojson
CONVERTS GEOJSON(MAX 1MB) TO TOPOJSON:
* Only POST requests are processed 
* Expects a vaild geojson as input 
* Topojson output is echoed as plain text
* Topojson have quantization on  The level of quantization is based on map tile zoom level 

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

## To test using the request scripts the following are also needed:

* Request
* Form-Data

```Node
P:\Github\rapidInquiryFacility\rifNodeServices>node expressServer.js
Listening on port 3000...
```

```Node
P:\Github\rapidInquiryFacility\rifNodeServices>node request.js
1
Sending requests:1
Upload #1 successful!  Server responded with: {"type":"Topology","objects":{"collection":{"type":"GeometryCollection","crs":{"type":"name","properties":{"name":"urn:ogc:def:crs:OGC
:1.3:CRS84"}},"geometries":[{"type":"Polygon","arcs":[[0,1,2,3,4,5]]},{"type":"Polygon","arcs":[[-5,-4,6,7,8,9]]},{"type":"Polygon","arcs":[[-6,-10,10,11,12,13,14,15,16]]},{"type":
"Polygon","arcs":[[17,18,19,20,21,22,23]]},{"type":"Polygon","arcs":[[24,25,26,27]]},{"type":"Polygon","arcs":[[-14,28,29]]},{"type":"Polygon","arcs":[[30,31,32,33,34]]},{"type":"P
olygon","arcs":[[35,36,37,38,39]]},{"type":"Polygon","arcs":[[40,-16,41,42]]},{"type":"Polygon","arcs":[[43,44,45]]},{"type":"Polygon","arcs":[[46,47,48,49,50]]},{"type":"Polygon",
"arcs":[[51,52,53,-1,-17,-41,54,55]]},{"type":"Polygon","arcs":[[56,57,58,59,60,61,62,63]]},{"type":"Polygon","arcs":[[64,65,-51,66,67,68]]},{"type":"Polygon","arcs":[[69,70,71,-61
]]},{"type":"Polygon","arcs":[[72,73,74,75]]},{"type":"Polygon","arcs":[[76,77,-2,-54]]},{"type":"Polygon","arcs":[[78,79]]},{"type":"Polygon","arcs":[[80,81,82]]},{"type":"Polygon
","arcs":[[83,84,85]]},{"type":"Polygon","arcs":[[86,87]]},{"type":"Polygon","arcs":[[88,89,90,91]]},{"type":"Polygon","arcs":[[92,93,94,-31]]},{"type":"Polygon","arcs":[[95,96,97]
]},{"type":"Polygon","arcs":[[98,99,-58,100,-33]]},{"type":"Polygon","arcs":[[101,102,-70,-60]]},{"type":"Polygon","arcs":[[103,104,105,106,107,108,109,110]]},{"type":"Polygon","ar
cs":[[111,112,-88,113,-92,114,115,116,117,118]]},{"type":"Polygon","arcs":[[119,120,121,122,123,124,125,126]]},{"type":"Polygon","arcs":[[127,128,129,130,131]]},{"type":"Polygon","
arcs":[[132,133,134]]},{"type":"Polygon","arcs":[[-12,135,-8,-7,136,-29,-13]]},{"type":"Polygon","arcs":[[137,-135,138,139,140,141]]},{"type":"Polygon","arcs":[[-39,142,143,144]]},
{"type":"Polygon","arcs":[[145,146,147,148]]},{"type":"Polygon","arcs":[[149,-132,150,151,152,153,154,155]]},{"type":"Polygon","arcs":[[156,-63,157,158,159]]},{"type":"Polygon","ar
cs":[[160,161,162,163,-74]]},{"type":"Polygon","arcs":[[-9,-136,11,-12,-11]]},{"type":"Polygon","arcs":[[164,-126,165,-152]]},{"type":"Polygon","arcs":[[166,167,168,169,170,-123]]}
,{"type":"Polygon","arcs":[[171,172,173,174]]},{"type":"Polygon","arcs":[[175,176,177,178,179,180,181,182]]},{"type":"Polygon","arcs":[[-27,183,184,185,186]]},{"type":"Polygon","ar
cs":[[187,188,189,190]]},{"type":"Polygon","arcs":[[191,192,193,194]]},{"type":"Polygon","arcs":[[-97,195,-77,-53,196]]},{"type":"Polygon","arcs":[[197,-183,198,199]]},{"type":"Pol
...
```
