RIF.menu = ( function ( settings ) {

  var menus = settings.menus,
    _observable = {},
    _p = {

      menuUtils: RIF.menu.utils(),

      initialize: function () {
        var l = menus.length
        while ( l-- ) {
          _p.initializeUnit( menus[ l ] );
        };
        return _p;
      },

      initializeUnit: function ( name ) {
        var dom = _p.getDom( name ),
          unit = _p.getUnit( dom, name ),
          controller = _p.getController( unit, name ),
          firer = _p.getFirer( name ),
          subscriber = _p.getSubscriber( controller, name );

        _p.setEvent( _observable, dom, name );
      },

      localExtend: function ( obj ) {
        for ( var i in obj ) {
          if ( typeof _observable[ i ] == 'undefined' ) {
            _observable[ i ] = obj[ i ];
          } else {
            var copy = _observable[ i ],
              copy2 = obj[ i ];
            _observable[ i ] = function ( args ) {
              copy2( args );
              copy( args );
            };
          };
        }
      },

      setUser: function ( userName ) {
        localStorage.setItem( 'RIF_user', userName );
      },

      getUnit: function ( dom, name ) {
        var unit = RIF.utils.getUnit( 'menu', name, dom, this.menuUtils );
        return unit;
      },

      getFirer: function ( unitName ) {
        var firer = RIF.utils.getFirer( 'menu', unitName );
        _p.localExtend( firer );
        return firer;
      },

      getSubscriber: function ( controller, unitName ) {
        var sub = RIF.utils.getSubscriber( 'menu', unitName, controller );
        _p.localExtend( sub );
        return sub;
      },

      getController: function ( unit, unitName ) {
        return RIF.utils.getController( 'menu', unit, unitName );
      },
      getDom: function ( unit ) {
        return RIF.dom[ 'menu' ][ unit ]();
      },

      setEvent: function ( firer, dom, unitName ) {
        RIF.utils.setMenuEvent( firer, dom, unitName, this.menuUtils );
      }
    };


  _p.initialize();

  return _observable;
} );