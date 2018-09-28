/*
 * Modified to support TopoJSON from: 
 *
 * Leaflet.GeoJSONGridLayer: https://github.com/ebrelsford/leaflet-geojson-gridlayer
 * by Eric Brelsford 
 *
 * Usage:
 
 		topojsonTileLayer = new L.topoJsonGridLayer(topojsonURL, 
		// E.g. http://127.0.0.1:3000/getMapTile/?zoomlevel=1&x=0&y=0&databaseType=PostGres&table_catalog=sahsuland_dev&table_schema=peter&table_name=geography_sahsuland&geography=SAHSULAND&geolevel_id=2&tiletable=tiles_sahsuland&output=topojson
			{
				attribution: 'Tiles &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility">Imperial College London</a>',
                layers: {
					default: { // If not using a feature collection (which we are not)
						style: style,
						onEachFeature: createPopup // i.e. Call createPopup(feature, layer) function,
						... more L.GeoJSON options
					} 
                }
			}
		);
		
 */

(function () {

    var console = window.console || {
        error: function () {},
        warn: function () {}
    };

    function defineLeafletTopoJSONGridLayer(L) {
        L.TopoJSONGridLayer = L.GridLayer.extend({
            initialize: function (url, options) {
                L.GridLayer.prototype.initialize.call(this, options);
	
                this._url = url;
                this._geojsons = {};
                this._features = {};
				if (options.consoleError) {
					this.options.consoleError = options.consoleError;
				}
				if (options.consoleDebug) {
					this.options.consoleDebug = options.consoleDebug;
				}
				if (options.areaIdObj) {
					this.options.areaIdObj = options.areaIdObj;
					this.options.areaIdObjKeys = Object.keys(this.options.areaIdObj).length;
					this.consoleDebug("[TopoJSONGridLayer.js] areaIdObj filter enabled, keys: " + this.options.areaIdObjKeys);
				}
				else {
					this.consoleDebug("[TopoJSONGridLayer.js] areaIdObj filter disabled.");
					this.options.areaIdObjKeys = 0;
				}
				
                this.geoJsonClass = (this.options.geoJsonClass ? this.options.geoJsonClass : L.GeoJSON);
				if (!options.useCache) { // Add pouchDB
					this._db     = null;
					return;
				}
				this.PouchDBError = {};
				this._db = undefined;
				
				var myNav = navigator.userAgent.toLowerCase();
				if ((myNav.indexOf('msie') != -1) && parseInt(myNav.split('msie')[1])) {
					this.options.useCache=false;
					this.consoleDebug("[TopoJSONGridLayer.js] IE detectect, unable to create PouchDB, useCache disabled");
				}
				else {
					this._db = new PouchDB('offline-tiles', {auto_compaction: (options.auto_compaction || false) });
					if (this._db == undefined) {
						this.options.useCache=false;
						this.consoleError("[TopoJSONGridLayer.js] Unable to create PouchDB, useCache disabled");
					}
					else {
						this.consoleDebug("[TopoJSONGridLayer.js] Created tile cache (PouchDB), auto_compaction: " + options.auto_compaction);
					}
				}
            },

			consoleError: function (msg) {
				if (this.options.consoleError) {
					this.options.consoleError(msg);
				}	
			},
		
			consoleDebug: function (msg) {
				if (this.options.consoleDebug) {
					this.options.consoleDebug(msg);
				}	
			},
			
            onAdd: function (map) {
                var layers = this._geojsons;
                Object.keys(layers).forEach(function (key) {
                    map.addLayer(layers[key]);
                });

                L.GridLayer.prototype.onAdd.call(this, map);
                this.zoomanimHandler = this._handleZoom.bind(this);
                map.on('zoomanim', this.zoomanimHandler);
            },

            onRemove: function (map) {
                var layers = this._geojsons;
                Object.keys(layers).forEach(function (key) {
                    map.removeLayer(layers[key]);
                });

                L.GridLayer.prototype.onRemove.call(this, map);
                map.off('zoomanim', this.zoomanimHandler);
            },

            _handleZoom: function (e) {
                this.checkZoomConditions(e.zoom);
            },

			getTile: function(tile, tileUrl, tileLayer, coords, done, existingRevision) {
				tileLayer._db.get(tileUrl, {revs_info: true, conflicts: true}).then(function (doc) {					
					if (doc && doc.dataUrl) {
						
						tileLayer.fire('tilecachehit', {
							tile: tile,
							url: tileUrl
						});
						

						if (Date.now() > doc.timestamp + tileLayer.options.cacheMaxAge) {
						// Tile is too old, try to refresh it
							tile.existingRevision = doc._revs_info[0].rev;
							tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.get() Tile is too old, refreshing: " + tileUrl + 
								"; rev: " + tile.existingRevision);
							tileLayer.fetchTile(coords || tile.coords, tile.existingRevision, function (error) {
								done(error, tile, existingRevision);
							});
						}
						else {
//								tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.get() " + doc.name + 
//									"; _id: " + doc._id + 
//									"; _rev: " + doc._rev + 
//									"; length: " + doc.urlLength);
						
							try {
								tileLayer.addData(doc.dataUrl);
								done(null, tile, existingRevision);
							}
							catch (err) {
								tileLayer.fire('tilecacheerror', { tile: tile, error: err });
								done(err, tile, existingRevision);
							}
						}
					}	
					else { // Cache error
						var err=new Error("_db.get() invalid doc: "+ JSON.stringify(doc, null, 2));
						tileLayer.fire('tilecacheerror', { tile: tile, error: err });
						done(err, tile, existingRevision);
					}	
				}).catch(function (err) {
					if (err && err.status == 404) { // Cache miss
						
						tileLayer.fire('tilecachemiss', {
							tile: tile,
							url: tileUrl
						});
						
						tileLayer.fetchTile(coords || tile.coords, undefined /* No pre existing revision */, 
							function (error) {
								done(error, tile, existingRevision);
							});
					}
					else if (err && err.status == 500 && err.name == "indexed_db_went_bad" && err.error) { 
					/*
14:53:54.352 +6.1 ERROR: _db.put() error: {
"status": 500,
"name": "indexed_db_went_bad",
"message": "unknown",
"error": true,
"reason": "Failed to open indexedDB, are you in private browsing mode?"
} 1 tile-common.js:206
					 */
						tileLayer.options.useCache=false;	// Disable cache
						tileLayer.PouchDBError = err;		// Flag error
						tileLayer.consoleError("[TopoJSONGridLayer.js] TopoJSONGridLayer PouchDB error: " + err.reason + ", useCache disabled");	
						tileLayer.fire('tilecacheerror', { tile: tile, error: err });
						tileLayer.fetchTile(coords, undefined /* No pre existing revision */, function (error) {
							done(error, tile, existingRevision);
						});
					}
					else {
						tileLayer.consoleError("[TopoJSONGridLayer.js] _db.get() error: " + JSON.stringify(err, null, 2) +
							"; tileUrl: " + tileUrl);
						tileLayer.fire('tilecacheerror', { tile: tile, error: err });
						tileLayer.fetchTile(coords, undefined /* No pre existing revision */, function (error) {
							done(error, tile, existingRevision);
						});
					}
				}); // End of this._db.get() promise
			},
			
            createTile: function (coords, done) {
                var tile = L.DomUtil.create('div', 'leaflet-tile');
                var size = this.getTileSize();
                tile.width = size.x;
                tile.height = size.y;
				
                var tileUrl = L.Util.template(this._url, coords);
				tile.tileUrl = tileUrl;	// Added for cacheevents
				tile.coords = coords;
				
                var tileLayer = this;
				if (this.options.useCache) {
					this.getTile(tile, tileUrl, tileLayer, coords, done);
				} 
				else {
					this.fetchTile(coords, undefined /* No pre existing revision */, function (error) {
						done(error, tile);
					});
				}
                return tile;
            },

            fetchTile: function (coords, existingRevision, done) {
                var tileUrl = L.Util.template(this._url, coords);
                var tileLayer = this;
				var tile = { // Dummy tile object for cacheevents
					conflicts: 0,
					tileUrl: tileUrl,
					coords: coords,
					existingRevision: existingRevision
				};
				
				conflictResolver = function(error, tile, existingRevision) { // Upsert
					if (error) {
						tileLayer.consoleDebug("[TopoJSONGridLayer.js] conflictResolver: @" + tile.conflicts + "; error: " + 
							JSON.stringify(error, null, 2) +
							"; for tile: " + tileUrl);
						done(error, tile);
					}
					else {
						tileLayer.consoleDebug("[TopoJSONGridLayer.js] conflictResolver: @" + tile.conflicts +
							"; for tile: " + tileUrl); 
						tileLayer._db.remove(tileUrl).then(function (response) { // Remove conflict; force reload
								if (response.ok) {	
									tileLayer._db.put(doc).then(function (response) {
											if (response.ok) {
												tileLayer.consoleDebug("[TopoJSONGridLayer.js] on conflict: " + tile.conflicts + 
													" _db.remove() then _db.put(): " + 
													response.id + 
													"; 	revisions: " + existingRevision + ", " + tile.existingRevision);	
												tile.conflicts--;
												done(null, tile);								
											}
											else {
												var err=new Error("[TopoJSONGridLayer.js] on conflict _db.put() invalid response: " + 
													JSON.stringify(response, null, 2));
												tileLayer.fire('tilecacheerror', { tile: tile, error: err });
												done(err);
											}
										}).catch(putErrorFunction);
								}
								else {
									var err=new Error("[TopoJSONGridLayer.js] _db.put() invalid response: " + 
										JSON.stringify(response, null, 2));
									tileLayer.fire('tilecacheerror', { tile: tile, error: err });
									done(err);
								}
							});
								

					}
				}
							
				putErrorFunction = function(err) {
/*
+32.1: [ERROR] [TopoJSONGridLayer.js] _db.put() error: {
"description": "Adapter is missing",
"code": 404,
"stack": "Error: Adapter is missing\n   at Anonymous function (http://localhost:8080/RIF4/libs/pouchdb.js:3133:11)\n   at Anonymous function (http://localhost:8080/RIF4/libs/pouchdb.js:3111:6)\n   at tryToUnwrap (http://localhost:8080/RIF4/libs/pouchdb.js:12003:5)\n   at tryCatch (http://localhost:8080/RIF4/libs/pouchdb.js:12015:5)\n   at safelyResolveThenable (http://localhost:8080/RIF4/libs/pouchdb.js:12006:3)\n   at Promise (http://localhost:8080/RIF4/libs/pouchdb.js:11880:5)\n   at PouchDB (http://localhost:8080/RIF4/libs/pouchdb.js:3099:3)\n   at initialize (http://localhost:8080/RIF4/libs/TopoJSONGridLayer.js:53:5)\n   at e (http://localhost:8080/RIF4/libs/standalone/leaflet.js:5:2729)\n   at L.topoJsonGridLayer (http://localhost:8080/RIF4/libs/TopoJSONGridLayer.js:347:13)"
}
Stack: undefined
*/									
					if (err && err.status == 404 && err.description == "Adapter is missing") { 
						tileLayer.options.useCache=false;	// Disable cache
						tileLayer.PouchDBError = err;		// Flag error
						tileLayer.consoleError("[TopoJSONGridLayer.js] TopoJSONGridLayer PouchDB error: " + err.stack + ", useCache disabled");	
						tileLayer.fire('tilecacheerror', { tile: tile, error: err });
						tileLayer.fetchTile(coords, undefined /* No pre existing revision */, function (error) {
							done(error, tile);
						});
					}
/*
+2657.3: [ERROR] [TopoJSONGridLayer.js] _db.put() error: {
"status": 409,
"name": "conflict",
"message": "Document update conflict",
"error": true
} 
*/
					else if (err && err.status == 409 && err.message == "Document update conflict") { 
						tile.conflicts++;
						var randomNumberBetween0and19 = Math.floor(Math.random() * 20);
						if (tile.conflicts < 3) { // Limit recursion
							tileLayer.fire('tilecacheerror', { tile: tile, error: err });
/*
 * Conflict resolution: upsert: re-get tile, remove, put
 */
							var resolver=function() {
								tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.put() update conflict: @" + tile.conflicts + 
									"; " + JSON.stringify(err, null, 2) +
									"; random offset: " + randomNumberBetween0and19 + 
									"; for tile: " + tileUrl);
								tileLayer.getTile(tile, tileUrl, tileLayer, coords, conflictResolver, tile.existingRevision);
							}
							setTimeout(resolver, randomNumberBetween0and19);
						}
						else {
							setTimeout(done, randomNumberBetween0and19, null); // Ignore conflict, conflictResolver will fix
						}
					}
					else if (err == undefined || err.status == undefined) {
						tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.put() no error");	
						done(null, tile);			
					}
					else {
						tileLayer.consoleError("[TopoJSONGridLayer.js] _db.put() error: " + JSON.stringify(err, null, 2) +
							"; for tile: " + tileUrl);
						tileLayer.fire('tilecacheerror', { tile: tile, error: err });
						tileLayer.options.useCache=false;	// Disable cache
						tileLayer.PouchDBError = err;		// Flag error
						done("[TopoJSONGridLayer.js] _db.put() error: " + JSON.stringify(err, null, 2));	
					}
				};
						
                var request = new XMLHttpRequest();
                request.open('GET', tileUrl, true);

                request.onload = function () {
                    if (request.status >= 200 && request.status < 400) {
                        var data = JSON.parse(request.responseText);
                        tileLayer.addData(data);
						
						if (tileLayer.options.useCache) {						
							var doc = {
									_id: tileUrl,
									dataUrl: data,
									timestamp: Date.now(),
									urlLength: request.responseText.length,
									name: (tileLayer.options && tileLayer.options.name || "TopoJSONGridLayer")
								};
								
							if (tile.existingRevision) {
								tileLayer._db.remove(tileUrl, tile.existingRevision).then(function (response) {
									if (response.ok) {
	//									tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.remove(): " + response.id);
										tileLayer._db.put(doc).then(function (response) {
												if (response.ok) {
				//									tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.put(): " + response.id);
													done(null, tile);								
												}
												else {
													var err=new Error("[TopoJSONGridLayer.js] _db.put() invalid response: " + 
														JSON.stringify(response, null, 2));
													tileLayer.fire('tilecacheerror', { tile: tile, error: err });
													done(err);
												}
											}).catch(putErrorFunction);						
									}
									else {
										var err=new Error("[TopoJSONGridLayer.js] _db.put() invalid response: " + 
											JSON.stringify(response, null, 2));
										tileLayer.fire('tilecacheerror', { tile: tile, error: err });
										done(err);
									}
								}).catch(putErrorFunction);
							}
							else {
								tileLayer._db.put(doc).then(function (response) {
										if (response.ok) {
		//									tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.put(): " + response.id);
											done(null, tile);								
										}
										else {
											var err=new Error("[TopoJSONGridLayer.js] _db.put() invalid response: " + 
												JSON.stringify(response, null, 2));
											tileLayer.fire('tilecacheerror', { tile: tile, error: err });
											done(err);
										}
									}).catch(putErrorFunction);
							}
						}
						else {
							done(null, tile);								
						}
                    } 
					else {
                        // We reached our target server, but it returned an error
                        done(request.statusText);
                    }
                };

                request.onerror = function () {
                    done(request.statusText);
                };

                request.send();
            },

            getLayers: function () {
                var geojsons = this._geojsons,
                    layers = [];
                Object.keys(geojsons).forEach(function (key) {
                    layers.push(geojsons[key]);
                });
                return layers;
            },

            hasLayerWithId: function (sublayer, id) {
                if (!this._geojsons[sublayer] || !this._features[sublayer]) return false;
                return this._features[sublayer].hasOwnProperty(id);
            },

            addData: function (data) {
				
				if (data.type === "Topology") { // Add topojson support
					for (key in data.objects) {
						if (data.objects.hasOwnProperty(key)) {
							geojson = topojson.feature(data, data.objects[key]);
						}
					}
					data=geojson;
				}
								
                if (data.type === 'FeatureCollection') {
                    this.addSubLayerData('default', data);
                }
                else {
                    var tileLayer = this;
                    Object.keys(data).forEach(function (key) {
						tileLayer.addSubLayerData(key, data[key]);
                    });
                }
            },
            addSubLayerData: function (sublayer, data) {
                var tileLayer = this;
				var subLayerAdds=0;
				var subLayerUpdates=0;
				
				if (data == undefined || data.features == undefined || sublayer == undefined) { 
					// Attempt to get an invalid tile
					return;
				}
				
                if (!this._geojsons[sublayer]) {
                    this._geojsons[sublayer] = new this.geoJsonClass(null, this.options.layers[sublayer]).addTo(this._map);	
                    this.checkZoomConditions(this._map.getZoom());
                }
				
                var toAdd = data.features.filter(function (feature) { 	// Return a filtered array: features needing to be added
																		// and in areaIdObj{} object
                    if (this.hasLayerWithId(sublayer, feature.id ? feature.id : feature.properties.id)) { // Already added
						return false;
					}
					else if (this.options.areaIdObjKeys == 0) {
						 return true;
					}
					else if (feature.properties && feature.properties.area_id && this.options.areaIdObj[feature.properties.area_id]) {
						 return true;
					}
					
					return false;
                }, this /* thisValue: i.e. TopoJSONGridLayer object */);
//				if (data.features.length > 0 ) {
//					this.consoleDebug("[TopoJSONGridLayer.js] layer: " + sublayer + "; data.features: " + data.features.length + 
//						"; toAdd: " + toAdd.length);
//				}				

                if (!this._features[sublayer]) {
                    this._features[sublayer] = {};
                }
                toAdd.forEach(function (feature) {
                    var id = feature.id ? feature.id : feature.properties.id;
					var areaId = feature.properties ? feature.properties.area_id : undefined;

					if (this._features[sublayer][id]) {
						subLayerUpdates++;
					}
					else {
						subLayerAdds++;
					}
					this._features[sublayer][id] = feature;
                }, this);

                var geoJSONlayer=this._geojsons[sublayer].addData({ // Add geoJSON data. This creates multiple layers
                    type: 'FeatureCollection',
                    features: toAdd
				});
				
				
				tileLayer.fire('addsublayer', { sublayer: sublayer, subLayerAdds: subLayerAdds, subLayerUpdates: subLayerUpdates });
            },

            checkZoomConditions: function (zoom) {
                var layers = this._geojsons,
                    map = this._map;
                Object.keys(layers).forEach(function (key) {
                    var layer = layers[key],
                        options = layer.options;
                    if ((options.maxZoom && zoom > options.maxZoom) ||
                        (options.minZoom && zoom < options.minZoom)) {
                        map.removeLayer(layer);
                    }
                    else {
                        map.addLayer(layer);
                    }
                });
            }
        });

		// namespace TopoJSONGridLayer
		// ection PouchDB tile caching options
		// option useCache: Boolean = false
		// Whether to use a PouchDB cache on this tile layer, or not
		L.TopoJSONGridLayer.prototype.options.useCache     = false;

		// option cacheMaxAge: Number = 24*3600*1000
		// Maximum age of the cache, in milliseconds
		L.TopoJSONGridLayer.prototype.options.cacheMaxAge  = 24*3600*1000;

        L.topoJsonGridLayer = function(url, options) {
            return new L.TopoJSONGridLayer(url, options);
        };
		
		// option auto_compaction: true/false
		// This turns on auto compaction, which means compact() is called after every change to the database. Defaults to false.
		L.TileLayer.prototype.options.auto_compaction = false;	
    }

    if (typeof define === 'function' && define.amd) {
        // Try to add leaflet.loading to Leaflet using AMD
        define(['leaflet'], function (L) {
            defineLeafletTopoJSONGridLayer(L);
        });
    }
    else {
        // Else use the global L
        defineLeafletTopoJSONGridLayer(L);
    }

})();

