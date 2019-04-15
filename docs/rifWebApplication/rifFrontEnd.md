---
layout: default
title: The use of Angular.js in RIF Front End, April 2019
---

1. Contents
{:toc}

# Introduction

Assumes you are familiar with Angular nomenclature, so you know a partial from a directive.

* In this document RIF will be used to refer to the front-end of the RIF;
* Uses the Angular 1.x JavaScript framework. There is no JQuery at all;
* Tested extensively on Firefox, Chrome, Edge. Will work on Internet Explorer, Opera and most 
  Firefox/Chromium browser derivatives;
* See the GitHub wiki of how to open in NetBeans;
* See the [RIF Web Application and Middleware Installation](../Installation/rifWebApplication) on how to set 
  up Tomcat, rifServices.war etc;
* All libraries need to be hard-wired as the RIF has to be standalone and not via CDNs (no internet 
  connection if running on a private network);
* There will be no map tiles on the private network (unless we cache them as planned). There is code for local 
  caching on the TopoJSON GridLayer, however it is very dependent on browser permissions and not in use;
* Various libraries are used, see index.html. Mainly, the Bootstrap library is used for modal dialogues, 
  Leaflet for map containers, D3 for graphs, ui-grid and ui-layout;
* There are no unit tests and the code has not been JS-Linted. Did not have the time or resources.

# General Layout of the RIF files 

The file are located in *rifWebApplication\src\main\resources*:

* backend/services
* css: stylesheets
* dashboards: core dashboard modals, divided into export, login, mapping, submission and viewer
  and then sub divided into: controllers, directives, partials and services
* images: images used by the RIF, divided into: colorBrewer, glyphicon and trees
* libs: libraries used by the RIF. Generally browserified code is is standalone and other libraries 
  including RIF modified ones are in the root (libs)
* modules: The RIF Angular module
* utils: common utilities, divided into controllers, directives, partials and services

## Naming Convention

All RIF specific files use the same convention: *rif(type)-(usedby)-(description).(extension)*

For example:

* **rifc-dmap.main.js**: *rif(controller)-(used by disease mapping dashboard)-(the main controller).(js file)*

File types can be:

| Suffix | Description	              | Extension |
|--------|----------------------------|-----------|
| c	 	 | Angular controller         | .js       |
| d		 | Angular directive	      | .js       |
| m		 | Angular module     	      | .js       |
| s		 | Angular service or factory | .js       |
| p		 | HTML partial               | .html     |
| x		 | CSS                        | .css      |

*rif[c/p/d/s]-&lt;specific dashboard or utility&gt;-&lt;name&gt;* E.g:

* rifp-dsub-main.html: partial for the main submission screen (with the four trees)
* rifc-dsub-main.js: controller for the main submission screen

They will be found in dashboards/submission.

The abbreviations [c/p/d/s] are:

* **c**: controller;
* **d**: directive;
* **p**: partial;
* **s**: service.

The specific dashboard or utility is:

* **dsub**: study submission dashboard in in *dashboards/submission*;
* **expt**: study export dashboard;
* **login**: login popup modal;
* **dmap**: dual map mapping dashboard;
* **view**: map and data viewer dashboard;
* **util**: utilities;

## Directory Structure

From the file root, the RIF has the following directory structure:

| Directory name | Description                                                               |
|----------------|---------------------------------------------------------------------------|
| backend        | Functionality to deal with the database via the middleware                |
| css            | RIF specific css                                                          |
| dashboards     | Functionality to deal with the main RIF tab states. e.g. viewer, login    |
| images         | Images used by the RIF                                                    |
| libs           | All third-party libraries - Hardwired in, do not use remote sources       |
| modules        | Contains the definition of the RIF angular module                         |
| utils          | Functionality shared over more than one part of the RIF                   |

In the root there is also the index.html. Here all the paths to third-party libraries are given (i.e the local libs directory), all RIF specific JavaScript files, css etc. The placeholder for the Angular content is also defined within a div in the html body - "data-ui-view" and also the directive for the notifications bar. Dashboards is further split into five directories, these represent the tabs seen in the RIF GUI, also in the RIF main module, these are the five states defined in the "$stateProvider" (see below). The utils directory has functionality that is shared by more than one of these dashboards, this is mostly to do with mapping of results.

