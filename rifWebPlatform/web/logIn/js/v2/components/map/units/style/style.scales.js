/* params = {classification: , colorScale: , field: , intervals:, domain , colors  }  */
RIF.style.scales = ( function( params ) {

  var style = this,
    type = params[ "classification" ],
    min_value = params.min,
    max_value = params.max,
    intervals = parseInt( params[ "intervals" ] ),
    values = params[ "values" ],
    domain = params[ "domain" ],
    colors = params[ "colors" ],

    scales = {

      quantize: function() {
        var q = d3.scale.quantize()
          .domain( [ min_value, max_value ] )
          .range( d3.range( intervals ) );

        return q;
      },

      quantile: function() {
        var q = d3.scale.quantile()
          .domain( [ min_value, max_value ] )
          .range( d3.range( intervals ) );

        setBreaks( q );

        return q;
      },

      jenks: function() {
        var jenks = ss.jenks( values, intervals );

        setBreaks( jenks );

        /* remove extreme intervals */
        jenks.pop();
        jenks.shift();

        var l = jenks.length;
        var j = d3.scale.threshold()
          .domain( jenks )
          .range( d3.range( intervals ).map( function( i ) {
            return i
          } ) );

        return j;
      },

      threshold: function() {
        domain.pop();
        domain.shift();
        domain = domain.sort( d3.ascending );

        var t = d3.scale.threshold()
          .domain( domain ) //[.02, .04, .06, .08, .10]
          .range( d3.range( intervals + 1 ).map( function( i ) {
            return i
          } ) );

        setBreaks( t, 'yesMin' );
        return t;
      },

      standardDeviation: function() {
        /*
         * Implementation derived by ArcMap Stand. Deviation classification
         * 5 intervals of which those around the mean are 1/2 the Standard Deviation
         */
        var sd = ss.sample_standard_deviation( values ),
          mean = d3.mean( values ),
          below_mean = mean - sd / 2,
          above_mean = mean + sd / 2,
          breaks = [];

        for ( i = 0; below_mean > min_value && i < 2; i++ ) {
          breaks.push( below_mean );
          below_mean = below_mean - sd;
        }

        for ( i = 0; above_mean < max_value && i < 2; i++ ) {
          breaks.push( above_mean );
          above_mean = above_mean + sd;
        }

        breaks.sort( d3.ascending );

        var j = d3.scale.threshold()
          .domain( breaks )
          .range( d3.range( intervals ).map( function( i ) {
            return i
          } ) );

        setBreaks( j, 'yesMin' );

        return j;
      },


      logarithmic: function() {}
    };

  function setBreaks( breaks, type ) {

    style.breaks = [];

    if ( typeof breaks === 'object' ) {
      style.breaks.push( d3.format( ".2f" )( max_value ) );
      for ( var i = 0; i < intervals; i++ ) {
        if ( typeof breaks[ i ] !== 'undefined' ) {
          style.breaks[ i ] = d3.format( ".2f" )( breaks[ i ] );
        }
      };
      style.breaks.reverse();

    } else if ( typeof breaks === 'function' ) {
      style.breaks.push( d3.format( ".2f" )( max_value ) );
      while ( intervals-- ) {
        var r = breaks.invertExtent( intervals );
        if ( typeof r[ 0 ] !== 'undefined' ) {
          style.breaks.push( d3.format( ".2f" )( r[ 0 ] ) );
        };
      }
    };

    if ( type === 'yesMin' ) {
      style.breaks.push( d3.format( ".2f" )( min_value ) );
    };



  };

  return scales[ type ]();

} );