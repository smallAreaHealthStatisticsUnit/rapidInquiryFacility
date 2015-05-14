RIF.menu['firer-frontSubmission'] = (function() {

   var firer = {

      studyNameChanged: function(arg) {
         this.fire('studyNameChanged', arg);
      },
      healthThemeChanged: function(arg) {
         this.fire('healthThemeChanged', arg);
      },
      numeratorChanged: function(arg) {
         this.fire('numeratorChanged', arg);
      },
      denominatorChanged: function(arg) {
         this.fire('denominatorChanged', arg);
      },

      isDialogReady: function(dialog) {
         this.fire('isDialogReady', dialog);
      },

      isDialogSelectionComplete: function(dialog) {
         this.fire('isDialogSelectionComplete', dialog);
      },

   };
   return firer;
});