Contained within these directories are separate folders for Angular controllers, directives, partials and services for the relevant part of the RIF.

# Libraries

| Library                                                                 | Files                                                                                                                                                             |
|-------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [AngularJS 1.5.8 ](https://angularjs.org/)                              | angular.min.js, angular-animate.min.js, angular-sanitize.min.js, angular-aria.min.js, angular-messages.min.js, angular-ui-router.min.js, angular-simple-logger.js |
| - [Angular Material Design components](https://material.angular.io/)    | angular-material.min.js, libs/standalone/angular-material.min.css                                                                                                 |
| - [UI Bootstrap modal windows](https://angular-ui.github.io/bootstrap/) | ui-bootstrap-tpls-2.3.0.min.js, bootstrap.min.css                                                                                                                 |
| - [UI Grid](http://ui-grid.info/)                                       | ui-grid.min.js, ui-grid.min.css                                                                                                                                   |
| - [UI Layout](https://github.com/angular-ui/ui-layout)                  | ui-layout.css                                                                                                                                                     |
| Leaflet 1.0.3](https://leafletjs.com/)                                  | leaflet.css, leaflet.js                                                                                                                                           |
| - [Leaflet draw](https://github.com/Leaflet/Leaflet.draw)               | leaflet.draw.css, leaflet.draw.js                                                                                                                                 |
| - [Leaflet fullscreen](https://github.com/Leaflet/Leaflet.fullscreen)   | Leaflet.fullscreen.min.js, /leaflet.fullscreen.css                                                                                                                |
| - [Leaflet map sync](https://github.com/jieter/Leaflet.Sync)            | L.Map.Sync.js                                                                                                                                                     |
| - [Leaflet map spinner](https://github.com/makinacorpus/Leaflet.Spin)   | spin.min.js, spin.js, spin.css, leaflet.spin.min.js                                                                                                               |
| [Aynsc library](https://caolan.github.io/async/)                        | async.js                                                                                                                                                          |
| [TopoJSON](https://github.com/topojson/topojson)                        | topojson.min.js                                                                                                                                                   |
| - TopoJSON GridLayer: Created from Leaflet.GeoJSONGridLayer             | topoJSON.js, TopoJSONGridLayer.js                                                                                                                                 |
| [D3 v3 and D3 export](https://d3js.org/)                                | d3.v4.min.js, canvas-toBlob.js, FileSaver.min.js                                                                                                                  |
	
* Angular 1.5.8 is ? releases behind current
* TopoJSON GridLayer created from Leaflet.GeoJSONGridLayer: https://github.com/ebrelsford/leaflet-geojson-gridlayer by Eric Brelsford 

```
<script src="libs/moment.min.js"></script> 
<script src="libs/leaflet-slider.js"></script> 
<link rel="stylesheet" href="libs/leaflet-slider.css"> 
<script type="text/javascript" src="libs/leaflet-control-condended-attribution.js"></script> 
<script type="text/javascript" src="libs/leaflet.geometryutil.js"></script>



<!-- THESE ONLY EVER AS STANDALONE LIBRARIES -->
<!-- Angular -->
<script src="libs/angular-simple-logger.js"></script>

<!-- Leaflet -->
<script src="libs/l.control.geosearch.js"></script>
<script src="libs/l.geosearch.provider.openstreetmap.js"></script>
<link rel="stylesheet" href="libs/l.geosearch.css" />
<script src="libs/leaflet-image.js"></script>        
<script src="libs/turf.min.js"></script>  
<script src="libs/pouchdb.js"></script>
<script src="libs/"></script>

<!-- proj4 (rep-projection) -->
<script src="libs/proj4.js"></script>

<!-- save html2canvas -->
<script src="libs/html2canvas.js"></script>

<!-- Simple Statistics -->
<script src="libs/sstatistics.js"></script>

<!-- UI layout grid -->
<!--This version of ui-layout has patch for sticky mouse issue #181-->
<script src="libs/ui-layout.js"></script>   

<!-- notification -->
<link rel="stylesheet" href="libs/ngNotificationsBar.css">
<script src="libs/ngNotificationsBar.min.js"></script>   

<!-- shapefile in leaflet -->
<script src="libs/leaflet.shpfile.js"></script>
<script src="libs/shp.min.js"></script>

<!-- ngPatternRestrict -->
<script src="libs/ng-pattern-restrict.min.js"></script>

<!-- JSON5 parser -->
<script src="libs/json5.js"></script>
```
For more information on:

* The modal windows: https://angular-ui.github.io/bootstrap/
* The split containers: https://github.com/angular-ui/ui-layout
* The notification bar: https://github.com/alexbeletsky/ng-notifications-bar
* The tables: http://ui-grid.info/
* The maps: http://leafletjs.com/
* The graphs: https://d3js.org/
* Some of the fancy stuff: https://material.angularjs.org/latest/

# The main module

rifm-app: The definition of the RIF angular module. This is where the "$stateProvider" states URLs are defined. 
These broadly relate to the dashboards selected by the tabs in the main RIF toolbar. All subsequent 
angular code chains to: angular.module("RIF").

**ADD NOTE ON THE STATE MACHINE**

## CSS

The RIF specific CSS is a bit of a mess and may have a lot of redundancy. This was inherited from the old 
RIF prototype. Neither DM or PH had time to go through it properly. How the split containers refresh on 
resize and on browser resize is now much improved. It is either jumpy at best, irresponsive at worst. The map
tables do not have ui-layout and do not resize correctly. This is a known issue (#???) in ui-layout and not 
the RIF.

##  Alerts

Alerts are handled in **rifc-util-alert**, which is right up at the root (on the body of index.html) of all 
the so easily available by inheritance to all other controllers/scopes in the RIF. The use of inheritance 
does not work in services modules and can be hard to predict especially for directives where they are re-used. 
A service was therefore created: **rifs-util-alert**. **rifc-util-alert** uses the html attribute: 
notifications-bar. Some of the errors thrown by the middleware can be a bit weird and not informative. The 
middleware should provide more human readable error messages.

Alerts are of two types: permanent (which have to be closed) and auto closing after five seconds. There is a modal 
from the submission modal which can view all alerts for a session. 

# Dashboards

Each dashboard for a user session has its state stored in a service (e.g. **rifs-dmap-mappingstate**). This is 
a singleton (with closure) and is not destroyed during a RIF session. The state service are used to either 
restore a user's choices on state changes (which destroys scope and controllers), but also to restore the 
RIF defaults if needed. The save/load study functions reads and writes to these singleton
states.

Each dashboard also has an html partial (e.g. **rifp-dmap-main**) which renders in div defined in *index.html* 
with the data-ui-view attribute. Changing the tab on the main RIF tab bar changes the state 
(**rifc-util-tabctrl**) as defined in the module (**rifm-app**).

Each dashboard has a main controller. These are often quite large and could be refactored in the future, in 
particular there is a lot of functionality that could be moved into services. For example, it is not very 
'angular' to have code that does not deal directly with the UI in the controller. In general, code has been
moved into new services. Specific directives usually refer to a unique feature such as a D3 plot 
(e.g. **rifd-dmap-d3rrzoom**).

Where there is shared functionality between dashboards, this is usually found in the utils directory. For 
example, choropleth mapping (**rifc-util-choro**), basemaps (**rifc-util-basemap**, available maps are 
defined in **rifs-util-basemap**), notifications (**rifc-util-alert**) etc.

## Login

**rifc-login-login**: Calls the login method for the RIF database. Ensures all states (i.e. previous user 
inputs) are reset, initialises the taxonomy service. On success we transition to "state1", i.e. the submission 
dashboard. On failure, we remain with the login page.

The logout method (click on the running man on the tool bar) is handled by (**rifc-util-tabctrl**) via a 
yes/no modal.

## Submission

**rifc-dsub-main**: Initially makes several chained calls to the database to fill all drop-downs etc depending 
on the user. Once loaded, several other controllers are responsible for entering user defined submission data. 
These all appear in bootstrap modal pop-ups. Each "tree" has at least its own controller, often multiple controllers.

The results of user selections are stored in the relevant states (see the submission/services). On clicking 
"Run", we use these states to populate a lump of JSON (**rifs-dsub-model**). This JSON object is then posted 
to the database with the submitStudy method in **rifs-back-requests** as submissionFile.txt. No actual 
calculations are done by the front-end. The save (**rifc-dsub-save**), reset (**rifc-dsub-reset**) and 
open (**rifc-dsub-fromfile**) RIF job methods work with this JSON and the associated state services.

Comparison and Study areas are defined with the same dialog defined by the maptable directive 
(**rifd-dsub-maptable**) launched by either the respective **rifc-dsub-comparea** or **rifc-dsub-studyarea** 
controllers. See **rifs-dsub-model** for how the areas selected are submitted - there is a key for "map_areas" 
in the JSON object relating to either study area or comparison area, within this an area object is stored with 
attributes: id, gid, label and band (for risk analysis).

So far this band attribute is not used as risk analysis cannot be done by the RIF backend yet. 
**rifs-dsub-model** also contains slots that vary depending if the study type is disease mapping or risk 
analysis (disease_mapping_study, risk_analysis_study). As risk mapping has not been done yet, how the two 
different study types are recognised may need to be thought through again. The database cannot handle 
"risk_analysis_study" and will throw an error. All this depends on how risk analysis will be handled in the 
database and middle ware.

Using rifd-dsub-maptable, areas can be selected at the required resolution,

* By clicking on the table or map (synced with a $watchCollection)
* By drawing polygons and concentric circles (uses the LeafletDraw library modified in **rifs-util-leafletdraw** to broadcast on
  *$rootScope*)
* By uploading a csv list of districts in the format: [ID, band] (**rifc-dsub-idlist**)
* By selecting with a zipped shapefile (see **rifd-dsub-risk**). Various methods are possible; defining buffers 
  around a point file, defining selection by polygon extent, defining selection by polygon attribute.
* Note that selection based on shapefile/polygon overlay is determined on the basis of intersection with the 
  districts centroid and the overlay. Essentially a simple point-in-polygon test: **rifs-util-gis**.
* Study area for risk analysis allows 1:6 bands, disease mapping just one band. Comparison areas always only 
  allow one band. It is possible to select comparison areas using shapefiles
* Methods to select layers will be refined once we have some user feedback

Investigation parameters is where the taxonomy service is used. This is a standalone service due to copyright 
issues with the ICD10 codes (we cannot distribute this on github). ICD 9 and 10 are the only services available; 
more taxonomies are planned to be incorporated. This dialog can now handle multiple covariates and in future 
could use set lists of ICD codes and support multiple investigations. The taxonomy list is a ui-grid table. 
UI-grid now supports "hierarchial" rows (in beta) and so the display could be improved to show a hierarchy:

* C: All malignant cancers
  * C3:
    * C33:
      * C341:

Again, several calls are made to the middle ware (in **rifc-dsub-params**) to fill the drop-downs and results 
are saved in **rifs-dsub-paramstate** for submission. This part of the front-end will probably need the most 
work after the backend and middle ware are updated.

Statistical methods (**rifc-dsub-stats**) selects which smoothing method to run. Possible methods are defined in
the middleware (not hard-typed). So far we have HET, BYM, CAR and none. It is also possible to store parameters 
for the model, but this is not used at the moment.

Prior to submission, we can get an HTML formatted summary of the RIF job (**rifc-dsub-summary**) using the tags 
in **rifc-dsub-model** area table function. The model here is of JSON structure and is posted as a text file. 
To get the structure of this, use the 'save' option on the GUI to download a current job as a .json file. The 
model json structure has changed slightly in detail during RIF development, all changes are generally back 
compatible.

On submission, the job is run in the database behind the scenes. Clicking status (**rifc-dsub-status**) gives 
the status for a user's RIF jobs. Status is also checked every 4 seconds to give a notification of c
ompletion of any pending jobs (this is done in by the tab controller **rifc-util-tabctrl**). This can report a study 
completing twice, this is because the main timer loop is "stacking" up and the code needs to be modified to 
prevent this.

## Mapping and Viewer

These dashboards are very similar so will be explained together. During development, I could not get a straight 
answer as to what the difference with these should be and what actually is required, the two-tab approach is 
directly taken from Fred Fabbri's initial prototype. Hopefully with some user feedback, the functionality of 
these should be refined.

The main difference is that the viewer allows a map and table view, while the mapper allows two maps and focuses
on "bow-tie" plots of risks. Both allow choropleth mapping (**rifc-util-choro**) and changeable basemaps in 
Leaflet (**rifc-util-basemap**). Mapping and Viewer dashboards have their own main controllers 
(**rifc-dmap-main** and **rifc-view-viewer**), but the mapping itself is governed by the **rifc-util-mapping** 
controller as functionality is shared. The appropriate Leaflet container (div) is referenced as either
"diseasemap1", "diseasemap2" or "viewermap".

Polygons are served up in topoJSON tiles as created by Peter's tilemaker as an *L.topoJsonGridLayer*. This is 
very similar to handling normal geoJSON, just the call to the middle ware must be defined correctly: getTileMakerTiles. Once loaded, the tiled layer behaves as
any geoJSON in Leaflet. This has since been extended to use bitmap tiles for all the areas in the geolevel not 
selected/part of a study. This code has only been fitted to the study and comparison selectors and requires 
more work to handle mous events efficiently. 

There are a selection of mapping and selection tools as directives, see **rifd-util-leafletTools**. There are 
also tools to save the D3 plots to png (**rifd-util-savechart**). There exists a directive to save the Leaflet 
map with overlays in **rifd-util-leafletTools** (leafletToPng). This does not work very well and is 
inconsistent between browsers, it will need looking at. I think that it should actually be removed in favour 
of the export tools (explained later) or prompting the user to go full screen and use the screen dump (Print 
Scrn button) to clipboard.

(Note that the directive used for the map area submissions, **rifd-dsub-maptable** duplicates a lot of the 
functionality used here. It was always the intention to refactor **rifd-dsub-maptable** to be more consistent 
with **rifc-util-mapping** and its associated directives).

All D3 plots are dealt with using directives to define a new HTML element. Note that these still need wrapping 
in a *$watch* to allow them to refresh. Capturing the keyboard events in angular for multiple maps and D3 
graphs was quite challenging, there may still be bugs here.

The processed study being mapped are selected via the drop-downs. There is an info button next to these to 
display study details (**rifd-util-info**). This needs some work still on the back-end as not all of the 
relevant information is stored and/or retrieved, see the method getDetailsForProcessedStudy.

Choropleth mapping uses colour scales defined by colorbrewer.org (**rifs-util-colorbrewer**). Category 
definitions follow the usual methods; equal interval, quantile etc (**rifs-util-choro**). The code to do the 
rendering is a bit messy and could do with a tidy (**rifc-util-choro**)

## Export

To get all the results out of the RIF in a zip file (**rifc-expt-export**). All the zipping etc. is done in the 
middle ware. So far this exports the map table (RIF results) and the extract table (data used by the RIF to 
make the results) as csv files. The study and comparison areas are also exported as geoJSON files (text, can 
be loaded into any GIS and converted to a shapefile). Other tables could be exported here too, e.g. a 
formatted table for input to SatScan.

This dashboard also allows a quick preview of the data and the map areas. This would need modification after 
user feedback.

After user testing, there will be loads of requests as to what needs to be outputted ("what-the-old-RIF-did" 
etc. etc.)

## Backend

Deals with everything related to the connection with the RIF postgres or sqlserver databases via the Java 
middleware.

* **rifs-back-database**: A service storing which database is being used (postgres or ms sqlserver). This is 
  defined in the rifServces.war (resources > properties file) and is therefore not editable from the front-end. 
  This may not be needed in future depending on how the middle ware is refactored, i.e the /ms or /pg may be 
  dropped from the URLs.

* **rifs-back-interceptor**: A "$httpInterceptor" service to deal with outgoing RIF specific requests. This 
  checks if a user is logged in and checks for a 200 response on return of promise. Handles any problems with 
  the calls.

* **rifs-back-requests**: Contains ALL the requests to the middle ware for information from the database for 
  the whole RIF. These return a promise (in most cases).

* **rifs-back-urls**: Contains the constants for the base URLs to the rifServices. These can be edited here depending on the localhost used.
  See also RIF wiki set up instructions.

# Utilities


**Peter Hambly, April 2019**