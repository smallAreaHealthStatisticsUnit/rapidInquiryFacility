RIF.menu = ( function( settings ) {

  var menus = settings.menus,

    _p = {

      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      proxy: {

        frontMappingready: function() {
          _p.studyArea.request( 'getSelectAtsAvailable', RIF.user );
        },

        selectAtChange: function( selectAt ) {
          _p.studyArea.request( 'getResolutionsAvailable', [ RIF.user, selectAt ] );
          _p.facade.selectAtChanged( selectAt );
          _p.facade.resolutionChanged( null );
        },

        studyAreaReady: function() {
          _p.healthCodes.request( 'getTaxonomy' );
        },

        updateTopLevelHealthCodes: function( icd ) {
          _p.healthCodes.request( 'getTopLevelHealthCodes', icd );
        }

      },

      getFacade: function() {
        this.facade = RIF.getFacade( 'menu', settings.studyType, this );
        return this;
      },

      extendMenu: function() {
        _p = this.extendMenuComponent( _p, menus );
        return _p;
      },

      setEvents: function() {
        var ev = this.setMenuEvent( _p, menus );
      }


    };



  _p.init()
    .getFacade()
    .extendMenu()
    .setEvents();


  return _p.facade;

} );