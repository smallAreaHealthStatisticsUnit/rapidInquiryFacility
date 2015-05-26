RIF['table']['firer-comparisonArea'] = (function () {

  var firer = {
    comparisonAreaSelectionEvent: function (rowId) {
      this.fire('comparisonAreaSelectionEvent', rowId);
    },
  };

  return firer;

});