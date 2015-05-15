RIF.Map = (function() {

   var _map;

   this.makeMap = function(mapId) {
      var options = {
         attributionControl: false,
      };
      _map = new L.Map(mapId, options);
      new L.geoJson({
         "type": "LineString",
         "coordinates": [
        [0, 0],
        [0, 0]
      ]
      }).addTo(_map);
      _map.invalidateSize();
      _map.doubleClickZoom.disable();
   };

   this.setFullExtent = function(geolevel) {
      RIF.getFullExtent(this.zoomTo, [geolevel]);
   };


   this.removeLayer = function() {
      if (typeof this.layer === 'object') {
         $('svg.leaflet-zoom-animated g').remove();
         this.layer.clearLegend();
         _map.removeLayer(this.layer.mylyr);
         _map.invalidateSize();
      };
   };

   this.addTiled = function(lyr, geoTable) {
      if (!this.extentSet) {
         this.setFullExtent(geoTable);
      };
      _map.addLayer(lyr);
   };

   this.setFullExtent = function(geolevel) {
      this.extentSet = 0;
      RIF.getFullExtent(this.zoomTo, [geolevel]);
   };

   this.zoomTo = function() { /* Json context */
      //var bounds = this;    
      var ymax = this[0]['ymax'],
         xmax = this[0]['xmax'],
         xmin = this[0]['xmin'],
         ymin = this[0]['ymin'];
      var bounds = [
      [ymax, xmax],
      [ymin, xmin]
    ];
      _fitBounds(bounds);
   };

   var _fitBounds = function(bounds) {
      _map.fitBounds(bounds);
   };


});