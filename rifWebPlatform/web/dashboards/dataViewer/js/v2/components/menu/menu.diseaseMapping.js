RIF.menu = ( function( settings ) {

  var menus = settings.menus,

    _p = {

      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      proxy: {

        studyReady: function( studies ) {
          _p.facade.setStudy( studies[ 0 ] );
        },

        investigationReady: function( inv ) {
          _p.facade.setInvestigation( inv[ 0 ] );
          _p.settings.request( 'getAllFieldsAvailable', inv[ 0 ] ); // setting menu 
        },

        resultSetsReady: function( resSets ) {
          _p.settings.callback( 'resultSetsReady', resSets );
          _p.facade.setResultSet( resSets[ 0 ] );
          _p.facade.setResultSets( resSets );
          _p.facade.areaChartSelectionChanged( _p.getCheckedValues( "resultsSets" ) );
        },

        genderReady: function( genders ) {
          _p.facade.setGender( genders[ 0 ] );
        },

        yearReady: function( years ) {
          _p.facade.setYear( years[ 0 ] );
          _p.facade.menusReady();
        },

      },


      showScaleRangeInterface: function( args ) {
        _p.choropleth.showScaleRange( args );
      },

      populate: function( args ) {
        RIF.getZoomIdentifiers( _p.callbacks.zoomToOptions, [ args.geoLvl ] );
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