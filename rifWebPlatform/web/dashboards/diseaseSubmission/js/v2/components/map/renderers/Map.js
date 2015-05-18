RIF.Map = (function() {

   var _map;
   var _initialExtent = null;

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
      // Disease mapping specific - hack to have a global variable for map extent
      _map.on('moveend', function(e) {
         RIF.mapExtent = _map.getBounds();
      });
   };

   this.setFullExtent = function(geolevel) {
      RIF.getFullExtent(this.zoomTo, [geolevel]);
   };


   this.addTiled = function(lyr, geoTable) {
      if (_initialExtent == null) {
         this.setFullExtent(geoTable);
      } else {
         this.zoomTo();
      };
      _map.addLayer(lyr);
   };

   this.removeLayer = function() {
      if (typeof this.layer === 'object') {
         $('svg.leaflet-zoom-animated g').remove();
         _map.removeLayer(this.layer);
         _map.invalidateSize();
      };
   };

   this.setFullExtent = function(geolevel, ext) {
      RIF.getFullExtent(this.zoomTo, [geolevel]);
   };

   this.setInitialExtent = function(ext) {
      RIF.mapExtent = _initialExtent = ext;
   };

   this.zoomTo = function() {
      if (typeof this[0] == 'object') {
         var ymax = this[0]['ymax'],
            xmax = this[0]['xmax'],
            xmin = this[0]['xmin'],
            ymin = this[0]['ymin'];
      } else {
         var ymax = _initialExtent[0]['ymax'],
            xmax = _initialExtent[0]['xmax'],
            xmin = _initialExtent[0]['xmin'],
            ymin = _initialExtent[0]['ymin'];
      };
      var bounds = [
      [ymax, xmax],
      [ymin, xmin]
    ];
      _fitBounds(bounds);
   };


   this.getExtent = function() {
      return _map.getBounds()
   };

   var _fitBounds = function(bounds) {
      _map.fitBounds(bounds);
   };




});