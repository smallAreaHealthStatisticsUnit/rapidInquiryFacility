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
        // Health Code tree

        icd: 'icd10', //default  

        updateTopLevelHealthCodes: function( icd ) {
          _p.healthCodes.request( 'getTopLevelHealthCodes', icd );
          _p.proxy.icd = icd;
        },

        updateSubLevelHealthCodes: function( code, domEl ) {
          _p.healthCodes.request( 'getSubLevelHealthCodes', {
            "taxonomy": _p.proxy.icd,
            "code": code,
            "dom": domEl
          } );
        },

        taxonomyChanged: function( args ) {
          _p.facade.taxonomyChanged( args );
        },

        updateEventsHealthTree: function() {
          _p.setEvents( [ 'healthCodes' ] );
        },

      },

      getFacade: function() {
        this.facade = RIF.getFacade( 'menu', settings.studyType, this );
        return this;
      },

      extendMenu: function() {
        _p = this.extendMenuComponent( _p, menus );
        return _p;
      },

      setEvents: function( menu ) {
        var m = menu || menus;
        var ev = this.setMenuEvent( _p, m );
      }


    };



  _p.init()
    .getFacade()
    .extendMenu()
    .setEvents();


  return _p.facade;

} );