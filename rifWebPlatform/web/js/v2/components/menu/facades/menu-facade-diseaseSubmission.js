RIF.menu[ 'menu-facade-diseaseSubmission' ] = ( function( _p ) {

  /*
   * Facades can only communicate to main component.
   * Cannot call directly component sub units
   */

  var facade = {

    /* Subscribers */
    updateHealthThemeAvailables: function() {},
    updateNumeratorAvailables: function() {},
    updateDenominatorAvailables: function() {},


    /* Firers  */
    healthThemeChanged: function( args ) {},
    numeratorChanged: function( args ) {},
    denominatorChanged: function( args ) {},
    resolutionChanged: function( args ) {},
    selectAtChanged: function( args ) {},

    /* Study Related */


  };

  return facade;


} );