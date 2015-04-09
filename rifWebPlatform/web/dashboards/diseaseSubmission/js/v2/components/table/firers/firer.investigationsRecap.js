RIF.table[ 'firer-investigationsRecap' ] = ( function () {

  var firer = {
    removeInvestigationRow: function ( invId ) {
      this.fire( 'removeInvestigationRow', invId );
    }
  };
  return firer;
} );