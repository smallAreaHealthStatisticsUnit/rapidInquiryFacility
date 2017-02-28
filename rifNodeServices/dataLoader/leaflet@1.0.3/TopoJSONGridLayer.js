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
                this.geoJsonClass = (this.options.geoJsonClass ? this.options.geoJsonClass : L.GeoJSON);
				if (!options.useCache) { // Add pouchDB
					this._db     = null;
					return;
				}
				this._db = new PouchDB('offline-tiles', {auto_compaction: (options.auto_compaction || false) });
				consoleLog("Created tile cache (PouchDB), auto_compaction: " + options.auto_compaction);
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
                var tileLayer = this;
				tile.coords = coords;
				if (this.options.useCache) {
					this._db.get(tileUrl, {revs_info: true}).then(function (doc) {					
						if (doc && doc.dataUrl) {
							// Handle expired
							consoleLog("_db.get() " + doc.name + 
								"; _id: " + doc._id + 
								"; _rev: " + doc._rev + 
								"; length: " + doc.urlLength);
							tileLayer.addData(doc.dataUrl);
							done(null, tile);
						}	
						else {
							tileLayer.fetchTile(coords || tile.coords, function (error) {
								done(error, tile);
							});
						}	
					}).catch(function (err) {
						if (err && err.status == 404) {
							tileLayer.fetchTile(coords || tile.coords, function (error) {
								done(error, tile);
							});
						}
						else {
							consoleError("_db.get() error: " + JSON.stringify(err, null, 2));
							done(err, tile);
						}
					});
				} 
				else {
					this.fetchTile(coords, function (error) {
						done(error, tile);
					});
				}
                return tile;
            },

            fetchTile: function (coords, done) {
                var tileUrl = L.Util.template(this._url, coords);
                var tileLayer = this;

                var request = new XMLHttpRequest();
                request.open('GET', tileUrl, true);

                request.onload = function () {
                    if (request.status >= 200 && request.status < 400) {
                        var data = JSON.parse(request.responseText);
                        tileLayer.addData(data);
						
						var doc = {
								_id: tileUrl,
								dataUrl: data,
								timestamp: Date.now(),
								urlLength: request.responseText.length,
								name: (tileLayer.options && tileLayer.options.name || "TopoJSONGridLayer")
							};
						tileLayer._db.put(doc).then(function (response) {
								consoleLog("_db.put() " + response.ok + ": " + response.id);
								done(null);
							}).catch(function (err) {
								consoleError("_db.put() error: " + JSON.stringify(err, null, 2));
								done(err);
							});
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

		// ðŸ‚namespace TopoJSONGridLayer
		// ðŸ‚section PouchDB tile caching options
		// ðŸ‚option useCache: Boolean = false
		// Whether to use a PouchDB cache on this tile layer, or not
		L.TopoJSONGridLayer.prototype.options.useCache     = false;

		// ðŸ‚option saveToCache: Boolean = true
		// When caching is enabled, whether to save new tiles to the cache or not
		L.TopoJSONGridLayer.prototype.options.saveToCache  = true;

		// ðŸ‚option useOnlyCache: Boolean = false
		// When caching is enabled, whether to request new tiles from the network or not
		L.TopoJSONGridLayer.prototype.options.useOnlyCache = false;

		// ðŸ‚option useCache: String = 'image/png'
		// The image format to be used when saving the tile images in the cache
		L.TopoJSONGridLayer.prototype.options.cacheFormat = 'image/png';

		// ðŸ‚option cacheMaxAge: Number = 24*3600*1000
		// Maximum age of the cache, in milliseconds
		L.TopoJSONGridLayer.prototype.options.cacheMaxAge  = 24*3600*1000;

        L.topoJsonGridLayer = function(url, options) {
            return new L.TopoJSONGridLayer(url, options);
        };
		
		// ðŸ‚option auto_compaction: true/false
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

