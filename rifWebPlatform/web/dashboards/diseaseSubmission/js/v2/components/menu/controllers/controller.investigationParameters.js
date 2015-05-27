RIF.menu['controller-investigationParameters'] = (function (unit) {

  var _p = {

    getYears: function (val) {
      var clbk = function () {
        var lower = [this[0].lowerBound],
          upper = [this[0].upperBound],
          years = [];
        while (lower <= upper) {
          years.push(lower++);
        };
        unit.getYears(years);

      };
      RIF.getYearRange(clbk, [val]);
    },

    getCovariates: function (selectAt, resoution) {
      var clbk = function () {
        unit.getCovariates(this);
      };
      RIF.getCovariates(clbk, [selectAt, resoution]);
    },

    getGender: function () {
      var clbk = function () {
        var genders = this[0].names;
        unit.getGender(genders);
      };
      RIF.getSexes(clbk, null);
    },

    startInvestigationParameter: function (num, selectAt, resoution) {
      _p.getYears(num);
      _p.getCovariates(selectAt, resoution);
      _p.getGender();
    },

    investigationReadyToBeAdded: function () {
      unit.investigationReadyToBeAdded();
    },

    investigationNotReadyToBeAdded: function () {
      unit.investigationNotReadyToBeAdded();
    }
  };

  return _p;

});