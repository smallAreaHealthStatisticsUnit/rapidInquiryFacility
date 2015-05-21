RIF.table['controller-studyArea'] = (function(unit) {

   var _currentGeolvl;
   var _getBounds = function() {
      var bounds = {
         yMax: RIF.mapExtent['_northEast']['lat'],
         xMax: RIF.mapExtent['_northEast']['lng'],
         yMin: RIF.mapExtent['_southWest']['lat'],
         xMin: RIF.mapExtent['_southWest']['lng']
      };
      return bounds;
   };

   var _p = {

      getTabularData: function(geolvl) {
         if (typeof geolvl != 'undefined') {
            _currentGeolvl = geolvl;
         };
         var callback = function() {
            unit.empty();
            unit.getTabularData(this);
         };
         RIF.getTableMapAreas(callback, [_currentGeolvl, _getBounds()]);
      },

      syncTabularData: function(mapAreas) {
         var callback = function() {
            unit.empty();
            unit.getTabularDataFromMap(mapAreas);
            unit.getTabularData(this);
         };

         RIF.getTableMapAreas(callback, [_currentGeolvl, _getBounds()]);
      },

      studySelectAllRows: function() {
         unit.selectAll();
      },

      getSelection: function() {
         return unit.getSelection();
      },

      clearSelection() {
         unit.clearAll();
      }

   };


   return _p;
});