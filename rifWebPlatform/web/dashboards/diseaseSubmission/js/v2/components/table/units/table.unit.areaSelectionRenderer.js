RIF.table.areaSelectionRenderer = ( function( _dom ) {

  var parent = this,

    _requests = {
      getTabularData: function() {
        RIF.getGeolevelSelect( _callbacks[ 'getTabularData' ], null );
      }

    },

    _callbacks = {
      getTabularData: function() {
        //var start = new Date().getTime();
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
      var count = 0;
      for ( var i in data ) {
        var oddOreven = ( ++count % 2 == 0 ) ? 'even' : 'odd',
          div = document.createElement( "div" );

        div.className = 'aSR ' + oddOreven;
        div.id = count;
        div.innerHTML = '<div>' + data[ i ].identifier + '</div><div>' + data[ i ].label + '</div>';
        fragment.appendChild( div );
      }

      _dom.tableContent.appendChild( fragment );
      _dom.tableContent.style.display = 'block';
    },

    _p = {

      initAreaSelectionRenderer: function() {
        _requests.getTabularData();
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