RIF['map']['unit-studyArea'] = (function (_dom, menuUtils) {


  var mapLayer = new RIF.Layer(_dom.id, _dom.tooltip, _dom.areaCounter);

  var _p = {

    updateCounter: function (n) {
      $(_dom.areaCounter).text(n);
    },

    updateGeolevel: function (geolvl) {
      mapLayer.initLayer(geolvl);
      _p.updateCounter(0);
    },

    sync: function (selection) {
      mapLayer.clearSelection(_dom.id);
      mapLayer.selectAreas(selection);
      _p.updateCounter(selection.gid.length);
    },

    clearAll: function () {
      mapLayer.clearSelection(_dom.id);
      _p.updateCounter(0);
    }

  };

  return _p;

});