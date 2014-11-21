RIF.menu = ( function( settings ) {

  var menus = settings.menus,

    _p = {
      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      callbacks: {
    
          
      },
        
      populate: function( args ) {
          _p.dropDownInputText( ['sdfsdf','vvvvvvvvvv'], _p.getHealthThemesListElement() )
      },


      extendMenu: function() {
        _p = RIF.extendComponent( 'menu', _p, menus );
        return _p;
      },

      getFacade: function() {
        this.facade = RIF.getFacade( 'menu', settings.studyType, this );
        return this;
      },
        
      setEvents: function() {
        var ev = RIF.getEvent( 'menu', settings.studyType );
        ev.call( this );
      }

    };

  _p.init()
    .getFacade()
    .extendMenu()
    .setEvents();

  
  _p.populate();    
  return _p.facade;
        
} );