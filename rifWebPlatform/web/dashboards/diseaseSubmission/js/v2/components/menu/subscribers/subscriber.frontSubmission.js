RIF.menu['subscriber-frontSubmission'] = (function(  controller ) {

   var subscriber = {
      /* Subscribers */
      updateHealthThemeAvailables: function() {},
      updateNumeratorAvailables: function() {},
      updateDenominatorAvailables: function() {},
      /*investigationReadyToBeAdded: function() {
         _p.proxy.investigationReadyToBeAdded();
      },
      investigationNotReadyToBeAdded: function() {
         _p.proxy.investigationNotReadyToBeAdded();
      },*/

      showDialog: function() {
         controller.showDialog();
      },
      
      logOut: function(){
        controller.logOut();
      }   
         
   };
    
   return subscriber;
});