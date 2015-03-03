/*
 *
 * RIF 4.0
 * Created by Federico Fabbri
 * Imperial College London
 *
 *
 */
var RIF = (function(R) {

   R.version = "4.0";
   R.components = {};
   R.modules = {};
   R.resizeWidth = function( /*obj,*/ px) {};
   R.user = 'ffabbri';

   if (detectBrowser.browser === "Explorer" && parseInt(detectBrowser.version) < 9) {
      window.top.location = ""; //LANDING PAGE
   };

   if (detectBrowser.browser === "Explorer" && parseInt(detectBrowser.version) == 9) {
      $(document).ready(function() {
         $('input').placeholder();
      });
   };



   return R;

}(RIF || {}));