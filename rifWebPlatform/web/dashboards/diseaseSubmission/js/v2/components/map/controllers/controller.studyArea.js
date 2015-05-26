RIF['map']['controller-studyArea'] = (function (unit) {

  var _p = {

    setGlobalMapFullExtent: function () {
      var callback = function () {
        var ext = this[0];
        unit.setExtent(ext);
      };
      RIF.getFullExtent(callback, []);
    },

    updateGeolevel: function (geolvl) {
      unit.updateGeolevel(geolvl);
    },

    syncStudyArea: function (selection) {
      unit.sync(selection);
    },

    clearSelection: function () {
      unit.clearAll();
    },

    removeMapGs: function () {
      unit.removeMapGs();
    },

    refreshMap: function () {
      unit.refreshMap();
    }

  };

  return _p;

});