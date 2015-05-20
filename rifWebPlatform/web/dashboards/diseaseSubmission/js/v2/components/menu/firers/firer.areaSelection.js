RIF.menu['firer-areaSelection'] = (function() {

   var firer = {

      selectAtChanged: function(arg) {
         this.fire('selectAtChanged', arg);
         this.getResolutions(arg);
      },

      resolutionChanged: function(arg) {
         this.fire('resolutionChanged', arg);
      },

      syncStudyAreaButtonClicked: function() {
         this.fire('syncStudyAreaButtonClicked', []);
      }

   };
   return firer;
});