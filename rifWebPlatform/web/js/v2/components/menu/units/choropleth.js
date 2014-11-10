RIF.menu.choropleth = ( function() {

  var parent = this,

    /* DOM elements */
    _domObjects = {
      menu: $( "#choropleth" ),
      choroplethBtn: $( "#choroplethBtn" ),
      save: $( ".save-fld" ),
      classification: $( "#classification" ),
      scale: "lightGreen",
      intervals: $( "#intervals" ),
      edit: $( ".editBreaks a" ),
      invert: $( "#invertColors" ),
    },

    /* events */
    _events = function() {
      $( "#intervals,#classification" ).change( function() {
        _p.breaksEdited = false;
        $( "#breaks" ).hide();
      } );

      _domObjects.classification.change( function() {
        if ( $( "#classification" ).val() === 'standardDeviation' ) {
          _p.updateStandardDeviation();
        };
        _p.updateIntervals( $( "#selectedScale" ).children().length );
      } );

      _domObjects.choroplethBtn.click( function() {
        $( "#choropleth" ).show();
      } );

      $( "dl.dropdown" ).click( function() {
        _p.updateIntervals( $( "#selectedScale" ).children().length );
      } );


      _domObjects.edit.click( function() {
        _p.breaksEdited = false;
        var slctd = _p.selected();
        if ( slctd.field !== "N/A" ) {
          parent.facade.fire( "editChoroplethBreaks", slctd );
        };
      } );

      _domObjects.invert.change( function() {
        $( '.palette' ).empty();
        _p.makeColourScales( "invert" );
        var selectionInv = $( "." + _domObjects.scale ).html()
        $( ".dropdown dt div #selectedScale" ).html( selectionInv );
      } );

      _domObjects.save.click( function() {
        if ( $( "#classification" ).val() === 'standardDeviation' ) {
          _p.updateStandardDeviation();
        };

        $( "#choropleth" ).hide();

        var slctd = _p.selected();

        if ( slctd.field !== "N/A" ) {
          parent.facade.fire( "mapStyleChange", slctd );
          parent.facade.hoverFieldChange( slctd.field );
        };

      } );
    },

    /* choropleth obj */
    _p = {

      breaksEdited: false,

      initChoropleth: function() {
        _events();
        this.makeColourScales();
      },

      selected: function() {
        return {
          colorScale: _domObjects.scale,
          intervals: parseInt( _domObjects.intervals.find( ":selected" ).text() ),
          classification: _p.getClassification(),
          domain: _p.getInputBreaks()
        };
      },

      makeColourScales: function( invert ) {
        if ( invert ) {
          _p.revertRamps();
        };

        d3.select( ".palette" )
          .selectAll( "div" )
          .data( d3.entries( RIF.colorbrewer ) )
          .enter().append( "div" )
          .attr( "class", function( d ) {
            return d.key;
          } )
          .selectAll( ".swatch" )
          .data( function( d ) {
            var ramp = d.value[ d3.keys( d.value ).map( Number ).sort( d3.descending )[ 0 ] ];
            return ramp;
          } )
          .enter().append( "span" )
          .attr( "class", "swatch" )
          .style( "background-color", function( d ) {
            return d;
          } )
          .forEach( function( d ) {
            var l = d.length;
            d3.selectAll( d )
              .style( "width", function( d ) {
                return ( l === 1 ) ? "162px" :
                  ( l === 9 ) ? "18px" :
                  ( l === 12 ) ? "14px" :
                  ( l === 11 ) ? "15px" :
                  "21px";
              } )
          } )

        this.coloursScalesEvents();
      },

      coloursScalesEvents: function() {
        $( ".palette div" ).click( function() {
          _p.updateIntervals( $( this ).children().length );
          _domObjects.scale = this.className;
          var text = $( this ).html();
          $( ".dropdown dt div #selectedScale" ).html( text );
          $( ".dropdown .palette" ).hide();
        } );
      },

      revertRamps: function() {
        for ( var col in RIF.colorbrewer ) {
          for ( var ramp in RIF.colorbrewer[ col ] ) {
            RIF.colorbrewer[ col ][ ramp ] = RIF.colorbrewer[ col ][ ramp ].reverse();
          }
        }
      },

      updateIntervals: function( n ) {
        if ( $( "#selectedScale" ).children().length === 1 ) {
          n = 1;
        };

        var intervals = [],
          maxIntervals = ( n > 1 ) ? 3 : 1;
        for ( ; n >= maxIntervals; n-- ) {
          intervals.push( n );
        };
        parent.dropDown( intervals, _domObjects.intervals );
      },

      updateStandardDeviation: function() {
        _domObjects.intervals.empty();
        parent.dropDownFromArray( [ 5 ], _domObjects.intervals );
      },

      showScaleRange: function( scale ) {
        var l = scale.length,
          html = "";
        _p.breaksEdited = true;
        while ( l-- ) {
          html += "<input type = 'text' value=" + scale[ l ] + " ></input>";
        }
        $( "#breaks" ).empty().show().append( html );
      },

      getInputBreaks: function() {
        var domain = [];
        if ( _p.breaksEdited ) {
          d3.selectAll( "#breaks input" ).each( function() {
            domain.push( parseFloat( this.value ) );
          } );
        };
        domain.sort( d3.ascending );
        return domain;
      },

      getClassification: function() {
        if ( _p.breaksEdited ) {
          return "threshold";
        } else {
          return _domObjects.classification.find( ":selected" ).val();
        }
      },

      getChoroplethMenuDom: function( obj ) {
        return _domObjects[ obj ];
      }

    };

  _p.initChoropleth();

  return _p;
} );