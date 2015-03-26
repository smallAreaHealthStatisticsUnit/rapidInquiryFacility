RIF.menu['firer-investigationParameters'] = (function() {

   var firer = {

      startYearChanged: function(arg) {
         this.fire('startYearChanged', arg);
      },
      endYearChanged: function(arg) {
         this.fire('endYearChanged', arg);  
      },
      genderChanged: function(arg) {
         this.fire('genderChanged', arg);   
      },
      covariatesChanged: function(arg) {
         this.fire('covariatesChanged', arg);
      },
       
      isInvestigationReady: function() {
         this.fire('isInvestigationReady', null);
      },
      addInvestigation: function() {
         this.fire('addInvestigation', null);
      },
      clearAllParameters: function() {
         this.fire('clearAllParameters', null);  
      }

   };
   return firer;
});