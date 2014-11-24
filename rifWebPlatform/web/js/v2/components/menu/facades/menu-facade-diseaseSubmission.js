RIF.menu[ 'menu-facade-diseaseSubmission' ] = ( function( _p ) {

  /*
   * Facades can only communicate to main component.
   * Cannot call of directly component sub units
   */

  var facade = {

    /* Subscribers */
    updateHealthThemeAvailables: function() {},
    updateNumeratorAvailables: function() {},
    updateDenominatorAvailables: function() {},


    /* Firers  */
    healthThemeChanged: function() {},
    numeratorChanged: function() {},
    denominatorChanged: function() {},


    /* Study Related */


  };

  return facade;


} );