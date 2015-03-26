( function () {
  var statusBarMsgs = {},
    utils = {
      initComponents: function () { /* context passed by calling object */
        for ( var c in this.components ) {
          this[ c ] = RIF[ c ]( this.components[ c ] );
          RIF.makePublisher( this[ c ] );
        }
      },
      addEvents: function () { /* context passed by calling object */
        var evts = this.events;
        for ( var c in evts ) {
          var f = evts[ c ].firer,
            fl = f.length;
          while ( fl-- ) {
            var s = evts[ c ].subscribers,
              sl = s.length;
            while ( sl-- ) {
              this[ f[ fl ] ].on( c, evts[ c ].method, this[ s[ sl ] ] );
            }
          }
        }
      },
      makePublisher: function ( o ) {
        var i;
        for ( i in RIF.publisher ) {
          if ( RIF.publisher.hasOwnProperty( i ) && typeof RIF.publisher[ i ] === "function" ) {
            o[ i ] = RIF.publisher[ i ];
          }
        }
        o.subscribers = {
          any: []
        };
      },
      extend: function ( parent, child ) {
        var i,
          toStr = Object.prototype.toString,
          astr = "[object Array]";
        child = child || {};
        for ( i in parent ) {
          if ( parent.hasOwnProperty( i ) ) {
            if ( typeof parent[ i ] === 'object' ) {
              child[ i ] = ( toStr.call( parent[ i ] ) === astr ) ? [] : {};
              this.extend( parent[ i ], child[ i ] );
            } else {
              child[ i ] = parent[ i ];
            }
          }
        }
        return child;
      },
      redirect: function ( url ) {
        window.location.href = url;
      },
      getRedirectFromURL: function () {
        var r = this.getURLParameter( "rd" );
        return r == 'logIn' ? r : r == 'diseaseSubmission' ? r : r == 'diseaseMapping' ? r : 'diseaseSubmission';
      },
      getURLParameter: function ( name ) {
        return decodeURIComponent( ( new RegExp( '[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)' ).exec( location.search ) || [ , "" ] )[ 1 ].replace( /\+/g, '%20' ) ) || null
      },
      removeG: function ( id ) {
        return id.replace( "g", '' );
      },
      addG: function ( idN ) {
        return "g" + idN;;
      },
      object: function ( o ) {
        function F() {};
        F.prototype = o;
        return new F();
      },
      mix: function () {
        var arg, prop, child = {};
        for ( arg = 0; arg < arguments.length; arg += 1 ) {
          for ( prop in arguments[ arg ] ) {
            if ( arguments[ arg ].hasOwnProperty( prop ) ) {
              child[ prop ] = arguments[ arg ][ prop ];
            }
          }
        }
        return child;
      },
      unique: function ( array ) {
        var a = array.concat();
        for ( var i = 0; i < a.length; ++i ) {
          for ( var j = i + 1; j < a.length; ++j ) {
            if ( a[ i ] === a[ j ] ) a.splice( j--, 1 );
          }
        }
        return a;
      },
      difference: function arr_diff( a1, a2 ) {
        var a = [],
          diff = [];
        for ( var i = 0; i < a1.length; i++ ) a[ a1[ i ] ] = true;
        for ( var i = 0; i < a2.length; i++ )
          if ( a[ a2[ i ] ] ) delete a[ a2[ i ] ];
          else a[ a2[ i ] ] = true;
        for ( var k in a ) diff.push( k );
        return diff;
      },
      replaceAll: function ( find, replace, str ) {
        return str.replace( new RegExp( find, 'g' ), replace );
      },
      splitId: function ( id ) {
        var d = id.split( '__' );
        return [ d[ 0 ], d[ 1 ] ];
      },
      isArray: function ( obj ) {
        return Object.prototype.toString.call( obj ) === "[object Array]";
      },
      arraysEqual: function ( arr1, arr2 ) {
        return $( arr1 ).not( arr2 ).length == 0 && $( arr2 ).not( arr1 ).length == 0;
      },
      getFacade: function ( componentName, studyType, componentContext ) {
        var facadeName = [ 'facade', studyType ].join( '-' );
        return RIF[ componentName ][ facadeName ]( componentContext );
      },
      getEvent: function ( componentName, studyType ) {
        var eventName = [ componentName, 'event', studyType ].join( '-' );
        return RIF[ componentName ][ eventName ];
      },
      extendComponent: function ( componentName, component, units ) {
        var l = units.length;
        while ( l-- ) {
          var r = RIF[ componentName ][ units[ l ] ].call( component );
          component = RIF.mix( r, component );
        };
        return component;
      }
    };
  utils.extend( utils, RIF );
}() );