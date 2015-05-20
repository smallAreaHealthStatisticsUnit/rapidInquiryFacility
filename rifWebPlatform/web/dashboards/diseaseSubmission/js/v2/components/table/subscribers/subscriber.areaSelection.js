RIF.table['subscriber-areaSelection'] = (function(controller) {

   var subscriber = {

      selectAtChangeUpdate: function(geolvl) {
         controller.getTabularData(geolvl);
      },
      syncStudyAreaTable: function(mapAreas) {
         controller.syncTabularData(mapAreas);
      }

   };

   return subscriber;
});