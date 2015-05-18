RIF.menu['firer-areaSelection'] = (function() {

   var firer = {

      selectAtChanged: function(arg) {
         this.fire('selectAtChanged', arg);
         this.getResolutions(arg);
      },

      resolutionChanged: function(arg) {
         this.fire('resolutionChanged', arg);
      },

      syncStudyArea: function() {
         this.fire('syncStudyArea', []);
      }

   };
   return firer;
});