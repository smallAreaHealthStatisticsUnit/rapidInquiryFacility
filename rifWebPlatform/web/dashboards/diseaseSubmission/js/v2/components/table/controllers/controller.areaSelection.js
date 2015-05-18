RIF.table['controller-areaSelection'] = (function(unit) {

   var _currentGeolvl;

   var _p = {

      getTabularData: function(geolvl) {

         if (typeof geolvl != 'undefined') {
            _currentGeolvl = geolvl;
         };

         var callback = function() {
            unit.getTabularData(this);
         };

         var bounds = {
            yMax: RIF.mapExtent['_northEast']['lat'],
            xMax: RIF.mapExtent['_northEast']['lng'],
            yMin: RIF.mapExtent['_southWest']['lat'],
            xMin: RIF.mapExtent['_southWest']['lng']
         }

         RIF.getTableMapAreas(callback, [_currentGeolvl, bounds]);

      }
   };


   return _p;
});