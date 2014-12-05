RIF.menu.choropleth = ( function() {

  var parent = this,

    _requests = {},

    _callbacks = {},

    _p = {

      breaksEdited: false,
      scale: "lightGreen",


      initChoropleth: function() {
        this.makeColourScales();
      },

      selected: function( _dom ) {
        return {
          colorScale: _p.scale,
          intervals: parseInt( _dom.intervals.find( ":selected" ).text() ),
          classification: _p.getClassification( _dom.classification ),
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
      },

      revertRamps: function() {
        for ( var col in RIF.colorbrewer ) {
          for ( var ramp in RIF.colorbrewer[ col ] ) {
            RIF.colorbrewer[ col ][ ramp ] = RIF.colorbrewer[ col ][ ramp ].reverse();
          }
        }
      },

      updateIntervals: function( n, dom ) {
        if ( dom.selectedScale.children().length === 1 ) {
          n = 1;
        };

        var intervals = [],
          maxIntervals = ( n > 1 ) ? 3 : 1;
        for ( ; n >= maxIntervals; n-- ) {
          intervals.push( n );
        };
        parent.dropDown( intervals, dom.intervals );
      },

      updateStandardDeviation: function( domIntervals ) {
        domIntervals.empty();
        parent.dropDownFromArray( [ 5 ], domIntervals );
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

      getClassification: function( domClassification ) {
        if ( _p.breaksEdited ) {
          return "threshold";
        } else {
          return domClassification.find( ":selected" ).val();
        }
      }

    };

  _p.initChoropleth();


  return {
    choropleth: _p
  };
} );