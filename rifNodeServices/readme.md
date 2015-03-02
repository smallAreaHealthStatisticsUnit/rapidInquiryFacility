<h1>Node Web Services API for RIF 4.0:</h1>
<h2> toTopojson </h2>
<p>
CONVERTS GEOJSON(MAX 1MB) TO TOPOJSON <br/>
 Only POST requests are processed <br/>
  Expects a vaild geojson as input <br/>
  Topojson output is echoed as plain text <br/>
 Topojson have quantization on  The level of quantization is based on map tile zoom level <br/>
 More info on quantization here: https://github.com/mbostock/topojson/wiki/Command-Line-Reference

</p>
<h2> simplify </h2>
<p>
COPY / UNZIP / SIMPLIFY FIRST SHAPEFILE FOUND <br/>
 Only POST requests are processed <br/>
 Expects a shapefile as input <br/>
 Simplified shapefile as output  <br/>
 NOTE: ADM-ZIP is blocking <br/>
</p>

<h2>The API make use of the following modules downloadable via npm:</h2>
<ul>
  <li>Express</li>
  <li>ADM-ZIP</li>
  <li>Topojson</li>
  <li>Connect-Busboy</li>
  <li>Mapshaper</li>
</ul>
<h2>To test using the request script the following are also needed:</h2>
<ul>
  <li>Request</li>
  <li>Form-Data</li>
</ul>
