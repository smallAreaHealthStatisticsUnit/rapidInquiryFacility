RIF.table.areaSelectionRenderer = ( function( _dom ) {

  var parent = this,

    _requests = {


    },

    _callbacks = {



    },


    _p = {

      initAreaSelectionRenderer: function() {

      },


      request: function( reqName, params ) {
        _requests[ reqName ]( params );
      },

    };

  _p.initAreaSelectionRenderer();

  return {
    areaSelectionRenderer: _p
  };
} );