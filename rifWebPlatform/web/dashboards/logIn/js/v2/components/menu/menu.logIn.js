RIF.menu = ( function( settings ) {

  var menus = settings.menus,

    _p = {

      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      proxy: {

        

      },


      setEvents: function() {
        var ev = this.setMenuEvent( _p, menus );
      },

      extendMenu: function() {
        _p = this.extendMenuComponent( _p, menus );
        return _p;
      },

      getFacade: function() {
        this.facade = RIF.getFacade( 'menu', settings.studyType, this );
        return this;
      }
    };


  _p.init()
    .getFacade()
    .extendMenu()
    .setEvents();


  return _p.facade;
} );