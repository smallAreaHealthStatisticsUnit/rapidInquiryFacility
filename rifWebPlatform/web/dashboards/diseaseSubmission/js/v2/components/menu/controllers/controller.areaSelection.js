RIF.menu['controller-areaSelection'] = (function( unit ) {
   
   var _p = {
         
        getSelectAtsAvailable: function() {
            var callback = function(){
               var selectAts = this[0].names;
               unit.getSelectAt(selectAts);
            };
            
            RIF.getSelectAt(callback, null);
         },
       
         getResolutionsAvailable: function(params) {
            var callback = function(){
                var selectAts = this[0].names;
                unit.getResolutions(selectAts); 
            };
             
            RIF.getResolutions(callback, [params[0], params[1]]);
         }
       
    };
    
    _p.getSelectAtsAvailable();
    
    return _p;
});