RIF.table.areaSelectionRenderer = ( function( _dom ) {

  var parent = this,

    _requests = {
      getTabularData: function( geolvl ) {
        RIF.getGeolevelSelect( _callbacks[ 'getTabularData' ], [ geolvl ] );
      }

    },

    _callbacks = {
      getTabularData: function() {
        //var start = new Date().getTime();
        if ( typeof this[ 0 ][ 'errorMessages' ] != 'undefined' ) {
          RIF.statusBar( this[ 0 ][ 'errorMessages' ], 1, 'notify' );
          return;
        };

        _renderTable( this );
        _dom.rows.change();
        parent.setEvents( [ 'areaSelectionRenderer' ] );
        /*var end = new Date().getTime();
            var time = end - start;*/
      }
    },

    _renderTable = function( data ) {
      _dom.tableContent.style.display = 'none';
      var fragment = document.createDocumentFragment();
      ids = data[ 1 ].id,
      labels = data[ 2 ].label,
      l = ids.length;
      while ( l-- ) {
        var oddOreven = ( l % 2 == 0 ) ? 'even' : 'odd',
          div = document.createElement( "div" );

        div.className = 'aSR ' + oddOreven;
        div.id = l;
        div.innerHTML = '<div>' + ids[ l ] + '</div><div>' + labels[ l ] + '</div>';
        fragment.appendChild( div );
      }

      _dom.tableContent.appendChild( fragment );
      _dom.tableContent.style.display = 'block';
    },

    _p = {

      initAreaSelectionRenderer: function() {
        //_requests.getTabularData();
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