RIF.table['firer-studyArea'] = (function () {

  var firer = {
    studyAreaSelectionEvent: function (rowId) {
      this.fire('studyAreaSelectionEvent', rowId);
    },
  };
  return firer;
});