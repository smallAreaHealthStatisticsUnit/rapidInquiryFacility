RIF.menu.transparency = ( function() {

  var parent = this,

    _domObjects = {
      /* DOM elements */
      transpContainer: $( '#transparencyContainer' ),
      transpSlider: $( '#transparencyContainer input' ),
    },

    /* events */
    _events = function() {

      _domObjects.transpSlider.change( function() {
        var transparency = $( this ).val();
        parent.facade.transparencyChanged( transparency );
      } );
    },

    /* baseMap obj */
    _p = {

      initBaseMap: function() {
        _events();
      },

      getBaseTransparencyMenuDom: function( obj ) {
        return _domObjects[ obj ];
      }

    };

  _p.initBaseMap();

  return _p;
} );