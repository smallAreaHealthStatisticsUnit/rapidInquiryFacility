RIF.table['controller-ageGroups'] = (function (unit) {

  var _p = {
    getAgeGroups: function (numerator) {
      var _callback = function () {
        unit.getAgeGroups(this);
      };
      RIF.getAgeGroups(_callback, [numerator]);
    }
  };


  return _p;
});