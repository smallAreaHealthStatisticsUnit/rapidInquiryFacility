RIF.table = (function(settings) {
   var tables = settings.tables,
       _observable = {},
   _p = {/*
      init: function() {
         _p = RIF.mix(RIF.table.utils(), _p);
         return _p;
      },
      proxy: {
         updateStudyGrid: function(geoLvl) {
            _p.areaSelectionRenderer.request('getTabularData', geoLvl);
         },
         addInvestigationRow: function(inv) {
            _p.investigationsRecap.addRow(inv);
         },
         removeInvestigationRow: function(inv) {
            _p.facade.removeInvestigationRow(inv);
         },
         getAgeGroups: function(numerator) {
            _p.ageGroupsRenderer.request('getAgeGroups', numerator);
         }
      },
      getFacade: function() {
         this.facade = RIF.getFacade('table', settings.studyType, this);
         return this;
      },
      extendTable: function() {
         _p = this.extendTableComponent(_p, tables);
         return _p;
      },
      setEvents: function(table) {
         var units = table || tables;
         var ev = this.setTableEvent(_p, tables);
      },
      specialEvent: function() {
         $(document).mouseup(function() {
            _p.isMouseDown = false;
         });
         return _p;
      },
      isMouseDown: false*/
      
       tableUtils: RIF.table.utils(),
          
       initialize: function() {
            var l = tables.length
            while (l--) { 
                _p.initializeUnit(tables[l]);
            };
            return _p;
        },  
            
        initializeUnit: function( name ){ 
            var dom = _p.getDom(name)
              , unit = _p.getUnit(dom, name)
              , controller = _p.getController( unit, name)
              , firer = _p.getFirer(name)
              , subscriber = _p.getSubscriber( controller, name);
            
            _p.setEvent( _observable, dom, name);
        },  
        
        localExtend: function(obj){
            for( var i in obj){
                if(typeof _observable[i] == 'undefined'){
                   _observable[i] = obj[i]; 
                }else {  
                   var copy = _observable[i],
                       copy2 = obj[i];   
                   _observable[i] = function(args){    
                       copy2(args);   
                       copy(args);
                   };
                };
            }    
        },  
        
         getUnit: function(dom,name){
            var unit = RIF.utils.getUnit('table',name,dom, this.menuUtils);
            return unit;
         },
          
         getFirer: function( unitName) {
            var firer = RIF.utils.getFirer('table',  unitName); 
             _p.localExtend(firer);
            return firer; 
         },
          
         getSubscriber: function(controller, unitName) {
            var sub = RIF.utils.getSubscriber('table', unitName, controller); 
            _p.localExtend(sub);
            return sub; 
         },
          
         getController: function(unit, unitName) {
            return RIF.utils.getController('table', unit, unitName);
         },
         getDom: function(unit){
            return RIF.dom['table'][unit]();
         },  
          
         setEvent: function(firer, dom , unitName) {
            RIF.utils.setTableEvent(firer, dom, unitName);
         }, 
       
         specialEvent: function() {
            $(document).mouseup(function() {
               _p.isMouseDown = false;
            });
            return _p;
         },
         isMouseDown: false
       
   };
             
   _p.initialize();
    
   return _observable;
});