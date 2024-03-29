// ************************************************************************
//
// GIT Header
//
// $Format:Git ID: (%h) %ci$
// $Id: 7ccec3471201c4da4d181af6faef06a362b29526 $
// Version hash: $Format:%H$
//
// Description:
//
// Rapid Enquiry Facility (RIF) - Tile viewer basemap code
//
// Copyright:
//
// The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
// that rapidly addresses epidemiological and public health questions using 
// routinely collected health and population data and generates standardised 
// rates and relative risks for any given health outcome, for specified age 
// and year ranges, for any given geographical area.
//
// Copyright 2014 Imperial College London, developed by the Small Area
// Health Statistics Unit. The work of the Small Area Health Statistics Unit 
// is funded by the Public Health England as part of the MRC-PHE Centre for 
// Environment and Health. Funding for this project has also been received 
// from the Centers for Disease Control and Prevention.  
//
// This file is part of the Rapid Inquiry Facility (RIF) project.
// RIF is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// RIF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
// to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
// Boston, MA 02110-1301 USA
//
// Author:
//
// Peter Hambly, SAHSU

var controlLayers;
var autoCompaction=true;
var useCache=true;
	
/*
 * Function: 	Basemap()
 * Parameters:	basemapOptions, mapArrays object
 * Returns:		Basemap() Object
 * Description:	Create Basemap object
 */	
function Basemap(basemapOptions, mapArrays) { 
	this.name=basemapOptions.name||"UNK";
	this.tileLayer=undefined;
	this.useCacheDefault=false;
	mapArrays.basemapArray.push(this);
	
	if (basemapOptions.tileLayerType && basemapOptions.tileLayerOptions) {
		this.tileLayerType=basemapOptions.tileLayerType;
		this.tileLayerURL=basemapOptions.tileLayerURL;
		this.tileLayerOptions=basemapOptions.tileLayerOptions;
		if (basemapOptions.tileLayerOptions.useCache) {
			this.useCacheDefault=basemapOptions.tileLayerOptions.useCache;
		}
	}
	
} // End of Basemap() object constructor
	Basemap.prototype = { // Add methods
		/*
		 * Function: 	createBaseMap()
		 * Parameters:	mapArrays object
		 * Returns:		Nothing
		 * Description:	Add basemap to basemap array
		 */	
		createBaseMap: function(mapArrays) {
			if (this.tileLayerType && this.tileLayerOptions) {
				if (this.tileLayerType == "tileLayer" && this.tileLayerOptions) {
					this.tileLayer=L.tileLayer(this.tileLayerURL, this.tileLayerOptions);
					if (this.tileLayer) {
//						consoleLog("Basemap() constructor: created tileLayerType: " + 
//							this.tileLayerType + " for: " + this.name);
					}
					else {
						consoleError("Basemap() constructor: no tileLayer object can be created, tileLayerType: " + 
							this.tileLayerType + " for: " + this.name);
					}
				}
				else if (this.tileLayerType == "googleMutant") {
					this.tileLayer=L.gridLayer.googleMutant(this.tileLayerOptions);
					if (this.tileLayer) {
						consoleLog("Basemap() constructor: created tileLayerType: " + 
							this.tileLayerType + " for: " + this.name);
					}
					else {
						consoleError("Basemap() constructor: no tileLayer object can be created, tileLayerType: " + 
							overlaymapOptions.tileLayerType + " for: " + this.name);
					}
				}	
				else {
					this.tileLayer=undefined;
					consoleError("Basemap() constructor: no tileLayer object can be created, invalid tileLayerType: " + 
						this.tileLayerType + " for: " + this.name);
				}
			}
			else {
				this.tileLayer=undefined;
				consoleError("Basemap() constructor: no tileLayer object can be created, missing tileLayerType/tileLayerURL/tileLayerOptions for: " + 
					this.name);
			}
			
			if (this.tileLayer) {	
				this.tileLayer.mapArrays=mapArrays; // Add pointer to container object
				this.cacheStats={
					hits: 0,
					misses: 0,
					errors: 0,
					tiles: 0,
					size: 0
				};
			}
			
			if (this.tileLayer.mapArrays) {
				this.mapArrays=mapArrays;
			}
			else {
				consoleError("Basemap() constructor: no mapArrays object for: " + this.name);
			}
	
			var nBaseLayer=this;			
			this.tileLayer.on('tileerror', function(tile) {
				if (nBaseLayer && nBaseLayer.cacheStats) {
					nBaseLayer.cacheStats.errors++;
					consoleError("Error: loading " + nBaseLayer.name + " baselayer tile: " + (JSON.stringify(tile.coords)||"UNK") +
						"; total errors: " + nBaseLayer.cacheStats.errors);
				}
				else {
					consoleError("Error: loading " + nBaseLayer.name + " baselayer tile: " + (JSON.stringify(tile.coords)||"UNK") + 
						" [No stats update]");
				}
			});
	
			this.tileLayer.on('tilecachehit', function tileCacheHitHandler(ev) {
				if (nBaseLayer && nBaseLayer.cacheStats) {
					nBaseLayer.cacheStats.hits++;
//					consoleLog("tileCacheHitHandler(): Cache hit " + nBaseLayer.name + " tile: " + ev.url +
//						"; total hits: " + nBaseLayer.cacheStats.hits);
				}
				else {
					consoleLog("tileCacheHitHandler(): Cache hit " + nBaseLayer.name + " tile: " + ev.url + " [No stats update]");
				}
			});
			
			this.tileLayer.on('tilecachemiss', function tileCacheMissHandler(ev) {
				if (nBaseLayer && nBaseLayer.cacheStats) {
					nBaseLayer.cacheStats.misses++;
					consoleLog("tileCacheMissHandler(): Cache miss " + nBaseLayer.name + " tile: " + ev.url+
						"; total misses: " + nBaseLayer.cacheStats.misses);
				}
				else {
					consoleLog("tileCacheMissHandler(): Cache miss " + nBaseLayer.name + " tile: " + ev.url + " [No stats update]");
				}
			});

			this.tileLayer.on('tilecacheerror', function tileCacheErrorHandler(ev) {
				if (nBaseLayer && nBaseLayer.cacheStats) {
					nBaseLayer.cacheStats.errors++;
					if (this.mapArrays && this.mapArrays.options) {
						this.options.useCache=false;
						this.mapArrays.options.useCache=false;
						consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + nBaseLayer.name + ": " + ev.tile+
							"; total errors: " + nBaseLayer.cacheStats.errors + "; all caching disabled");
					}
					else {
						consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + nBaseLayer.name + ": " + ev.tile+
							"; total errors: " + nBaseLayer.cacheStats.errors);
					}
				}
				else {
					consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + nBaseLayer.name + ": " + ev.tile + 
						" [No stats update]");
				}
			});
					
		}
	}
			
