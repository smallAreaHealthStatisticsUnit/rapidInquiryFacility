/* params = {classification: , colorScale: , field: , intervals:, domain , colors  }  */
RIF.style.scales = ( function( params ) {

  var type = params[ "classification" ],
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

        return q;
      },

      jenks: function() {
        var jenks = ss.jenks( values, intervals );
        /* remove extreme intervals */
        var l = jenks.length;
        var j = d3.scale.threshold()
          .domain( jenks )
          .range( d3.range( intervals + 2 ).map( function( i ) {
            return i - 1
          } ) );

        return j;
      },

      threshold: function() {
        var t = d3.scale.threshold()
          .domain( domain ) //[.02, .04, .06, .08, .10]
          .range( d3.range( intervals + 1 ).map( function( i ) {
            return i
          } ) );

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

        breaks.push( min_value );
        breaks.push( max_value );

        for ( i = 0; below_mean > min_value && i < 2; i++ ) {
          breaks.push( below_mean );
          below_mean = below_mean - sd;
        }

        for ( i = 0; above_mean < max_value && breaks.length < 6; i++ ) {
          breaks.push( above_mean );
          above_mean = above_mean + sd;
        }

        breaks.sort( d3.ascending );

        var j = d3.scale.threshold()
          .domain( breaks )
          .range( d3.range( intervals + 2 ).map( function( i ) {
            return i - 1
          } ) );

        return j;
      },


      logarithmic: function() {}
    };

  return scales[ type ]();

} );