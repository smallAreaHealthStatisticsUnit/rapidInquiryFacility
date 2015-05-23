RIF['map']['firer-studyArea'] = (function () {

  var firer = {

    studyMapAreaSelectionEvent: function (mapId) {
      this.fire('studyMapAreaSelectionEvent', RIF[mapId]);
    },

  };

  return firer;

});