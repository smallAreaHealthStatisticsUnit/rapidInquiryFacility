RIF['map']['firer-comparisonArea'] = (function () {

  var firer = {

    comparisonMapAreaSelectionEvent: function (mapId) {
      this.fire('comparisonMapAreaSelectionEvent', RIF[mapId]);
    }

  };

  return firer;
});