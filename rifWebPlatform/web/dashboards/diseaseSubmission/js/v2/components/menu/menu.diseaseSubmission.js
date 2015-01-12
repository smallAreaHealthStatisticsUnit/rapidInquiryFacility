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
          _p.investigationParameters.request( 'getYears' );
        },
        // Health Code tree

        taxonomi: 'icd10', //default  

        updateTopLevelHealthCodes: function( taxonomi ) {
          _p.healthCodes.request( 'getTopLevelHealthCodes', taxonomi );
          _p.proxy.taxonomi = taxonomi;
          this.investigationParameterChange( taxonomi, _p.facade.taxonomyChanged );
        },

        updateSubLevelHealthCodes: function( code, domEl ) {
          _p.healthCodes.request( 'getSubLevelHealthCodes', {
            "taxonomy": _p.proxy.taxonomi,
            "code": code,
            "dom": domEl
          } )
        },

        investigationParameterChange: function( val, fnct ) {
          fnct.call( _p.facade, val );
          _p.facade.isInvestigationReady();
        },

        icdSelectionChanged: function( args ) {
          this.investigationParameterChange( args, _p.facade.icdSelectionChanged );
        },

        updateEventsHealthTree: function() {
          _p.setEvents( [ 'healthCodes' ] );
        },

        investigationReadyToBeAdded: function() {
          $( '#addInvestigation' ).addClass( 'addInvestigationActive' );
        },

        investigationNotReadyToBeAdded: function() {
          $( '#addInvestigation' ).removeClass( 'addInvestigationActive' );
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