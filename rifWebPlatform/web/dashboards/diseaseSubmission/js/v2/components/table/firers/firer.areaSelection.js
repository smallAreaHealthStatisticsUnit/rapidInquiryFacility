RIF.table['firer-areaSelection'] = (function() {

   var firer = {
      studyAreaSelectionEvent: function(rowId) {
         this.fire('studyAreaSelectionEvent', rowId);
      },
   };
   return firer;
});