/*
 * Function: 	Overlaymap()
 * Parameters:	overlaymapOptions, mapArrays object
 * Returns:		Basemap() Object
 * Description:	Create Basemap object
 */	
function Overlaymap(overlaymapOptions, mapArrays) { 
	this.name=overlaymapOptions.name||"UNK";
	
	if (overlaymapOptions.tileLayerType && overlaymapOptions.tileLayerURL && overlaymapOptions.tileLayerOptions) {
		this.tileLayerType=overlaymapOptions.tileLayerType;
		this.tileLayerURL=overlaymapOptions.tileLayerURL;
		this.tileLayerOptions=overlaymapOptions.tileLayerOptions;
		if (overlaymapOptions.tileLayerType == "tileLayer" && overlaymapOptions.tileLayerOptions) {
			this.tileLayer=L.tileLayer(overlaymapOptions.tileLayerURL, overlaymapOptions.tileLayerOptions);
			if (this.tileLayer) {
				consoleLog("Overlaymap() constructor: created tileLayerType: " + 
					overlaymapOptions.tileLayerType + " for: " + this.name);
			}
			else {
				consoleError("Overlaymap() constructor: no tileLayer object can be created, tileLayerType: " + 
					overlaymapOptions.tileLayerType + " for: " + this.name);
			}
		}
		else {
			this.tileLayer=undefined;
			consoleError("Overlaymap() constructor: no tileLayer object can be created, invalid tileLayerType: " + 
				overlaymapOptions.tileLayerType + " for: " + this.name);
		}
	}
	else {
		this.tileLayer=undefined;
		consoleError("Overlaymap() constructor: no tileLayer object can be created, missing tileLayerType/tileLayerURL/tileLayerOptions for: " + 
			this.name);
	}
	
	if (this.tileLayer) {	
		this.tileLayer.mapArrays=mapArrays; // Add pointer to container object
		this.cacheStats={
			hits: 0,
			misses: 0,
			errors: 0,
			tiles: 0,
			size: 0
		};
	}
	
	if (this.tileLayer.mapArrays) {
		this.mapArrays=mapArrays;
	}
	else {
		consoleError("Overlaymap() constructor: no mapArrays object for: " + this.name);
	}

	var nOverlayLayer=this;	
	this.tileLayer.on('tileerror', function(tile) {
		if (nOverlayLayer && nOverlayLayer.cacheStats) {
			nOverlayLayer.cacheStats.errors++;
			consoleError("Error: loading " + nOverlayLayer.name + " overlay tile: " + (JSON.stringify(tile.coords)||"UNK") +
				"; total errors: " + nOverlayLayer.cacheStats.errors);
		}
		else {
			consoleError("Error: loading " + nOverlayLayer.name + " overlay tile: " + (JSON.stringify(tile.coords)||"UNK") + 
				" [No stats update]");
		}
	});
	
	this.tileLayer.on('tilecachehit', function tileCacheHitHandler(ev) {
		if (nOverlayLayer && nOverlayLayer.cacheStats) {
			nOverlayLayer.cacheStats.hits++;
//			consoleLog("tileCacheHitHandler(): Cache hit " + nOverlayLayer.name + " tile: " + ev.url +
//				"; total hits: " + nOverlayLayer.cacheStats.hits);
		}
		else {
			consoleLog("tileCacheHitHandler(): Cache hit " + nOverlayLayer.name + " tile: " + ev.url + " [No stats update]");
		}
	});
	
	this.tileLayer.on('tilecachemiss', function tileCacheMissHandler(ev) {
		if (nOverlayLayer && nOverlayLayer.cacheStats) {
			nOverlayLayer.cacheStats.misses++;
//			consoleLog("tileCacheMissHandler(): Cache miss " + nOverlayLayer.name + " tile: " + ev.url+
//				"; total misses: " + nOverlayLayer.cacheStats.misses);
		}
		else {
			consoleLog("tileCacheMissHandler(): Cache miss " + nOverlayLayer.name + " tile: " + ev.url + " [No stats update]");
		}
	});

	this.tileLayer.on('tilecacheerror', function tileCacheErrorHandler(ev) {
		if (nOverlayLayer && nOverlayLayer.cacheStats) {
			nOverlayLayer.cacheStats.errors++;
			if (this.mapArrays && this.mapArrays.options) {
				this.options.useCache=false;
				this.mapArrays.options.useCache=false;
				consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + nOverlayLayer.name + ": " + ev.tile+
					"; total errors: " + nOverlayLayer.cacheStats.errors + "; all caching disabled");
			}
			else {
				consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + nOverlayLayer.name + ": " + ev.tile+
					"; total errors: " + nOverlayLayer.cacheStats.errors);
			}
		}
		else {
			consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + nOverlayLayer.name + ": " + ev.tile + 
				" [No stats update]");
		}
	}); 

	mapArrays.overlaymapArray.push(this);
	
} // End of Overlaymap() object constructor

