RIF['map']['subscriber-studyArea'] = (function (controller) {


  var subscriber = {

    selectAtChangeUpdate: function (geolvl) {
      controller.updateGeolevel(geolvl);
    },

    syncStudyAreaMap: function (gids) {
      controller.syncStudyArea(gids);
    },

    clearStudySelection: function () {
      controller.clearSelection();
    }

  };

  return subscriber;

});