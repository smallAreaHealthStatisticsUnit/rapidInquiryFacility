RIF.menu.settings = ( function( _dom ) {

  var parent = this,


    _requests = {

      getAllFieldsAvailable: function( investigation ) {
        RIF.getFields( _callbacks.getAllFieldsAvailable, [ /*investigation*/ "atlas_leu_f" ] );
      },

    },

    _callbacks = {

      getAllFieldsAvailable: function( investigation ) {
        parent.dropDown( this, _dom.hoverSlct );
      },

      resultSetsReady: function( resSets ) {
        parent.fieldCheckboxesResultsSet( resSets, _dom.resultsChoice, "resultsSets" );
      },

    },


    _p = {

      request: function( reqName, params ) {
        _requests[ reqName ]( params );
      },

      callback: function( clbkName, params ) {
        _callbacks[ clbkName ]( params );
      }

    };



  return {
    settings: _p
  };
} );