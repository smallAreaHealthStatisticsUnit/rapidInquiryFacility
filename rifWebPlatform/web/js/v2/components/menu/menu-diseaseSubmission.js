RIF.menu = ( function( settings ) {

  var menus = settings.menus,

    _p = {
      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      callbacks: {},

      getFacade: function() {
        this.facade = RIF.getFacade( 'menu', settings.studyType, this );
        return this;
      },

      extendMenu: function() {
        _p = RIF.extendComponent( 'menu', _p, menus );
        return _p;
      },

      setEvents: function() {
        var ev = RIF.getEvent( 'menu', settings.studyType );
        ev.call( this );
      },

      populate: function() {

      }


    };

  _p.init()
    .getFacade()
    .extendMenu()
    .setEvents();


  return _p.facade;

} );