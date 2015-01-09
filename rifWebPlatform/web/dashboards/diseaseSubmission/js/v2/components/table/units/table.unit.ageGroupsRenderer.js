RIF.table.ageGroupsRenderer = ( function( _dom ) {

  var parent = this,

    _requests = {
      getAgeGroups: function( /*Numearator*/) {
        RIF.getAgeGroups( _callbacks[ 'getAgeGroups' ] /*,  Numerator table */ );
      }

    },

    _callbacks = {
      getAgeGroups: function() {
        //var start = new Date().getTime();
        if ( typeof this[ 0 ][ 'errorMessages' ] != 'undefined' ) {
          RIF.statusBar( this[ 0 ][ 'errorMessages' ], 1, 'notify' );
          return;
        };

        _renderTable( this );
        _dom.rows.change();
        parent.setEvents( [ 'ageGroupsRenderer' ] );
        /*var end = new Date().getTime();
            var time = end - start;*/
      }
    },

    _renderTable = function( data ) {
      _dom.tableContent.style.display = 'none';
      $( _dom.tableContent ).empty();
      var fragment = document.createDocumentFragment();
      names = data[ 0 ].name,
      lower = data[ 1 ].lowerAgeLimit,
      upper = data[ 2 ].upperAgeLimit,
      l = names.length;
      while ( l-- ) {
        var oddOreven = ( l % 2 == 0 ) ? 'even' : 'odd',
          div = document.createElement( "div" );

        div.className = 'aSR ' + oddOreven;
        div.id = 'ageGroup' + l;
        div.innerHTML = '<div>' + names[ l ] + '</div><div>' + lower[ l ] + ' - ' + upper[ l ] + '</div>';
        fragment.appendChild( div );
      }
      _dom.tableContent.appendChild( fragment );
      _dom.tableContent.style.display = 'block';
    },

    _p = {

      request: function( reqName, params ) {
        _requests[ reqName ]( params );
      },

    };

  _p.request( 'getAgeGroups' );

  return {
    ageGroupsRenderer: _p
  };
} );