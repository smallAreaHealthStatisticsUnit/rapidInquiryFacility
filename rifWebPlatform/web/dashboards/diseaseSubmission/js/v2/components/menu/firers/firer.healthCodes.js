RIF.menu['firer-healthCodes'] = (function() {

   var firer = {
       healthSelectionChanged: function( healthSelection ){
          this.fire('healthSelectionChanged', healthSelection);
       }

   };
    
   return firer;
});