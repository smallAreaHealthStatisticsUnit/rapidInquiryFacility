RIF.table['subscriber-ageGroups'] = (function(  controller ) {

   var subscriber = {
         startInvestigationParameter: function( numerator ){ 
            controller.getAgeGroups(numerator);
        },
       
      clearAllParameters: function() {
           $('.rowSelected').removeClass('rowSelected');
      },
         
   };
    
   return subscriber;
});