RIF.map = (function (settings, publisher) {

  var maps = settings.maps,
    _observable = {},

    _p = {

      initialize: function () {
        var l = maps.length
        while (l--) {
          _p.initializeUnit(maps[l]);
        };
        return _p;
      },

      initializeUnit: function (name) {
        var dom = _p.getDom(name),
          unit = _p.getUnit(dom, name),
          controller = _p.getController(unit, name),
          firer = _p.getFirer(name),
          subscriber = _p.getSubscriber(controller, name);

        _p.setEvent(_observable, dom, name);
      },

      localExtend: function (obj) {
        for (var i in obj) {
          if (typeof _observable[i] == 'undefined') {
            _observable[i] = obj[i];
          } else {
            var copy = _observable[i],
              copy2 = obj[i];
            _observable[i] = function (args) {
              copy2(args);
              copy(args);
            };
          };
        }
      },

      getUnit: function (dom, name) {
        var unit = RIF.utils.getUnit('map', name, dom, this.menuUtils);
        return unit;
      },

      getFirer: function (unitName) {
        var firer = RIF.utils.getFirer('map', unitName);
        _p.localExtend(firer);
        return firer;
      },

      getSubscriber: function (controller, unitName) {
        var sub = RIF.utils.getSubscriber('map', unitName, controller);
        _p.localExtend(sub);
        return sub;
      },

      getController: function (unit, unitName) {
        return RIF.utils.getController('map', unit, unitName);
      },

      getDom: function (unit) {
        return RIF.dom['map'][unit]();
      },

      setEvent: function (firer, dom, unitName) {
        RIF.utils.setMapEvent(firer, dom, unitName);
      }

    };


  _p.initialize();

  return _observable;

});