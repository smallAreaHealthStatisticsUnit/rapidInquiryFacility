RIF.table = (function(settings) {
   var tables = settings.tables,
      _observable = {},
      _p = {

         tableUtils: RIF.table.utils(),

         initialize: function() {
            var l = tables.length
            while (l--) {
               _p.initializeUnit(tables[l]);
            };
            return _p;
         },

         initializeUnit: function(name) {
            var dom = _p.getDom(name),
               unit = _p.getUnit(dom, name),
               controller = _p.getController(unit, name),
               firer = _p.getFirer(name),
               subscriber = _p.getSubscriber(controller, name);

            _p.setEvent(_observable, dom, name);
         },

         localExtend: function(obj) {
            for (var i in obj) {
               if (typeof _observable[i] == 'undefined') {
                  _observable[i] = obj[i];
               } else {
                  var copy = _observable[i],
                     copy2 = obj[i];
                  _observable[i] = function(args) {
                     copy2(args);
                     copy(args);
                  };
               };
            }
         },

         getUnit: function(dom, name) {
            var unit = RIF.utils.getUnit('table', name, dom, this.menuUtils);
            return unit;
         },

         getFirer: function(unitName) {
            var firer = RIF.utils.getFirer('table', unitName);
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
         getDom: function(unit) {
            return RIF.dom['table'][unit]();
         },

         setEvent: function(firer, dom, unitName) {
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