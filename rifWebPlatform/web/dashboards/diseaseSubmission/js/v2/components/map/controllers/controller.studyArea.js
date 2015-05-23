RIF['map']['controller-studyArea'] = (function (unit) {

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
    },

    refreshMap: function () {
      unit.refreshMap();
    }

  };

  return _p;

});