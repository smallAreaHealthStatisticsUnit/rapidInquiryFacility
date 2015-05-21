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

      syncStudyArea: function(selection) {
         unit.sync(selection);
      },

      clearSelection: function() {
         unit.clearAll();
      }

   };

   return _p;

});