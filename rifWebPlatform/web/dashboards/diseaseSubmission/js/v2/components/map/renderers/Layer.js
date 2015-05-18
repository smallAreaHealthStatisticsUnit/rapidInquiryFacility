RIF.Layer = (function(mapId, tooltipId) {

   /* Initialize a Map */
   this.makeMap(mapId);

   var _getOptions = function(geolvl) {
      var utils = RIF.TileTopojsonUtils(tooltipId, geolvl);
      var options = RIF.utils.mix(utils, {
         mapId: mapId
      });
      return options;
   };

   //this.addTiled( new L.TileLayer.TileTopojson( '', options ), "LEVEL4" );

   this.initLayer = function(geolvl) {
      this.removeLayer();
      this.layer = new L.TileLayer.TileTopojson('', _getOptions(geolvl));;
      this.addTiled(this.layer, geolvl);
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


   this.selectAreas = function(ids) {
      var l = ids.length;
      while (l--) {
         var id = "g" + ids[l];
         this.slct(id);
      };
   };

   this.setGeolevel = function(geolvl) {
      this.geolevel = geolvl;
   };

   this.getGeolevel = function() {
      return this.geolevel || 'noGeolevelSelected';
   };

});

// Inherit from Map
RIF.Layer.prototype = new RIF.Map();