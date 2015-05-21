RIF.menu['firer-areaSelection'] = (function() {

   var firer = {

      selectAtChanged: function(arg) {
         this.fire('selectAtChanged', arg);
         this.getResolutions(arg);
      },

      resolutionChanged: function(arg) {
         this.fire('resolutionChanged', arg);
      },

      syncTableButtonClicked: function() {
         this.fire('syncTableButtonClicked', []);
      },

      syncMapButtonClicked: function() {
         this.fire('syncMapButtonClicked', []);
      },

      studyMapAreaSelectionEvent: function(areaId) {
         this.fire('studyMapAreaSelectionEvent', RIF.mapAreasSelected);
      },

      studySelectAllRowsClicked: function() {
         this.fire('studySelectAllRowsClicked', []);
      },

      clearAreaSelectionEvent: function() {
         this.fire('clearAreaSelectionEvent', ['studyArea']);
      },

   };
   return firer;
});