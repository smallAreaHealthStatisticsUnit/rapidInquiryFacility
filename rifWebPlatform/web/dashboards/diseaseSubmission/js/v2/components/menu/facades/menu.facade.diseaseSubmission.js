RIF.menu['facade-frontSubmission'] = (function(_p) {
   /*
    * Facades can only communicate to main component.
    * Cannot call directly component sub units
    */
   var facade = {
      /* Subscribers */
      updateHealthThemeAvailables: function() {},
      updateNumeratorAvailables: function() {},
      updateDenominatorAvailables: function() {},
      investigationReadyToBeAdded: function() {
         _p.proxy.investigationReadyToBeAdded();
      },
      investigationNotReadyToBeAdded: function() {
         _p.proxy.investigationNotReadyToBeAdded();
      },
      addInvestigationRow: function(arg) {
         _p.proxy.addInvestigationRow(arg);
      },
      showDialog: function(dialog) {
         _p.proxy.showDialog(dialog);
      },
      /* Firers  */
      studyNameChanged: function(arg) {
         this.fire('studyNameChanged', arg);
      },
      healthThemeChanged: function(arg) {
         this.fire('healthThemeChanged', arg);
      },
      numeratorChanged: function(arg) {
         this.fire('numeratorChanged', arg);
      },
      denominatorChanged: function(arg) {
         this.fire('denominatorChanged', arg);
      },
      selectAtChanged: function(arg) {
         this.fire('selectAtChanged', arg);
      },
      resolutionChanged: function(arg) {
         this.fire('resolutionChanged', arg);
      },
      taxonomyChanged: function(arg) {
         this.fire('taxonomyChanged', arg);
      },
      icdSelectionChanged: function(arg) {
         this.fire('icdSelectionChanged', arg);
      },
      // investigation parameters   
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
      },
      isDialogReady: function(dialog) {
         this.fire('isDialogReady', dialog);
      }
      /* Study Related */
   };
   return facade;
});