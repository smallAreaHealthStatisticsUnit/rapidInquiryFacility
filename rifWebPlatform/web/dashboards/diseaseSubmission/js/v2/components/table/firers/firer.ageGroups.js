RIF.table['firer-ageGroups'] = (function() {

   var firer = {
      ageGroupsChanged: function(ageGroups) {
         this.fire('ageGroupsChanged', ageGroups);
         this.fire('isInvestigationReady', null);
      }
   };
   return firer;
});