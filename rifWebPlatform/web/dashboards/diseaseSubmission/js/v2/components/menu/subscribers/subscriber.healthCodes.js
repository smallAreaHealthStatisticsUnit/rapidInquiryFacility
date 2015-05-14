RIF.menu['subscriber-healthCodes'] = (function(controller) {

   var subscriber = {
      startInvestigationParameter: function() {
         controller.getTaxonomy();
      },

      updateSubLevelHealthCodes: function(params) {
         controller.getSubLevelHealthCodes(params);
      },

      updateTopLevelHealthCodes: function(taxonomy) {
         controller.getTopLevelHealthCodes(taxonomy);
      },

      searchHealthCodes: function(params) {
         controller.getSearchHealthCodes(params);
      },

   };

   return subscriber;
});