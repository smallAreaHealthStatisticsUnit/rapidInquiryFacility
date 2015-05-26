RIF['map']['subscriber-studyArea'] = (function (controller) {


  var subscriber = {

    userLoggedIn: function () {
      controller.setGlobalMapFullExtent();
    },

    setMapExtent: function () {
      console.log('setting study area extent');
    },

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