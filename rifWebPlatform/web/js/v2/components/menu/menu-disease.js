RIF.menu = ( function( settings ) {

  var menus = settings.menus,
    _studies,
    _investigations,
    _resultSets,
	_genders,
	_years,

    _p = {
      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      callbacks: {

        studyCallback: function() { // called once only

          if ( this.length > 0 ) {
            _studies = this;
			_p.facade.setStudy( _studies[ 0 ] );
            _p.dropDown( _studies, _p.getDiseaseStudyLevelMenuDom( "study" ) );
            _p.getInvestigations( _studies[ 0 ] );
            _p.getZoomIds( _studies[ 0 ] );
          }

        },

        avlbInvestigations: function() {
          _investigations = this;
		  _p.facade.setInvestigation( _investigations[ 0 ] );
          _p.dropDown( _investigations, _p.getDiseaseStudyLevelMenuDom( "investigation" ) );
          _p.getResultsSetAvailable();
          _p.getAllFieldsAvailable( _investigations[ 0 ] );
        },

        avlbResultSet: function() {
          _resultSets = this;
          _p.facade.setResultSet ( _resultSets[ 0 ] );
		  _p.dropDown( _resultSets, _p.getDiseaseStudyLevelMenuDom( "resultSet" ) );
          _p.fieldCheckboxesResultsSet( _resultSets, _p.getSettingsMenuDom( "resultsChoice" ), "resultsSets" );
		  _p.facade.areaChartSelectionChanged (_p.getCheckedValues( "resultsSets" ));
		  _p.getGenderAvailable();

        },
		
		avlbGender: function(){
		  _genders = this;
          _p.facade.setGender ( _genders[ 0 ] );
		  _p.dropDown( _genders, _p.getDiseaseStudyLevelMenuDom( "gender" ) );
		  _p.getYearsAvailable();
		},
		
		avlbYears: function(){
		  _years = this;
          _p.facade.setYear ( _years[ 0 ] );
		  _p.dropDown( _years, _p.getDiseaseStudyLevelMenuDom( "year" ) );
		  _p.facade.menusReady();
		},

        avlbFieldsChoro: function() {
          _p.dropDown( this, _p.getChoroplethMenuDom( "fieldToMap" ) );
          if ( this.length === 0 ) {
            _p.greyOut( _p.getChoroplethMenuDom( "menu" ) );
          } else {
            _p.removeGreyOut( _p.getChoroplethMenuDom( "menu" ) );
          }
        },

        zoomToOptions: function() {
          _p.dropDown( this, _p.getDiseaseStudyLevelMenuDom( "zoomTo" ) );
        },
      },

      showScaleRangeInterface: function( args ) {
        _p.showScaleRange( args );
      },

      populate: function( args ) {
        RIF.getZoomIdentifiers( _p.callbacks.zoomToOptions, [ args.geoLvl ] );
      },

      setEvents: function() {
        var ev = RIF.getEvent( 'menu', settings.studyType );
        ev.call( this );
      },

      extendMenu: function() {
        _p = RIF.extendComponent( 'menu', _p, menus );
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