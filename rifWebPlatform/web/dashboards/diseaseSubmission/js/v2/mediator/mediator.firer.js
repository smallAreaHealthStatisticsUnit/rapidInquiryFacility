RIF.mediator['firer-'] = (function() {


   var firer = {
      // FIRERS  
      selectAtChangeUpdate: function(geolvl) {
         this.fire('selectAtChangeUpdate', geolvl);
      },
      investigationReadyToBeAdded: function() {
         this.investigationReady = true;
         this.fire('investigationReadyToBeAdded', null);
      },
      investigationNotReadyToBeAdded: function() {
         this.investigationReady = false;
         this.fire('investigationNotReadyToBeAdded', null);
      },
      showDialog: function(dialog) {
         this.fire('showDialog', dialog);
      },

      startInvestigationParameter: function(num) {
         this.fire('startInvestigationParameter', num);
      },

      startAreaSelection: function() {
         this.fire('startAreaSelection', null);
      },

      startStatDialog: function() {
         this.fire('startStatDialog', null);
      },

      addInvestigationRow: function(nInvestigation, parameters) {
         this.fire('addInvestigationRow', [nInvestigation, parameters]);
      },

      modelToSchemaReady: function(modelToSchema) {
         this.fire('modelToSchemaReady', [modelToSchema]);
      },
   };

   return firer;
});