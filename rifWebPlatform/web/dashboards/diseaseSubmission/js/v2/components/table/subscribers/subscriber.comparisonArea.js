RIF['table']['subscriber-comparisonArea'] = (function (controller) {

  var subscriber = {

    comparisonSelectAtChangeUpdate: function (geolvl) {
      controller.getTabularData(geolvl);
    },

    syncComparisonAreaTable: function (mapAreas) {
      controller.syncTabularData(mapAreas);
    },

    comparisonSelectAllRowsClicked: function () {
      controller.comparisonSelectAllRows();
      var slctd = controller.getSelection();
      this.comparisonAreaSelectionEvent(slctd);
    },

    comparisonClearStudySelection: function () {
      controller.clearSelection();
    }

  };

  return subscriber;
});