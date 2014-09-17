RIF.menu.choropleth = ( function() {

  var parent = this,

    /* choropleth obj */
    _p = {

      initChoropleth: function() {
        this.events();
        this.makeColourScales();
      },

      /* DOM elements */
      menu: $( "#choropleth" ),
      choroplethBtn: $( "#choroplethBtn" ),
      save: $( ".save-fld" ),
      fieldToMap: $( "#fieldToMap" ),
      classification: $( "#classification" ),
      scale: "lightGreen",
      intervals: $( "#intervals" ),
      edit: $( ".editBreaks a" ),
      breaksEdited: false,

      selected: function() {
        return {
          field: _p.fieldToMap.find( ":selected" ).val(),
          colorScale: _p.scale,
          intervals: parseInt( _p.intervals.find( ":selected" ).text() ),
          classification: _p.getClassification(),
          domain: _p.getInputBreaks()
        };
      },

      makeColourScales: function() {
        d3.select( ".palette" )
          .selectAll( "div" )
          .data( d3.entries( RIF.colorbrewer ) )
          .enter().append( "div" )
          .attr( "class", function( d ) {
            return d.key;
          } )
          .selectAll( ".swatch" )
          .data( function( d ) {
            return d.value[ d3.keys( d.value ).map( Number ).sort( d3.descending )[ 0 ] ]
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
          _p.scale = this.className;
          var text = $( this ).html();
          $( ".dropdown dt div #selectedScale" ).html( text );
          $( ".dropdown .palette" ).hide();
        } );
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
        parent.dropDown( intervals, _p.intervals );
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
          return _p.classification.find( ":selected" ).val();
        }
      },

      /* events */
      events: function() {
        $( "#intervals,#classification" ).change( function() {
          _p.breaksEdited = false;
          $( "#breaks" ).hide();
        } );

        $( "#classification" ).change( function() {
          _p.updateIntervals( $( "#selectedScale" ).children().length );
        } );

        this.choroplethBtn.click( function() {
          $( "#choropleth" ).show();
        } );

        $( "dl.dropdown" ).click( function() {
          _p.updateIntervals( $( "#selectedScale" ).children().length );
        } );


        this.edit.click( function() {
          _p.breaksEdited = false;
          var slctd = _p.selected();
          if ( slctd.field !== "N/A" ) {
            parent.facade.fire( "editChoroplethBreaks", slctd );
          };
        } );

        this.save.click( function() {

          if ( $( "#classification" ).val() === 'standardDeviation' ) {
            _p.updateIntervals( 5 );
          };

          $( "#choropleth" ).hide();

          var slctd = _p.selected();

          if ( slctd.field !== "N/A" ) {
            parent.facade.fire( "mapStyleChange", slctd );
            parent.facade.hoverFieldChange( slctd.field );
          };

        } );
      }
    };

  _p.initChoropleth();

  return _p;
} );