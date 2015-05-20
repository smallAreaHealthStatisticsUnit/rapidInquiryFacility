RIF['map']['controller-studyArea'] = (function(unit) {

   var _p = {

      updateGeolevel: function(geolvl) {
         unit.updateGeolevel(geolvl);
      },

      setInitialExtent: function() {
         var callback = function() {
            unit.setExtent(this);
         };
         RIF.getFullExtent(callback, []); // Web service need to change to allow no geolevelselect
      }(),

   };

   return _p;

});