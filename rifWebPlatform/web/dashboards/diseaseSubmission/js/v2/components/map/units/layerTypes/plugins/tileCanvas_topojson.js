L.TileLayer.canvasTopojson = L.TileLayer.Canvas.extend({
   options: {
      debug: false
   },
   tileSize: 256,
   initialize: function(options) {
      L.Util.setOptions(this, options);
      this.drawTile = function(canvas, tilePoint, zoom) {
         var ctx = {
            canvas: canvas,
            tile: tilePoint,
            zoom: zoom
         };
         if (this.options.debug) {
            this._drawDebugInfo(ctx);
         }
         this._draw(ctx, tilePoint, zoom);
      };
      this.on("tileunload", function(d) {
         d.tile.getContext('2d').clearRect(0, 0, this.tileSize, this.tileSize);
         if (d.tile.xhr) d.tile.xhr.abort();
         d.tile.nodes = null;
         d.tile.xhr = null;
      });
   },
   _drawDebugInfo: function(ctx) {
      var max = this.tileSize,
         g = ctx.canvas.getContext('2d');
      g.strokeStyle = '#000000';
      g.fillStyle = '#FFFF00';
      g.strokeRect(0, 0, max, max);
      g.font = "12px Arial";
      g.fillRect(0, 0, 5, 5);
      g.fillRect(0, max - 5, 5, 5);
      g.fillRect(max - 5, 0, 5, 5);
      g.fillRect(max - 5, max - 5, 5, 5);
      g.fillRect(max / 2 - 5, max / 2 - 5, 10, 10);
      g.strokeText(ctx.tile.x + ' ' + ctx.tile.y + ' ' + ctx.zoom, max / 2 - 30, max / 2 - 10);
   },
   _tilePoint: function(ctx, coords) {
      // start coords to tile 'space'
      var s = ctx.tile.multiplyBy(this.tileSize),
         // actual coords to tile 'space'
         p = this._map.project(new L.LatLng(coords[1], coords[0])),
         // point to draw        
         x = Math.round(p.x - s.x),
         y = Math.round(p.y - s.y);
      return {
         x: x,
         y: y
      };
   },
   _clip: function(ctx, points) {
      var nw = ctx.tile.multiplyBy(this.tileSize),
         se = nw.add(new L.Point(this.tileSize, this.tileSize)),
         bounds = new L.Bounds([nw, se]),
         len = points.length,
         out = [];
      for (var i = 0; i < len - 1; i++) {
         var seg = L.LineUtil.clipSegment(points[i], points[i + 1], bounds, i);
         if (!seg) {
            continue;
         }
         out.push(seg[0]);
         // if segment goes out of screen, or it's the last one, it's the end of the line part
         if ((seg[1] !== points[i + 1]) || (i === len - 2)) {
            out.push(seg[1]);
         }
      }
      return out;
   },
   _isActuallyVisible: function(coords) {
      var coord = coords[0],
         min = [coord.x, coord.y],
         max = [coord.x, coord.y];
      for (var i = 1; i < coords.length; i++) {
         coord = coords[i];
         min[0] = Math.min(min[0], coord.x);
         min[1] = Math.min(min[1], coord.y);
         max[0] = Math.max(max[0], coord.x);
         max[1] = Math.max(max[1], coord.y);
      }
      var diff0 = max[0] - min[0],
         diff1 = max[1] - min[1];
      if (this.options.debug) {
         console.log(diff0 + ' ' + diff1);
      }
      var visible = diff0 > 1 || diff1 > 1;
      return visible;
   },
   _drawPoint: function(ctx, geom, style) {
      if (!style) {
         return;
      }
      var p = this._tilePoint(ctx, geom),
         c = ctx.canvas,
         g = c.getContext('2d');
      g.beginPath();
      g.fillStyle = style.color;
      g.arc(p.x, p.y, style.radius, 0, Math.PI * 2);
      g.closePath();
      g.fill();
      g.restore();
   },
   _drawLineString: function(ctx, geom, style) {
      if (!style) {
         return;
      }
      var coords = geom,
         proj = [],
         i;
      coords = this._clip(ctx, coords);
      coords = L.LineUtil.simplify(coords, 1);
      for (i = 0; i < coords.length; i++) {
         proj.push(this._tilePoint(ctx, coords[i]));
      }
      if (!this._isActuallyVisible(proj)) {
         return;
      }
      var g = ctx.canvas.getContext('2d');
      g.strokeStyle = style.color;
      g.lineWidth = style.size;
      g.beginPath();
      for (i = 0; i < proj.length; i++) {
         var method = (i === 0 ? 'move' : 'line') + 'To';
         g[method](proj[i].x, proj[i].y);
      }
      g.stroke();
      g.restore();
   },
   _drawPolygon: function(ctx, geom, style) {
      if (!style) {
         return;
      }
      var g = ctx.canvas.getContext('2d');
      outline = style.outline;
      for (var el = 0; el < geom.length; el++) {
         var coords = geom[el],
            proj = [],
            i,
            previous = {
               x: null,
               y: null
            };
         for (i = 0; i < coords.length; i++) {
            var current = this._tilePoint(ctx, coords[i]);
            if (previous.x === current.x && previous.y === current.y) {
               continue;
            }
            proj.push(this._tilePoint(ctx, coords[i]));
            previous = current;
         }
         //console.log(proj.length + "_" + coords.length)
         if (!this._isActuallyVisible(proj)) {
            continue;
         }
         g.fillStyle = style.color;
         if (outline) {
            g.strokeStyle = outline.color;
            g.lineWidth = outline.size;
         }
         g.beginPath();
         for (i = 0; i < proj.length; i++) {
            var method = (i === 0 ? 'move' : 'line') + 'To';
            g[method](proj[i].x, proj[i].y);
         }
         g.closePath();
         g.fill();
         if (outline) {
            g.stroke();
         }
      }
   },
   _draw: function(ctx, tilePoint, zoom) {
      // NOTE: this is the only part of the code that depends from external libraries (actually, jQuery only).        
      var loader = d3.xhr;
      loader.error = function(jqXHR, textStatus, errorThrown) {
         console.log(textStatus + " " + errorThrown)
      };
      var tileX = tilePoint.x,
         tileY = tilePoint.y,
         tileId = tileX + "_" + tileY + "_" + zoom;
      var nwPoint = ctx.tile.multiplyBy(this.tileSize);
      var sePoint = nwPoint.add(new L.Point(this.tileSize, this.tileSize));
      // optionally, enlarge request area.
      // with this I can draw points with coords outside this tile area,
      // but with part of the graphics actually inside this tile.
      // NOTE: that you should use this option only if you're actually drawing points!
      /*var buf = this.options.buffer;
        if (buf > 0) {
            var diff = new L.Point(buf, buf);
            nwPoint = nwPoint.subtract(diff);
            sePoint = sePoint.add(diff);
        }*/
      var nwCoord = this._map.unproject(nwPoint, ctx.zoom, true),
         seCoord = this._map.unproject(sePoint, ctx.zoom, true),
         bounds = [nwCoord.lng, seCoord.lat, seCoord.lng, nwCoord.lat],
         url = this.options.url(bounds, tileX, tileY, zoom),
         self = this,
         j;
      loader(url, function(data) {
         if (data.responseText === "-1") {
            return;
         }
         var data = jQuery.parseJSON(data.responseText.split("________")[1]),
            o = data.objects[tileId];
         var geoJS = topojson.feature(data, o).features;
         for (var i = 0; i < geoJS.length; i++) {
            var feature = geoJS[i],
               style = self.options.style(feature.id),
               type = feature.geometry.type,
               geom = feature.geometry.coordinates,
               len = geom.length;
            switch (type) {
               case 'Point':
                  self._drawPoint(ctx, geom, style);
                  break;
               case 'MultiPoint':
                  for (j = 0; j < len; j++) {
                     self._drawPoint(ctx, geom[j], style);
                  }
                  break;
               case 'LineString':
                  self._drawLineString(ctx, geom, style);
                  break;
               case 'MultiLineString':
                  for (j = 0; j < len; j++) {
                     self._drawLineString(ctx, geom[j], style);
                  }
                  break;
               case 'Polygon':
                  self._drawPolygon(ctx, geom, style);
                  break;
               case 'MultiPolygon':
                  for (j = 0; j < len; j++) {
                     self._drawPolygon(ctx, geom[j], style);
                  }
                  break;
               default:
                  throw new Error('Unmanaged type: ' + type);
            }
         }
      });
   },
   _tileLoaded: function() {
      this._tilesToLoad--;
      if (this._tilesToLoad === 0) {
         this.options.loaded();
      }
      if (this._animated) {
         L.DomUtil.addClass(this._tileContainer, 'leaflet-zoom-animated');
      }
      if (!this._tilesToLoad) {
         this.fire('load');
         if (this._animated) {
            // clear scaled tiles after all new tiles are loaded (for performance)
            clearTimeout(this._clearBgBufferTimer);
            this._clearBgBufferTimer = setTimeout(L.bind(this._clearBgBuffer, this), 500);
         }
      }
   },
   // NOTE: a placeholder for a function that, given a feature, returns a style object used to render the feature itself
   styleFor: function(feature) {
      // override with your code
   }
});