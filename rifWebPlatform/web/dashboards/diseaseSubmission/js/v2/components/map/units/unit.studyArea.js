RIF['map']['unit-studyArea'] = (function(_dom, menuUtils) {

   /*
    * Pseudo-classical inheritance (through prototyping): similar to Object.create
    *
    * RIF.SVGlayer.prototype = new RIF.Map();
    * var layer = new RIF.SVGlayer();
    *
    */

   var mapLayer = new RIF.Layer(_dom.id, _dom.tooltip, _dom.areaCounter);

   var _p = {

      updateCounter: function(n) {
         $(areaCounter).text(n);
      },

      updateGeolevel: function(geolvl) {
         mapLayer.initLayer(geolvl);
         _p.updateCounter(0);
      },

      setExtent: function(ext) {
         mapLayer.setInitialExtent(ext);
      },

      sync: function(selection) {
         mapLayer.clearSelection(_dom.id);
         mapLayer.selectAreas(selection);
         _p.updateCounter(selection.length);
      },

      clearAll: function() {
         mapLayer.clearSelection(_dom.id);
         _p.updateCounter(0);
      }
   };

   return _p;

});