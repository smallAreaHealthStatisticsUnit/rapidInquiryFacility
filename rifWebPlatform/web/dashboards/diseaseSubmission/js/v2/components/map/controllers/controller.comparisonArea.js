RIF['map']['controller-comparisonArea'] = (function (unit) {

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

    syncComparisonArea: function (selection) {
      unit.sync(selection);
    },

    clearSelection: function () {
      unit.clearAll();
    },

    removeMapGs: function () {
      unit.removeMapGs();
    }

  };

  return _p;

});