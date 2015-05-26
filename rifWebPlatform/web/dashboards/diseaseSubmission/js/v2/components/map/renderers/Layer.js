RIF.Layer = (function (mapId, tooltipId, counter) {

  var _layer, _utils;

  var _map = new RIF.Map(mapId);


  var _getOptions = function (geolvl) {
    _utils = RIF.TileTopojsonUtils(tooltipId, geolvl, counter, mapId);
    var options = RIF.utils.mix(_utils, {
      mapId: mapId
    });
    return options;
  };


  this.initLayer = function (geolvl) {
    _map.removeLayer(_layer);
    _layer = new L.TileLayer.TileTopojson('', _getOptions(geolvl));;
    _map.addTiled(_layer, geolvl);
  };

  this.resetSlctd = function () {
    for (var key in _selection) {
      var e = $("#" + key);
      e[0].style.fill = style.colors[key];
    }
  };

  this.repaintSlctd = function () {
    for (var key in _selection) {
      this.highlight(key);
    };
  };

  this.clearSelection = function (mapContainerId) {
    _utils.clear.call(null, mapContainerId);
  };

  this.selectAreas = function (selection) {
    var l = selection.gid.length;
    while (l--) {
      var row = selection[l];
      _utils.slct({
        gid: selection.gid[l],
        name: selection.name[l],
        "area_id": selection["area_id"][l]
      });
    };
  };

  this.setGeolevel = function (geolvl) {
    this.geolevel = geolvl;
  };

  this.getGeolevel = function () {
    return this.geolevel || 'noGeolevelSelected';
  };

  this.setExtent = function (ext) {
    _map.setExtent(ext)
  };


});