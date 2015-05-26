RIF['menu']['controller-comparisonArea'] = (function (unit) {

  var _p = {

    getSelectAtsAvailable: function () {
      var callback = function () {
        var selectAts = this[0].names;
        unit.getSelectAt(selectAts);
      };

      RIF.getSelectAt(callback, null);
    },

    getResolutionsAvailable: function (selectAt) {
      var callback = function () {
        var selectAts = this[0].names;
        unit.getResolutions(selectAts);
      };

      RIF.getResolutions(callback, [selectAt]);
    }
  };

  return _p;

});