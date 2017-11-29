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
					this._db.get(tileUrl, {revs_info: true}).then(function (doc) {					
						if (doc && doc.dataUrl) {
							
							tileLayer.fire('tilecachehit', {
								tile: tile,
								url: tileUrl
							});
							

							if (Date.now() > doc.timestamp + tileLayer.options.cacheMaxAge) {
							// Tile is too old, try to refresh it
								var existingRevision = doc._revs_info[0].rev;
								tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.get() Tile is too old: " + tileUrl + 
									"; rev: " + existingRevision);
								tileLayer.fetchTile(coords || tile.coords, existingRevision, function (error) {
									done(error, tile);
								});
							}
							else {
//								tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.get() " + doc.name + 
//									"; _id: " + doc._id + 
//									"; _rev: " + doc._rev + 
//									"; length: " + doc.urlLength);
							
								try {
									tileLayer.addData(doc.dataUrl);
									done(null, tile);
								}
								catch (err) {
									tileLayer.fire('tilecacheerror', { tile: tile, error: err });
									done(err, tile);
								}
							}
						}	
						else { // Cache error
							var err=new Error("_db.get() invalid doc: "+ JSON.stringify(doc, null, 2));
							tileLayer.fire('tilecacheerror', { tile: tile, error: err });
							done(err, tile);
						}	
					}).catch(function (err) {
						if (err && err.status == 404) { // Cache miss
							
							tileLayer.fire('tilecachemiss', {
								tile: tile,
								url: tileUrl
							});
							
							tileLayer.fetchTile(coords || tile.coords, undefined /* No pre existing revision */, 
								function (error) {
									done(error, tile);
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
								done(error, tile);
							});
						}
						else {
							tileLayer.consoleError("[TopoJSONGridLayer.js] _db.get() error: " + JSON.stringify(err, null, 2));
							tileLayer.fire('tilecacheerror', { tile: tile, error: err });
							tileLayer.fetchTile(coords, undefined /* No pre existing revision */, function (error) {
								done(error, tile);
							});
						}
					});
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
					tileUrl: tileUrl,
					coords: coords
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
								
							if (existingRevision) {
								tileLayer._db.remove(tileUrl, existingRevision);
							}
							tileLayer._db.put(doc).then(function (response) {
									if (response.ok) {
	//									tileLayer.consoleDebug("[TopoJSONGridLayer.js] _db.put(): " + response.id);
										done(null);								
									}
									else {
										var err=new Error("[TopoJSONGridLayer.js] _db.put() invalid response: " + 
											JSON.stringify(response, null, 2));
										tileLayer.fire('tilecacheerror', { tile: tile, error: err });
										done(err);
									}
								}).catch(function (err) {
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
									else {
										tileLayer.consoleError("[TopoJSONGridLayer.js] _db.put() error: " + JSON.stringify(err, null, 2));
										tileLayer.fire('tilecacheerror', { tile: tile, error: err });
										tileLayer.options.useCache=false;	// Disable cache
										tileLayer.PouchDBError = err;		// Flag error
										tileLayer.fetchTile(coords, undefined /* No pre existing revision */, function (error) {
											done(error, tile);
										});
									}
								});
						}
						else {
							done(null);								
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
				tileLayer.fire('addsublayer', { sublayer: sublayer });
                if (!this._geojsons[sublayer]) {
                    this._geojsons[sublayer] = new this.geoJsonClass(null, this.options.layers[sublayer]).addTo(this._map);	
                    this.checkZoomConditions(this._map.getZoom());
                }
                var toAdd = data.features.filter(function (feature) {
                    return !this.hasLayerWithId(sublayer, feature.id ? feature.id : feature.properties.id);
                }, this);

                if (!this._features[sublayer]) {
                    this._features[sublayer] = {};
                }
                toAdd.forEach(function (feature) {
                    var id = feature.id ? feature.id : feature.properties.id;
                    this._features[sublayer][id] = feature;
                }, this);

                var geoJSONlayer=this._geojsons[sublayer].addData({ // Add geoJSON data. This creates multiple layers
                    type: 'FeatureCollection',
                    features: toAdd
				});
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

