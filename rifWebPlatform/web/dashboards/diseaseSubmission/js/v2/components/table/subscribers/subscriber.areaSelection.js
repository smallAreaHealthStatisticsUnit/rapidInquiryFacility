RIF.table['subscriber-areaSelection'] = (function(controller) {

   var subscriber = {

      selectAtChangeUpdate: function(geolvl) {
         //console.log(RIF.mapExtent)    
         controller.getTabularData(geolvl);
      },
      syncStudyArea: function() {
         controller.getTabularData();
      }

   };

   return subscriber;
});