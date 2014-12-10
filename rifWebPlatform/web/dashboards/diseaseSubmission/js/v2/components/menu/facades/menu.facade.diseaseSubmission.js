RIF.menu[ 'facade-diseaseSubmission' ] = ( function( _p ) {

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

    studyNameChanged: function( arg ) {
      this.fire( 'studyNameChanged', arg );
    },
    healthThemeChanged: function( arg ) {
      this.fire( 'healthThemeChanged', arg );
    },
    numeratorChanged: function( arg ) {
      this.fire( 'numeratorChanged', arg );
    },
    denominatorChanged: function( arg ) {
      this.fire( 'denominatorChanged', arg );
    },
    selectAtChanged: function( arg ) {
      this.fire( 'selectAtChanged', arg );
    },
    resolutionChanged: function( arg ) {
      this.fire( 'resolutionChanged', arg );
    },


    /* Study Related */


  };

  return facade;


} );