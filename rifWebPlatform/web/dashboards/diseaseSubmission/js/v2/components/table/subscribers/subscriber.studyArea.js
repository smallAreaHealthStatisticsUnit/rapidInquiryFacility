RIF.table['subscriber-studyArea'] = (function (controller) {

  var subscriber = {

    selectAtChangeUpdate: function (geolvl) {
      controller.getTabularData(geolvl);
    },
    syncStudyAreaTable: function (mapAreas) {
      controller.syncTabularData(mapAreas);
    },

    studySelectAllRowsClicked: function () {
      controller.studySelectAllRows();
      var slctd = controller.getSelection();
      this.studyAreaSelectionEvent(slctd);
    },

    clearStudySelection: function () {
      controller.clearSelection();
    }

  };
  return subscriber;
});