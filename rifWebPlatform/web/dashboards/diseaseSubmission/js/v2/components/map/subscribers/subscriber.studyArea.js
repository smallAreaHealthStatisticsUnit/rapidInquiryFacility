RIF['map']['subscriber-studyArea'] = (function(controller) {

   var subscriber = {

      selectAtChangeUpdate: function(geolvl) {
         controller.updateGeolevel(geolvl);
      },

      setInitialExtent: function() {
         controller.setInitialExtent();
      },

   };

   return subscriber;

});