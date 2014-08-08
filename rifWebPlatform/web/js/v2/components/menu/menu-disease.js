RIF.menu = ( function( settings ) {

  var menus = settings.menus,

    _p = {
      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      callbacks: {

        studyCallback: function() { // called once only

          if ( this.length > 0 ) {
            _p.dropDown( this, _p.study );
            _p.getInvestigations( this[ 0 ] );
            _p.getZoomIds( this[ 0 ] );
          }

          _p.facade.addGeolevel( this[ 0 ] );
        },

        avlbInvestigations: function() {
          _p.dropDown( this, _p.investigation );
          _p.getResultsSet( this[ 0 ] );
        },

        avlbResultSet: function() {
          _p.dropDown( this, _p.resultSet );
          //Fire add geolevel
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