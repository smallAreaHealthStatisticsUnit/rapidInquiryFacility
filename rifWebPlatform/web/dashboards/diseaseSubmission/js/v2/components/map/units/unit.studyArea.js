RIF['map']['unit-studyArea'] = (function(_dom, menuUtils) {

   /*
    * Pseudo-classical inheritance (through prototyping): similar to Object.create
    *
    * RIF.SVGlayer.prototype = new RIF.Map();
    * var layer = new RIF.SVGlayer();
    *
    */

   var mapLayer = new RIF.Layer(_dom.id, _dom.tooltip);

   var _p = {
      updateGeolevel: function(geolvl) {
         mapLayer.initLayer(geolvl);
      },

      setExtent: function(ext) {
         mapLayer.setInitialExtent(ext);
      }
   };

   return _p;

});