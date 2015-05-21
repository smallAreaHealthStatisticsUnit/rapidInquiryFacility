RIF.TileTopojsonUtils = (function(tooltip, geolvl, counter) {

   var that = this;
   var slctd = RIF.mapAreasSelected = [];

   /* Inherit style */
   RIF.LayerStyle.apply(this, []);

   /* Global map area selection to facilitate sync, to be refactored in the future*/
   that.studyMapAreaSelection = [];

   var addToSelection = function(dProperties) {
      slctd.push({
         "area_id": dProperties["area_id"],
         gid: String(dProperties.gid),
         label: dProperties.name
      });
   };

   var removeFromSelection = function(dProperties) {
      var l = slctd.length;
      for (var i = 0; i < l; i++) {
         if (dProperties.gid == slctd[i].gid) {
            slctd.splice(i, 1);
            break;
         };
      };
   };

   var _slct = function(dProperties, isSlctd, id) {
      var id = (typeof id == 'undefined') ? 'g' + dProperties.gid : id;
      tiled.addOrRemoveId(id, isSlctd);
      if (isSlctd) {
         that.style.unhighlight(id);
         removeFromSelection(dProperties);
      } else {
         that.style.highlight(id);
         addToSelection(dProperties);
      };
      $(counter).text(slctd.length);
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
          'geoLevelSelectName=' + geolvl
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

      slct: function(d) {
         var actualPathId = 'g' + d.gid; //Coming from mediator subscriber, cannot use getPathTildId
         var isSlctd = tiled.isSlctd(actualPathId);
         tiled.addOrRemoveId(actualPathId, isSlctd);
         if (!isSlctd) {
            that.style.highlight(actualPathId);
            addToSelection(d);
         };
         $(counter).text(slctd.length);
      },


      getStyle: function(d) {
         var id = tiled.getPathId(d);
         if (tiled.isSlctd(id)) {
            return that.style.getSelectedStyle();
         } else {
            return that.style.getDefaultStyle();
         }
      },

      getClass: function(d) {
         var id = tiled.getPathId(d);
         if (tiled.isSlctd(id)) {
            return "areaSelected polygon";
         } else {
            return "polygon";
         }
      },

      addToSelection: function(d) {
         slctd.push({
            id: d.properties["area_id"],
            id: d.properties.gid,
            label: d.properties.name
         });

      },

      removeFromSelection: function(d) {

      },

      clear: function(mapContainerId) {
         tiled.selection = {};
         slctd = RIF.mapAreasSelected = [];
         that.style.unhighlightAll(mapContainerId);
      },

      evntHndl: function(c, d) {
         /* 'this' is the intialized layer's context */
         var id = this.id;
         var isSlctd = tiled.isSlctd(id);
         switch (c) {
            case "click":
               _slct(d /*properties*/ , isSlctd);
               break;
            case "mouseout":
               break;
            case "mouseover":
               /*
                * This should be replaced when getAttribute web service is ready
                */
               $(tooltip).find('div').text(d.properties.name)
               break;
         }
      }
   };

   return {
      class: tiled.getClass,
      evntHndl: tiled.evntHndl,
      id: tiled.getPathId,
      resetIds: tiled.resetIds,
      deduplicate: tiled.checkId,
      getTilesParams: tiled.getTilesParams,
      getStyle: tiled.getStyle,
      slct: tiled.slct,
      clear: tiled.clear
   };

});