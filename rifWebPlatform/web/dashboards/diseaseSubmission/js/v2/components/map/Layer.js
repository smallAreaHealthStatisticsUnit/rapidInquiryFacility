RIF.Layer = (function(mapId) {

   /* Initialize a Map */
   this.makeMap(mapId);


   /* Add Tiled Layer */
   var utils = RIF.TileTopojsonUtils();
   this.addTiled(new L.TileLayer.TileTopojson('', utils), "LEVEL4");

   /* Rents a style layer object */
   RIF.LayerStyle.call(this, []);

   /* Layer specific - implementation starts */
   var _selection = {};

   this.initLayer = function() {
      console.log('layerInit');
   };

   this.resetSlctd = function() {
      for (var key in _selection) {
         var e = $("#" + key);
         e[0].style.fill = style.colors[key];
      }
   };

   this.repaintSlctd = function() {
      for (var key in _selection) {
         this.highlight(key);
      };
   };

   this.isSlctd = function(id) {
      if (_selection[id] === undefined) {
         return false;
      }
      return true;
   };

   this.slct = function(id) {
      this.addRemoveId(id);
      this.selectionChanged()
      this.highlight(id);
   };


   this.addRemoveId = function(id) {
      if (typeof this.selection[id] === 'undefined') {
         _selection[id] = 1;
      } else {
         delete _selection[id];
      }
   };

   this.selectAreas = function(ids) {
      var l = ids.length;
      while (l--) {
         var id = "g" + ids[l];
         this.slct(id);
      };
   };


});

// Inherit from Map
RIF.Layer.prototype = new RIF.Map();