RIF.Map = (function (mapId) {

  var _map;
  var _ext;
  var _geolvlsChangeCount = 0;

  var _fitBounds = function (bounds) {
    _map.fitBounds(bounds);
  };

  this.makeMap = function () {
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
    _map.on('moveend', function (e) {
      var lBounds = _map.getBounds();
      RIF.mapExtent = {
        ymax: lBounds['_northEast']['lat'],
        xmax: lBounds['_northEast']['lng'],
        xmin: lBounds['_southWest']['lng'],
        ymin: lBounds['_southWest']['lat'],
      };
    });
  }();

  this.setExtent = function (ext) {
    _ext = ext;
  };

  this.addTiled = function (lyr, geoTable) {
    //this.setInitialExtent();
    this.zoomToExtent();
    _map.addLayer(lyr);
  };

  this.removeLayer = function (lyr) {
    if (typeof lyr === 'object') {
      $('#' + mapId + ' svg.leaflet-zoom-animated g').remove();
      $('#' + mapId + ' svg.leaflet-zoom-animated g path').unbind();
      _map.removeLayer(lyr);
    };
  };

  this.getFullExtent = function (geolevel, ext) {
    RIF.getFullExtent(this.zoomToFullExtent, [geolevel]);
  };

  this.zoomToFullExtent = function () {
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



  this.zoomToExtent = function () {
    if (_ext == null) {
      this.getFullExtent(geoTable);
    } else if (_geolvlsChangeCount++ == 0) {
      this.zoomTo();
    };
  };

  this.zoomTo = function () {
    if (typeof _ext == null) { // Coming from callback getFullExtyent
      var ymax = this[0]['ymax'],
        xmax = this[0]['xmax'],
        xmin = this[0]['xmin'],
        ymin = this[0]['ymin'];
    } else { //coming from zoomToExtent can either be
      var ymax = _ext['ymax'],
        xmax = _ext['xmax'],
        xmin = _ext['xmin'],
        ymin = _ext['ymin'];
    };
    var bounds = [
      [ymax, xmax],
      [ymin, xmin]
    ];
    _fitBounds(bounds);
  };


});