RIF['map']['controller-comparisonArea'] = (function (unit) {

  var _p = {

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
    }

  };

  return _p;

});