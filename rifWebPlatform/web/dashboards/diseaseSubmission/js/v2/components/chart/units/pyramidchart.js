RIF.chart.pyramid = (function() {
   var settings = {
         field: "SMR",
         element: "chart",
         margin: {
            top: 0,
            right: 20,
            bottom: 30,
            left: 18
         },
         dimensions: {
            width: $('.chart').width(),
            height: $('.chart').height()
         },
      },
      _render = function() {},
      _p = {
         setSettings: function(sett) {
            _p.lineSettings = RIF.extend(settings, sett);
         }
      };
   return _p;
});