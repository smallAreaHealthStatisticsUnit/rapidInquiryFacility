RIF['menu']['firer-comparisonArea'] = (function () {

  var firer = {

    comparisonSelectAtChanged: function (arg) {
      this.fire('comparisonSelectAtChanged', arg);
      this.getResolutions(arg);
    },

    comparisonResolutionChanged: function (arg) {
      this.fire('comparisonResolutionChanged', arg);
    },

    comparisonSyncTableButtonClicked: function () {
      this.fire('comparisonSyncTableButtonClicked', []);
    },

    comparisonSyncMapButtonClicked: function () {
      this.fire('comparisonSyncMapButtonClicked', []);
    },

    comparisonMapAreaSelectionEvent: function (areaId) {
      this.fire('comparisonMapAreaSelectionEvent', RIF.mapAreasSelected);
    },

    comparisonSelectAllRowsClicked: function () {
      this.fire('comparisonSelectAllRowsClicked', []);
    },

    comparisonClearAreaSelectionEvent: function () {
      this.fire('comparisonClearAreaSelectionEvent', ['comparisonArea']);
    },

  };

  return firer;

});