L.TileLayer.d3_geoJSON = L.TileLayer.extend({

    options: {
        async: true,
        tileSize: 256
    },

    onAdd: function (map) {
        var self = this;
        L.TileLayer.prototype.onAdd.call(this, map);
        this._path = d3.geo.path().projection(function (d) {
            var point = map.latLngToLayerPoint(new L.LatLng(d[1], d[0]));
            return [point.x, point.y];
        });

        map.on({
            'viewreset': self.options.resetIds
        });

        this.on("tileunload", function (d) {
            if (d.tile.xhr) d.tile.xhr.abort();
            if (d.tile.nodes) d.tile.nodes.remove();
            d.tile.nodes = null;
            d.tile.xhr = null;
        });
    },

    _loadTile: function (tile, tilePoint) {
        this._adjustTilePoint(tilePoint);
        if (!tile.nodes && !tile.xhr) {
            var url = this.options.getUrlFromTile(tilePoint, this._rif_map_zoom() ),
                c = this._d3Callback.call(this, tile, tilePoint);
            //this.start = new Date().getTime() / 1000;
            tile.xhr = d3.xhr(url, c);
        }
    },

    _tileOnLoad: function () {
        var layer = this._layer;
        layer._tileLoaded();
    },

    _tileLoaded: function () {
        this._tilesToLoad--;
        if (this._tilesToLoad === 1) {
            this.options.loaded();
            //this.end = new Date().getTime() / 1000;
			//console.log(this.end - this.start);
            if (this._animated) {
                clearTimeout(this._clearBgBufferTimer);
                this._clearBgBufferTimer = setTimeout(L.bind(this._clearBgBuffer, this), 500);
            }
        }
    },

    _d3Callback: function (tile, tilePoint) {
        var self = tile._layer = this,
            opt = self.options,
            obj = tilePoint.x + "_" + tilePoint.y + "_" + tilePoint.z,
		    drag = d3.behavior.drag();
        
		return function (r) {   

		   drag.on('drag', function(d,i) { self.drag = 1;});	           
		   tile.xhr = null;

		   if (r.responseText === "-1") {
			    self._tileOnLoad.call(tile);
                return;
            }
			var mytopo = jQuery.parseJSON(r.responseText.split("________")[1]),
			    o =  mytopo.objects[obj];
				o.geometries = o.geometries.filter(function(d) { return opt.deduplicate.call(self, d)});

			var geoJS = topojson.feature( mytopo, o ).features;

			if (geoJS === 'undefined') {
                return;
            }

            tile.nodes = d3.select("#map").select("svg").append("g");
            tile.nodes.selectAll("path")
                .data(geoJS)
                .enter()
                .append("path")
                .attr("d", self._path)
                .attr("style", opt.style)
                .attr("class", opt.class)
                .attr("id", opt.id)
                .on("mouseout", function () {
                    opt.evntHndl.call( this, "mouseout");
                })
				.on("mousedown", function () {
                    self.drag = -1;
                })
                .on("mouseover", function () {
                    opt.evntHndl.call( this, "mouseover");	
                })
				.on("click", function () {
                    if (self.drag === 1){
                        return;						
					}	
					opt.evntHndl.call( this, "click");				
                })
				.call(drag);

            self._tileOnLoad.call(tile);
        }
    },

    _rif_map_zoom: function () {
        return this._map.getZoom();
    }

});