RIF.menu = ( function( settings ) {

  var menus = settings.menus,
	 _studies, 
	 _investigations,
	 _resultSets,
    _p = {
      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      callbacks: {

        studyCallback: function() { // called once only

          if ( this.length > 0 ) {
			_studies = this;
            _p.dropDown( _studies, _p.study );
            _p.getInvestigations( _studies[ 0 ] );
            _p.getZoomIds( _studies[ 0 ] );
          }

        },

        avlbInvestigations: function() {
		  _investigations = this;	
          _p.dropDown( _investigations, _p.investigation );
          _p.getResultsSetAvailable( _investigations[ 0 ] );
        },

        avlbResultSet: function() {
		  _resultSets = this;
          _p.dropDown( _resultSets, _p.resultSet );
		  _p.dropDown( _resultSets, _p.fieldToMap );//Choropleth dialog
		  _p.fieldCheckboxesResultsSet( _resultSets, _p.resultsChoice, "resultsSets" );
		  
		  _p.facade.menusReady ( { 
			study: _studies[0], //By default we display the first study retrieved
			investigation: _investigations[0], //And first Ivestigation
			resultSet: _resultSets,
			/* TEMPORARY NULL VALUES */
			gender: null, // for now need to add gender selection LATER!
			year: null	// 	 for now need to add YEAR selection LATER!
		  });	
		  
        },

        avlbFieldsChoro: function() {
          _p.dropDown( this, _p.fieldToMap );
          if ( this.length === 0 ) {
            _p.greyOut( _p.menu );
          } else {
            _p.removeGreyOut( _p.menu );
          }
        },

        zoomTo: function() {
          _p.dropDown( this, _p.zoomTo );
        },
      },
	  
      showScaleRangeInterface: function( args ){
		  _p.showScaleRange( args );
	  },
	  
      populate: function( args ) {
        //_p.initdiseaseStudyLevel();
        //RIF.getNumericFields(  [_p.callbacks.avlbFieldsChoro, _p.callbacks.avlbFieldsHistogram], [_p.getDataset()] );
        RIF.getZoomIdentifiers( _p.callbacks.zoomTo, [ args.geoLvl ] );
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