RIF.table['subscriber-areaSelection'] = (function(  controller ) {

   var subscriber = {
        selectAtChanged: function(geolvl){
           controller.getTabularData(geolvl);
        }    
   };
    
   return subscriber;
});