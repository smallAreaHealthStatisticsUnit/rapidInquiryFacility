RIF.TileTopojsonUtils = (function() {

   var tiled = {

      ids: {},
      firstLoad: true,

      /*getTilesParams: function ( tilePoint, zoom ) {
        var tileX = tilePoint.x,
          tileY = tilePoint.y,
          tileId = tileX + '_' + tileY,
          start = tiled.getTileBounds( tileX, tileY, zoom ),
          to = tiled.getTileBounds( tileX + 1, tileY + 1, zoom ),
          url = [
            'xMin=' + start[ 0 ], //'-6.68853', //
            'yMax=' + Math.abs( start[ 1 ] ), //'54.6456', //Math.abs( start[ 1 ] ),
            'xMax=' + to[ 0 ], //'-6.32507', //
            'yMin=' + Math.abs( to[ 1 ] ), //'55.0122', //
            'zoomFactor=' + zoom,
            'tileIdentifier=' + tileId,
            'geoLevelSelectName=' + 'LEVEL4'
          ].join( "&" );
        return url;
      },*/

      getTilesParams: function(tilePoint, zoom) {
         var tileX = tilePoint.x,
            tileY = tilePoint.y,
            tileId = tileX + '_' + tileY,
            params = [
            'xTileIdentifier=' + tileX,
            'yTileIdentifier=' + tileY,
            'zoomFactor=' + zoom,
            'geoLevelSelectName=' + 'LEVEL4'
           ].join("&");

         return params;
      },

      getTileBounds: function(tilePointX, tilePointY, zoom) {
         //get Mercator lat / lon coordinates from tile number
         var originShift = Math.PI * 6378137,
            initialRes = 2 * Math.PI * 6378137 / 256,
            px = tilePointX * 256,
            py = tilePointY * 256,
            res = initialRes / (Math.pow(2, zoom)),
            coords_x = px * res - originShift,
            coords_y = py * res - originShift,
            coords_x_4326 = coords_x / originShift * 180.0,
            coords_y_4326 = coords_y / originShift * 180.0,
            coords_y_4326 = 180 / Math.PI * (2 * Math.atan(Math.exp(coords_y_4326 * Math.PI / 180)) - Math.PI / 2.0);
         return [coords_x_4326, coords_y_4326];
      },
      /*
       *  Should move highlight method to style.tilesvg
       */
      highlight: function(id, slctd) {
         var s = layer.getLayerStyle(id, slctd);
         d3.select("#" + id).style({
            "fill": s.fill,
            "stroke": s.stroke,
            "stroke-width": s.stroke_width,
            "opacity": 1
         })
      },
      getStyle: function(d) {
         /*var id = tiled.getPathId( d ),
          isSlctd = layer.isSlctd( id );
        if ( isSlctd ) {
          return "fill:" + layer.style.slctd.fill;
        }*/

         //return layer.style.getStyle( /*id*/ '', "tilesvg" );
      },

      getPathId: function(d) {
         var id = RIF.utils.addG(d.id); //+ "__" + d.properties.field.trim();
         return id.replace(/\s+/g, '');
      },

      getPathTitle: function(d) {
         return tiled.titles[d.id];
      },

      resetIds: function() {
         tiled.ids = {};
      },

      checkId: function(d) {
         var id = tiled.getPathId(d);
         if (typeof tiled.ids[id] === "undefined") {
            tiled.ids[id] = 1;
            return true;
         };
         return false;
      },

      evntHndl: function(c) {
         /* 'this' is the intialized layer's context */
         var isSlctd = layer.isSlctd(this.id);
         switch (c) {
            case "click":
               layer.slct(this.id, isSlctd);
               break;
            case "mouseout":
               if (!isSlctd) {
                  this.style.fill = layer.style.colors[this.id];
               }
               layer.out.call(this);
               break;
            case "mouseover":
               var id = RIF.removeG(this.id),
                  label = layer.hoverLbls[id];
               layer.hover.call(this, label);
               if (!isSlctd) {
                  this.style.fill = layer.style.hover.fill;
                  return;
               }
               break;
         }
      }

   };

   return {
      class: "polygon",
      evntHndl: tiled.evntHndl,
      id: tiled.getPathId,
      resetIds: tiled.resetIds,
      deduplicate: tiled.checkId,
      style: tiled.getStyle,
      getTilesParams: tiled.getTilesParams
   };

});