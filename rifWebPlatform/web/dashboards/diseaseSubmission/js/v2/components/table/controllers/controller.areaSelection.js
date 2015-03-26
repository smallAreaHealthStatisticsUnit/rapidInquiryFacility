RIF.table['controller-areaSelection'] = (function( unit ) {
   
   var _p = {
       getTabularData: function(geolvl) {
            var callback = function(){
                //var start = new Date().getTime();
                /*if (typeof this[0]['errorMessages'] != 'undefined') {
                   RIF.statusBar(this[0]['errorMessages'], 1, 'notify');
                   return;
                };*/
                unit.getTabularData(this);
                //parent.setEvents(['areaSelectionRenderer']);
                
                /*var end = new Date().getTime();
                var time = end - start;*/
            };
                      
            RIF.getGeolevelSelect(callback, [geolvl]);
           
        }
   };
    
    
    return _p;
});