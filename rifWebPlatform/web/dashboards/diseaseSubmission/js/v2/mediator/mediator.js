RIF.mediator = (function () {

  var _observable = {},
    modelAccessor = RIF.modelAccessor(),
    mediatorUtils = RIF.mediator.utils(modelAccessor);



  var _getFirer = function () {
      var firer = RIF.utils.getFirer('mediator', '');
      _localExtend(firer);
      return firer;
    },

    _getSubscriber = function () {
      var sub = RIF.utils.getSubscriber('mediator', '', mediatorUtils);
      _localExtend(sub);
      return sub;
    },

    _localExtend = function (obj) {
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
    };


  return RIF.utils.mix(_getFirer(), _getSubscriber());

});