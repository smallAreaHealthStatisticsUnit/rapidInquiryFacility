RIF.table.utils = ( function() {

  var _p = {



    extendTableComponent: function( component, units ) {
      var l = units.length;
      while ( l-- ) {
        var r = RIF[ 'table' ][ units[ l ] ].call( component, RIF.dom[ 'table' ][ units[ l ] ]() );
        component = RIF.mix( r, component );
      };

      return component;
    },

    setTableEvent: function( component, units ) {
      var l = units.length;
      while ( l-- ) {
        var eventName = [ 'event', units[ l ] ].join( '-' );
        RIF[ 'table' ][ eventName ].call( component, RIF.dom[ 'table' ][ units[ l ] ]() );
      };
    },


  };

  return _p;
} );