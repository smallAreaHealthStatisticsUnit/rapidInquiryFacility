RIF.menu['firer-areaSelection'] = (function() {

   var firer = {

      selectAtChanged: function(arg) {
         this.fire('selectAtChanged', arg);
      },

   };
   return firer;
});