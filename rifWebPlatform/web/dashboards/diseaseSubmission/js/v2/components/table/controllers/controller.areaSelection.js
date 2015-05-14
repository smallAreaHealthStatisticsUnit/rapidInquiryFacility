RIF.table['controller-areaSelection'] = (function(unit) {

   var _p = {
      getTabularData: function(geolvl) {
         var callback = function() {
            unit.getTabularData(this);
         };

         RIF.getGeolevelSelect(callback, [geolvl]);

      }
   };


   return _p;
});