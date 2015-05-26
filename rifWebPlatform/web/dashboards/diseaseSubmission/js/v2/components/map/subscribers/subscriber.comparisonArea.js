RIF['map']['subscriber-comparisonArea'] = (function (controller) {

  var subscriber = {

    userLoggedIn: function () {
      controller.setGlobalMapFullExtent();
    },

    /*setMapExtent: function(){
        console.log('setting comparison area extent');
    },*/

    comparisonSelectAtChangeUpdate: function (geolvl) {
      controller.updateGeolevel(geolvl);
    },

    /*setInitialExtent: function(){
        controller.setInitialExtent();
    },  
      
    syncStudyAreaMap: function( gids ){
        controller.syncStudyArea( gids );
    },
      
    clearStudySelection: function(){
        controller.clearSelection();
    } */


  };

  return subscriber;
});