L.TileLayer.TileTopojson = L.TileLayer.extend({
   options: {
      async: true,
      tileSize: 256
   },
   onAdd: function(map) {
      var self = this;
      L.TileLayer.prototype.onAdd.call(this, map);
      this._path = d3.geo.path().projection(function(d) {
         var point = map.latLngToLayerPoint(new L.LatLng(d[1], d[0]));
         return [point.x, point.y];
      });
      map.on({
         'viewreset': self.options.resetIds
      });
      this.on("tileunload", function(d) {
         if (d.tile.xhr) d.tile.xhr.abort();
         if (d.tile.nodes) d.tile.nodes.remove();
         d.tile.nodes = null;
         d.tile.xhr = null;
      });
   },
   _loadTile: function(tile, tilePoint) {
      this._adjustTilePoint(tilePoint);
      if (!tile.nodes && !tile.xhr) {
         var params = this.options.getTilesParams(tilePoint, this._rif_map_zoom()),
            callback = this._d3Callback.call(this, tile, tilePoint);
         //this.start = new Date().getTime() / 1000;
         RIF.getTiles(callback, params);
      }
   },
   _tileOnLoad: function() {
      var layer = this._layer;
      layer._tileLoaded();
   },
   _tileLoaded: function() {
      this._tilesToLoad--;
      if (!this._tilesToLoad) {
         this.options.loaded();
      }
      //this.end = new Date().getTime() / 1000;
      //console.log(this.end - this.start);
      if (this._animated) {
         clearTimeout(this._clearBgBufferTimer);
         this._clearBgBufferTimer = setTimeout(L.bind(this._clearBgBuffer, this), 500);
      }
   },
   _d3Callback: function(tile, tilePoint) {

      var self = tile._layer = this,
         opt = self.options;

      return function(r) {
         var r = this;
         var drag = d3.behavior.drag();

         drag.on('drag', function(d, i) {
            self.drag = 1;
         });

         tile.xhr = null;

         if (typeof r.objects === "undefined") {
            self._tileOnLoad.call(tile);
            return;
         }

         for (var i in r.objects) {
            var tileId = i;
            break;
         };

         var o = r.objects[tileId];

         o.geometries = o.geometries.filter(function(d) {
            return opt.deduplicate.call(self, d)
         });

         var geoJS = topojson.feature(r, o).features;
         if (geoJS === 'undefined') {
            return;
         };

         tile.nodes = d3.select("#" + opt.mapId).select("svg").append("g");
         tile.nodes
            .selectAll("path")
            .data(geoJS)
            .enter()
            .append("path")
            .attr("d", self._path)
            .attr("style", opt.getStyle)
            .attr("class", opt.class)
            .attr("id", opt.id)
            .on("mouseout", function() {
               opt.evntHndl.call(this, "mouseout");
            }).on("mousedown", function() {
               self.drag = -1;
            }).on("mouseup", function() {})
            .on("mouseover", function(d) {
               opt.evntHndl.call(this, "mouseover", d);
            }).on("click", function(d, i) {
               if (self.drag === 1) {
                  return;
               }
               opt.evntHndl.call(this, "click", d.properties);
            }).call(drag);

         self._tileOnLoad.call(tile);
      }
   },
   _rif_map_zoom: function() {
      return this._map.getZoom();
   }
});