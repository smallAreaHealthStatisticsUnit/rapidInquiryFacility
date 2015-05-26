RIF['table']['controller-comparisonArea'] = (function (unit) {

  var _currentGeolvl;

  var _getBounds = function () {
    var bounds = {
      ymax: RIF.mapExtent['ymax'],
      xmax: RIF.mapExtent['xmax'],
      ymin: RIF.mapExtent['ymin'],
      xmin: RIF.mapExtent['xmin']
    };
    return bounds;
  };

  var _p = {

    getTabularData: function (geolvl) {
      if (typeof geolvl != 'undefined') {
        _currentGeolvl = geolvl;
      };
      var callback = function () {
        unit.empty();
        unit.getTabularData(this);
      };
      RIF.getTableMapAreas(callback, [_currentGeolvl, _getBounds()]);
    },

    syncTabularData: function (mapAreas) {
      var callback = function () {
        unit.empty();
        unit.getTabularDataFromMap(mapAreas);
        unit.getTabularData(this);
      };

      RIF.getTableMapAreas(callback, [_currentGeolvl, _getBounds()]);
    },

    comparisonSelectAllRows: function () {
      unit.selectAll();
    },

    getSelection: function () {
      return unit.getSelection();
    },

    clearSelection() {
      unit.clearAll();
    }

  };


  return _p;

});