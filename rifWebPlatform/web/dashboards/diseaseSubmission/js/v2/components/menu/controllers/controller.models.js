RIF[ 'menu' ][ 'controller-models' ] = ( function ( unit ) {

  var _p = {
    startStatDialog: function () {
      var callback = function () {
        unit.populateModels( this );
      };
      RIF.getAvailableCalculationMethods( callback, null );
    }
  };

  return _p;
} );