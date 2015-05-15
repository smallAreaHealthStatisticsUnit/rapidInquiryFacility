RIF.TileTopojsonUtils = (function(tooltip) {

   var that = this;

   /* Inherit style */
   RIF.LayerStyle.apply(this, []);


   var _slct = function(id, isSlctd) {
      tiled.addOrRemoveId(id, isSlctd);
      if (isSlctd) {
         that.style.unhighlight(id, isSlctd);
      } else {
         that.style.highlight(id, isSlctd);
      };
   };

   var tiled = {

      ids: {},
      selection: {},
      firstLoad: true,

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

      addOrRemoveId: function(id, isSlctd) {
         if (isSlctd) {
            delete this.selection[id];
         } else {
            this.selection[id] = 1;
         }
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

      isSlctd: function(id) {
         if (this.selection[id] === undefined) {
            return false;
         }
         return true;
      },

      getStyle: function(d) {
         var id = tiled.getPathId(d);
         if (tiled.isSlctd(id)) {
            return that.style.getSelectedStyle();
         } else {
            return that.style.getDefaultStyle();
         }
      },

      evntHndl: function(c) {
         /* 'this' is the intialized layer's context */
         var id = this.id;
         var isSlctd = tiled.isSlctd(id);
         switch (c) {
            case "click":
               _slct(id, isSlctd);
               //console.log( tiled.selection )      
               break;
            case "mouseout":
               break;
            case "mouseover":
               $(tooltip).find('div').text(this.id)
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
      getTilesParams: tiled.getTilesParams,
      getStyle: tiled.getStyle
   };

});