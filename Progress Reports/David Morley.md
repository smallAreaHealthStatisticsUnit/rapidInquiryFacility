# David Morley Progress Report RIF 4.0

Principal Work Area: JavaScript User interface development

- Started in position 1st June 2016

# 2016
## June

- Taken over the front-end code of Federico Fabbri
- Code refactoring using AngularJS to reduce complexity, code volume and make more maintainable
- Tidying up of redundant and/or duplicate CSS code
- Submission study map selection tools:
	- Table and map now automatically synchronise on user events
	- Added map selection using user-drawn polygon
	- Added map selection using user-drawn concentric circles (exposure bands)
	- Selected areas may now be defined a band number
- Added a search by address tool to all Leaflet maps
- Preparing interface to be connected to back-end using angular $http service
- Testing a new login method using angular $httpInterceptors and tokens
- Added resizable containers to all dashboards
- Created placeholders/wrappers for all D3 charts FAO KG


## July

- Now using local version of RIF database
- adding calls to the middleware
- Study submission JSON object almost complete
- Choropleth mapping for a test dataset in dataviewer
- Integrated taxonomy service into parameters modal

## August

- Added histogram to choropleth map break selector
- Finished adding split-containers to dashboards
- Statistical method dialogue completed
- Finished login methods and added permission check to outgoing requests
- Started status report viewer
- Continued looking at D3 library with KG

## September

- Added histogram of breaks to choropleth maps
- Added capability to save a RIF submssion and load it back in
- Contiued adding middleware methods and removing hardtyping
- Added container for status updates
- Improved look of interface CSS
- Work with KG on D3 graphs for data viewer

## October

- All d3 charts added and are linked/interactive
- All data comes from database - no hardtyping
- Improved dialogue for disease parameters
- UI is end-to-end study run ready

## November

- New disease mapper with two 'Atlas' style maps
- Update from leaflet 0.7 to 1.0
- Export map to png feature
- Export as CSV option in results viewer
- Started looking at RIF Java classes

## December

- Marquee style progress button for long login process
- Added initial methods to import a AOI shapefile for disease mapping
- Posterior Probability graph in disease mapper
- Export D3 graphs to png buttons (not yet for IE11)
- Atlas colour scheme options for choropleth mapping
- Study status modal linked to database

# 2017
## January

- Refactoring of mapping and ui-grid code
- Added middle ware method to get geography info for complete study
- Polling of study status using $interval to notify on completion of submitted job
- Modified getSmoothedResults method to not need a year
- Population pyramids plotable by year
- Adding colour swatches to choropleth maps
- UK postcode base layers

## February

- More refactoring
- Browser compatibility fixes
- Middle ware method getTileMakerTiles to get topojson by leaflet gridLayer
- Tiled topojson now used in front end
- Debugging of R smoothing script to work with RIF submission options

## March

- getTileMakerTiles middle ware method finalised for new DB schema
- Fixing various rifService bugs with KG
- Risk analysis methods for selecting areas from shapefiles started
- Removal of old middleware methods
- Migrate all CDN libraries to standalone scripts
- Started study information recall method

## April

- Working full time on middle ware in April
- Changes to rifServices architecture to handle 2 DB types
- MS SQL server porting for study submission complete
- Started working on MS SQL server port for data viewer / disease mapping

## May

- Moved rifServices test cases to either sql server of pgres versions
- sql server porting for run Study (mostly problems with error checking routines)
- Fixed viewer sex and year for study drop-down middleware methods
- Removed dependency on ui-leaflet directive
- Fixed memory leak in map containers
- Middleware method to get polygon centroids from database (defaults to leaflet getCenter() on error)
- New toolbars for map consistent with standard leaflet tools
- Correct resizing issues in KG's D3 code
- Main navbar is now responsive to page resize

## June

- JRI method to run R scripts in Java
- Fix export to Zip File methods for map and extract tables
- New methods to download polygons as geoJSON
- New export tab on front-end
- Methods to get info on a previous study (still needs work in back-end)
- Various ad hoc middleware fixes requested by PH
- Created System Validation Document

## July

- Removing redundant code from rifServices
- Demos to possible RIF users and/or testers

## (August)


