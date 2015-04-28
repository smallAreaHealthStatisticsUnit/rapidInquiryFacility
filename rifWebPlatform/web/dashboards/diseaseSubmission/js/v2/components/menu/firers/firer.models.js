RIF[ 'menu' ][ 'firer-models' ] = ( function () {
  var firer = {
    calculationMethodsChanged: function ( arg ) {
      this.fire( 'calculationMethodsChanged', arg );
    }

  };
  return firer;
} );