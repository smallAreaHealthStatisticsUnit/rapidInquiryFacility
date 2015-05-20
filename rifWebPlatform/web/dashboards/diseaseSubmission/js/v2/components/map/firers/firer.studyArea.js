RIF['map']['firer-studyArea'] = (function() {

   var firer = {

      propagateMapExtent: function(ext) {
         console.log(ext);
      },

      studyMapAreaSelectionEvent: function(areaId) {
         this.fire('studyMapAreaSelectionEvent', areaId);
      }

   };
   return firer;
});