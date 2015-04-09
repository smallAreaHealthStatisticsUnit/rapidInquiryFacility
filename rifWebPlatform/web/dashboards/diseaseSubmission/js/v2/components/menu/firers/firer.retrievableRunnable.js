RIF.menu[ 'firer-retrievableRunnable' ] = ( function () {

  var firer = {
    isStudyReady: function () {
      this.fire( "isStudyReady", null );
    }
  };

  return firer;
} );