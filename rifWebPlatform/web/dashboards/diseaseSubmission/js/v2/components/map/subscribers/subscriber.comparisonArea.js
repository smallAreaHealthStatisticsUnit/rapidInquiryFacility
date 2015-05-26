RIF['map']['subscriber-comparisonArea'] = (function (controller) {

  var subscriber = {

    userLoggedIn: function () {
      controller.setGlobalMapFullExtent();
    },

    syncComparisonAreaMap: function (gids) {
      controller.syncComparisonArea(gids);
    },

    comparisonSelectAtChangeUpdate: function (geolvl) {
      controller.updateGeolevel(geolvl);
    },

    comparisonClearStudySelection: function () {
      controller.clearSelection();
    }

    /*setInitialExtent: function(){
        controller.setInitialExtent();
    },  
       
     */


  };

  return subscriber;
});