/*
 * Function: 	mapArray() 
 * Parameters:  map, defaultBaseMap, maxZoomlevel
 * Returns:		mapArray() Object
 * Description: Constructor for mapArrays
 */
function mapArrays(map, defaultBaseMap, maxZoomlevel, options) {
		this.basemapArray=[];
		this.overlaymapArray=[];
		
		this.cacheSize=0;
		this.totalTiles=0;					
		this.pouchDB=undefined;
		
		if (options && options.auto_compaction) {
			this.options.auto_compaction=options.auto_compaction;
		}
		this.options.useCache=true; // Default
		if (options && options.useCache) {
			this.options.useCache=options.useCache;
		}		

		try {
			this.initBaseMaps(map, defaultBaseMap, maxZoomlevel);
		}
		catch (e) {
			consoleError("Caught error in initBaseMaps(): " + JSON.stringify(e));
		}
	} // End of mapArrays() object constructor
	
	mapArrays.prototype = { // Add methods
		options:	{
			auto_compaction: (autoCompaction||false),	// option auto_compaction: true/false
			// This turns on auto compaction, which means compact() is called after every change to the database. Defaults to false.
			useCache: (useCache||true)	// option useCache: true/false
			// This turns on caching. Defaults to true. Disabled by PouchDB errors
		},
		/*
		 * Function: 	addBaseMap()
		 * Parameters:	baseMap object
		 * Returns:		Nothing
		 * Description:	Add basemap to basemap array
		 */	
		addBaseMap: function(baseMap) {
			this.basemapArray.push(baseMap);
		},	
		/*
		 * Function: 	addOverlayMap()
		 * Parameters:	overlayMap object
		 * Returns:		Nothing
		 * Description:	Add overlayMap to overlayMap array
		 */	
		addOverlayMap: function(overlayMap) {
			this.overlaymapArray.push(overlayMap);
		},	
		/*
		 * Function: 	initBaseMaps()
		 * Parameters:	map, defaultBaseMap, maxZoomlevel
		 * Returns:		Nothing
		 * Description:	Initialise base and overlay maps
		 */	
		initBaseMaps: function(map, defaultBaseMap, maxZoomlevel) {

			var foundDefault=undefined;		
			var currentBaseMap;	
			var layerList = {};
			
			if (defaultBaseMaps) {
				for (var i=0; i<defaultBaseMaps.length; i++) {
					defaultBaseMaps[i].tileLayerOptions.auto_compaction=this.options.auto_compaction;
					if (this.options.useCache && defaultBaseMaps[i].tileLayerOptions.useCache) {
						defaultBaseMaps[i].tileLayerOptions.useCache=this.options.useCache;
					}
					var basemap=new Basemap(defaultBaseMaps[i], this);
					if (basemap.name == defaultBaseMap) {
						foundDefault=i;
						basemap.createBaseMap(this);
					}
				}
				if (foundDefault == undefined) {
					foundDefault=0;
					defaultBaseMaps[0].createBaseMap(this); // Use first
				}
				
				var defBaseMap=this.basemapArray[foundDefault];
				defBaseMap.tileLayer.name=defBaseMap.name;
				layerList[defBaseMap.name]=defBaseMap.tileLayer;
				if (this.pouchDB == undefined && defBaseMap.tileLayer._db) {
					this.pouchDB=defBaseMap.tileLayer._db;
				}
				currentBaseMap=layerList[defBaseMap.name]
				consoleLog("initBaseMaps(): foundDefault " + foundDefault + 
					": " + defBaseMap.name +
					"; options: " + JSON.stringify(defBaseMap.tileLayerOptions));
				var mapArray=this;
				
				currentBaseMap.on('load', function currentBaseMapLoad(ev) {
					
					if (baseLayer.options && baseLayer.options.useCache) {						
						consoleLog("initBaseMaps(): Base layer loaded: " + baseLayer.name + "; cached" + "; default: " + defaultBaseMap);
					}
					else {
						consoleLog("initBaseMaps(): Base layer loaded: " + baseLayer.name + "; not cached" + "; default: " + defaultBaseMap);
					}
					
					var ChangeUseCacheToFalse=false;
					if (baseLayer.options && baseLayer.options.useCache == false &&
						baseLayer.options.useCache != baseLayer.useCacheDefault) {
						consoleLog("initBaseMaps(): useCache changed to false for: " + baseLayer.name);
						ChangeUseCacheToFalse=true;
					} 
		
					for (var i=0; i<mapArray.basemapArray.length; i++) { // Initialise rest
						if (mapArray.basemapArray[i].tileLayer == undefined) {
							if (ChangeUseCacheToFalse) {
								mapArray.basemapArray[i].tileLayerOptions.useCache=baseLayer.options.useCache;
							}
							mapArray.basemapArray[i].createBaseMap(mapArray);
							mapArray.basemapArray[i].tileLayer.name=mapArray.basemapArray[i].name;
							layerList[mapArray.basemapArray[i].name]=mapArray.basemapArray[i].tileLayer;
							if (mapArray.pouchDB == undefined && mapArray.basemapArray[i].tileLayer._db) {
								mapArray.pouchDB=mapArray.basemapArray[i].tileLayer._db;
							}						
						}
					}		
						
					controlLayers=L.control.layers(
						layerList, 		// Base layers 
						overlayList, 	// Overlays
					{
						position: 'topright',
						collapsed: true
					});	
					controlLayers.addTo(map); 
						
					map.on('baselayerchange', function baselayerchangeEvent(changeEvent) {
						baseLayer=changeEvent.layer;
						if (changeEvent.layer.mapArrays && baseLayer && baseLayer.options && baseLayer.options.useCache) { // Caching enabled
							changeEvent.layer.mapArrays.getCacheSize();
						}
						if (changeEvent.layer._db) {	
							changeEvent.layer._db.info(
								function getInfo(err, result) {		
									if (err) {
										consoleError("Error: " + err.message + " in _db.info() for base layer: " + changeEvent.layer.name);			
									}
									else {
										consoleLog("base layer changed to: " + changeEvent.layer.name + "; cache info: " + 
											JSON.stringify(result));
									}							
								});
						}
						else {
							consoleLog("baselayerchangeEvent(): Base layer changed to: " + changeEvent.layer.name + "; not cached");
						}
						document.getElementById("legend_baseLayer_value").innerHTML=changeEvent.layer.name;	
					});
					map.on('overlayadd', function overlayAddEvent(changeEvent) {
						consoleLog("overlayAddEvent(): overlay added: " + changeEvent.name);
					});
					map.on('overlayremove', function overlayRemoveEvent(changeEvent) {
						consoleLog("overlayRemoveEvent(): overlay removed: " + changeEvent.name);
					});
									
					currentBaseMap.off('load');							
				});
				
				baseLayer = currentBaseMap;
				currentBaseMap.addTo(map);					
			}
			else {
				errorPopup(new Error("initBaseMaps(): Cannot load: " + defaultBaseMap + "; no defaultBaseMaps"));
				return;
			}

			//Additional overlays
			if (defaultOverlayMaps) {
				for (var i=0; i<defaultOverlayMaps.length; i++) {
					defaultOverlayMaps[i].tileLayerOptions.auto_compaction=this.options.auto_compaction;
					if (this.options.useCache && defaultOverlayMaps[i].tileLayerOptions.useCache) {
						defaultOverlayMaps[i].tileLayerOptions.useCache=this.options.useCache;
					}
					new Overlaymap(defaultOverlayMaps[i], this);
				}
			}	
			else {
				errorPopup(new Error("initBaseMaps(): Cannot load overlays; no defaultOverlayMaps"));
				return;
			}		
				
			var overlayList = {};
			for (var i=0; i<this.overlaymapArray.length; i++) {
				this.overlaymapArray[i].tileLayer.name=this.overlaymapArray[i].name;
				overlayList[this.overlaymapArray[i].name]=this.overlaymapArray[i].tileLayer;
			}
			
			if (currentBaseMap == undefined) {
				errorPopup(new Error("initBaseMaps(): Cannot load basemap, no currentBaseMap"));
			}
		}, // End of initBaseMaps()
		/*
		 * Function: 	empty()
		 * Parameters:	callback(err, results)
		 * Returns:		Nothing (use callback to access this.cacheSize, this.totalTiles)
		 * Description:	Add basemap to basemap array
		 *				Then does a viewCleanup()
		 */	
		empty: function(emptyCallback) {
			
			scopeChecker({
				callback: emptyCallback
			})
			
			if (this.pouchDB) {

				var mapArrays=this;
				mapArrays.pouchDB.info(
					function getInfo(err, result) {			
						if (err) {
							consoleError("empty(): Error: " + err.message + " in _db.getInfo()");
							emptyCallback(err);
						}
						else {
							$( "#progressbar" ).progressbar({
								max: result.doc_count
							});
							
							mapArrays.pouchDB.allDocs({
									include_docs: true,
									attachments: false
								}, 
								function getAllDocs(err, result) {
									if (err) {
										consoleError("empty(): Error: " + err.message + " in _db.allDocs()");
										emptyCallback(err);
									}
									else {
										mapArrays.cacheSize=0;
										mapArrays.totalTiles=result.total_rows;
										var i=0;

										async.eachSeries(result.rows, 
											function emptyAsyncEachSeriesHandler(item, callback) {
												i++;	
												if (i%10 == 0) {
													$( "#progressbar" ).progressbar({
														value: i
													});
												}
											
												if (item.doc) {
													if (mapArrays.basemapArray) {
														for (var j=0; j<mapArrays.basemapArray.length; j++) {
															if (mapArrays.basemapArray[j].name == item.doc.name) {
																mapArrays.basemapArray[j].cacheStats.tiles=0;
																mapArrays.basemapArray[j].cacheStats.size=0;
																break; // Out of for loop
															}
														}
													}	
													if (mapArrays.overlaymapArray) {
														for (var j=0; j<mapArrays.overlaymapArray.length; j++) {
															if (mapArrays.overlaymapArray[j].name == item.doc.name) {
																mapArrays.overlaymapArray[j].cacheStats.tiles=0
																mapArrays.overlaymapArray[j].cacheStats.size=0;
																break; // Out of for loop
															}
														}
													}	
													mapArrays.pouchDB.remove(item.doc, undefined /* Options */, callback); 
												}
												else {
													throw new Error("emptyAsyncEachSeriesHandler: no doc in item: " + 
														JSON.stringify(item).substring(0, 200));
												}			
											}, 
											function emptyAsyncEachSeriesError(err) {
												if (err) {
													emptyCallback(err);
												}
												else {
													consoleLog("emptyAsyncEachSeriesError() deleted: " + mapArrays.totalTiles + " tiles");
													var results={ totalTiles: mapArrays.totalTiles };
													$( "#progressbar" ).progressbar({
														value: result.total_rows
													});
													mapArrays.compact(function() {
														emptyCallback(undefined, results);
													});
													
													mapArrays.totalTiles=0;
												}
											}
										);
									}
								}
							);							
						}
					}	
				);				

			}
			else {
				emptyCallback();
			}
		},
		/*
		 * Function: 	viewCleanup()
		 * Parameters:	callback (mandatory)
		 * Returns:		Nothing 
		 * Description:	Triggers a viewCleanup operation in the local pouchDB database
		 */	
		viewCleanup: function(viewCleanupCallback) {
			
			scopeChecker({
				callback: viewCleanupCallback
			})
			
			if (this.pouchDB && this.options.auto_compaction == false) {
				this.pouchDB.viewCleanup(viewCleanupCallback)
			}
			else {
				viewCleanupCallback();
			}
		},
		/*
		 * Function: 	compact()
		 * Parameters:	callback (mandatory)
		 * Returns:		Nothing 
		 * Description:	Triggers a compaction operation in the local pouchDB database if auto_compaction is not on
		 *				Then does a viewCleanup()
		 */	
		compact: function(compactCallback) {
			
			scopeChecker({
				callback: compactCallback
			})
			
			if (this.pouchDB && this.options.auto_compaction == false) {
				var mapArrays=this;
				this.pouchDB.compact(undefined /* Options */, 
					function() {
						mapArrays.pouchDB.viewCleanup(compactCallback); 
					});
			}
			else {
				compactCallback();
			}
		},
		/*
		 * Function: 	getCacheSize()
		 * Parameters:	callback(err, results) (optional)
		 * Returns:		Nothing (use callback to access this.cacheSize, this.totalTiles)
		 * Description:	Get cacheSize; returns data as results in callback:
		 *					results: {
		 *						tableHtml: 		tableHtml,
		 *						totalTiles: 	mapArrays.totalTiles,
		 *						cacheSize:		mapArrays.cacheSize,
		 *	 					autoCompaction:	baseLayer.options.auto_compaction
		 *					} 
		 *				Then does a viewCleanup()
		 */	
		getCacheSize: function(getCacheSizeCallback) {
			
			scopeChecker(undefined, {
				callback: getCacheSizeCallback
			})
			
			var mapArrays=this;
			
			if (this.pouchDB) {
				mapArrays.pouchDB.info( // convert to promises form
					function getInfo(err, result) {			
						if (err) {
							var nerr=new Error("getCacheSize(): Error: " + (err.reason || JSON.stringify(err)) + " in _db.getInfo()");
							consoleError(nerr.message);
							if (getCacheSizeCallback) {
								getCacheSizeCallback(nerr);
							}		
						}
						else {
							$( "#progressbar" ).progressbar({
								value: false
							});
							
							mapArrays.pouchDB.allDocs({
									include_docs: true,
									attachments: true
								}, 
								function getAllDocs(err, result) {
									if (err) {
										consoleError("getCacheSize(): Error: " + err.message + " in _db.allDocs()");
										if (getCacheSizeCallback) {
											getCacheSizeCallback(err);
										}		
									}
									else {
										mapArrays.cacheSize=0;
										mapArrays.totalTiles=result.total_rows;
										for (var j=0; j<mapArrays.basemapArray.length; j++) {
											mapArrays.basemapArray[j].cacheStats.tiles=0;
											mapArrays.basemapArray[j].cacheStats.size=0;
										}
										for (var j=0; j<mapArrays.overlaymapArray.length; j++) {
											mapArrays.overlaymapArray[j].cacheStats.tiles=0;
											mapArrays.overlaymapArray[j].cacheStats.size=0;
										}
										
										var tiles=0;
										var size=0;
										for (var i=0; i<result.total_rows; i++) {
											mapArrays.cacheSize+=(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
											result.rows[i].nameFound=false;											
											if (mapArrays.basemapArray) {
												for (var j=0; j<mapArrays.basemapArray.length; j++) {
													if (mapArrays.basemapArray[j].name == result.rows[i].doc.name) {
														result.rows[i].nameFound=true;
														mapArrays.basemapArray[j].cacheStats.tiles++;
														tiles++;
														size+=(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
														mapArrays.basemapArray[j].cacheStats.size+=
															(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
														break; // Out of for loop
													}
												}
											}																			
											if (mapArrays.overlaymapArray) {
												for (var j=0; j<mapArrays.overlaymapArray.length; j++) {
													if (mapArrays.overlaymapArray[j].name == result.rows[i].doc.name) {
														result.rows[i].nameFound=true;
														mapArrays.overlaymapArray[j].cacheStats.tiles++;
														tiles++;
														size+=(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
														mapArrays.overlaymapArray[j].cacheStats.size+=
															(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
														break; // Out of for loop
													}
												}
											}								
										}
										
										$( "#progressbar" ).progressbar({
											value: result.total_rows
										});
										
										var cacheRows="";
										var tableHtml="";					
										if (mapArrays.basemapArray) {
											consoleLog("basemapArray size: " + mapArrays.basemapArray.length);
											for (var i=0; i<mapArrays.basemapArray.length; i++) {
												if (mapArrays.basemapArray[i].tileLayer && 
												    mapArrays.basemapArray[i].cacheStats &&
													(mapArrays.basemapArray[i].cacheStats.tiles > 0 ||
													 mapArrays.basemapArray[i].cacheStats.errors > 0)) {
													cacheRows+="\n[" + i + "] " + mapArrays.basemapArray[i].tileLayer.name + 
														": hits: " + mapArrays.basemapArray[i].cacheStats.hits +
														"; misses: " + mapArrays.basemapArray[i].cacheStats.misses +
														"; errors: " + mapArrays.basemapArray[i].cacheStats.errors +
														"; tiles: " + mapArrays.basemapArray[i].cacheStats.tiles;
													if (baseLayer && baseLayer.name && 
													    baseLayer.name == mapArrays.basemapArray[i].tileLayer.name) {
														tableHtml+='  <tr>\n' +
															'    <td>' + mapArrays.basemapArray[i].tileLayer.name + ' [Current basemap]</td>\n' +
															'    <td>' + mapArrays.basemapArray[i].cacheStats.hits + '</td>\n' +
															'    <td>' + mapArrays.basemapArray[i].cacheStats.misses + '</td>\n' +
															'    <td>' + mapArrays.basemapArray[i].cacheStats.errors +  '</td>\n' +
															'    <td>' + mapArrays.basemapArray[i].cacheStats.tiles + '</td>\n' +
															'    <td>' + (fileSize(mapArrays.basemapArray[i].cacheStats.size)||'N/A') + '</td>\n' +
															'  </tr>';	
													}
													else {
														tableHtml+='  <tr>\n' +
															'    <td>' + mapArrays.basemapArray[i].tileLayer.name + '</td>\n' +
															'    <td>' + mapArrays.basemapArray[i].cacheStats.hits + '</td>\n' +
															'    <td>' + mapArrays.basemapArray[i].cacheStats.misses + '</td>\n' +
															'    <td>' + mapArrays.basemapArray[i].cacheStats.errors +  '</td>\n' +
															'    <td>' + mapArrays.basemapArray[i].cacheStats.tiles + '</td>\n' +
															'    <td>' + (fileSize(mapArrays.basemapArray[i].cacheStats.size)||'N/A') + '</td>\n' +
															'  </tr>';	
													}
												}
											} 
										}
										else {
											consoleError("getCacheSize() no basemapArray");
										}
													
										if (mapArrays.overlaymapArray) {
											consoleLog("overlaymapArray size: " + mapArrays.overlaymapArray.length);
											for (var i=0; i<mapArrays.overlaymapArray.length; i++) {
												if (mapArrays.overlaymapArray[i].tileLayer && 
												    mapArrays.overlaymapArray[i].cacheStats  &&
													(mapArrays.overlaymapArray[i].cacheStats.tiles > 0 ||
													 mapArrays.overlaymapArray[i].cacheStats.errors > 0)) {
													cacheRows+="\n[" + i + "] " + mapArrays.overlaymapArray[i].tileLayer.name + 
														": hits: " + mapArrays.overlaymapArray[i].cacheStats.hits +
														"; misses: " + mapArrays.overlaymapArray[i].cacheStats.misses +
														"; errors: " + mapArrays.overlaymapArray[i].cacheStats.errors;
													tableHtml+='  <tr>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].tileLayer.name + '</td>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].cacheStats.hits + '</td>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].cacheStats.misses + '</td>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].cacheStats.errors +  '</td>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].cacheStats.tiles + '</td>\n' +
														'    <td>' + (fileSize(mapArrays.overlaymapArray[i].cacheStats.size)||'N/A') + '</td>\n' +
														'  </tr>';	
												}
											} 
										}
										else {
											consoleError("getCacheSize() no overlaymapArray");
										}

//
// Process cache stats not used by basename (i.e topoJSON geolevels)
//
										var nonBasemapCacheStats = {};
										for (var i=0; i<result.total_rows; i++) {
											if (result.rows[i].nameFound == false) { // Not yet processed
												var name=(result.rows[i].doc.name||"No name");
												tiles++;
												size+=(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
												if (nonBasemapCacheStats[name]) {
													nonBasemapCacheStats[name].tiles++;
													nonBasemapCacheStats[name].size+=
														(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
												}
												else {
													nonBasemapCacheStats[name] = {
														found: false,
														tiles: 1,
														size: (result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length)										
													}
												}
											}
										}
										
										consoleLog("nonBasemapCacheStats() size: " + Object.keys(nonBasemapCacheStats).length +
											"; " + size + " bytes; tiles: " + tiles);
										consoleLog("getCacheSize(): " + mapArrays.totalTiles + " tiles; size: " + mapArrays.cacheSize + " bytes");
										if (getCacheSizeCallback) {
											$( "#progressbar" ).progressbar({
												value: result.total_rows
											});	
											mapArrays.viewCleanup(function() {
												getCacheSizeCallback(undefined /* No error */, 
													{
														tableHtml: 		tableHtml,
														nonBasemapCacheStats: nonBasemapCacheStats,
														totalTiles: 	mapArrays.totalTiles,
														cacheSize:		mapArrays.cacheSize,
														autoCompaction:	baseLayer.options.auto_compaction
													} // Results
												);
											}); 
										}
									}
								}
							);
						}
					}
				);
			}
		} // End of getCacheSize
		
}; // End of mapArrays() object	
		
// Eof