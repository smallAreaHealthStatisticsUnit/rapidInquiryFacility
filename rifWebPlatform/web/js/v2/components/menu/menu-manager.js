RIF.menu = ( function( settings ) {

  var menus = settings.menus,

    _p = {

      init: function() {
        _p = RIF.mix( RIF.menu.utils(), _p );
        return _p;
      },

      currentGeolevel: function() {
        return _p.getGeolevel();
      },

      getHistogramSettings: function() {
        return {
          geoLevel: _p.getGeolevel(),
          field: _p.getHistogramSelection(),
          dataSet: _p.getDataset()
        };
      },

      getPyramidSettings: function() {
        return {
          geoLevel: _p.getGeolevel(),
          field: _p.getPyramidSelection(),
          dataSet: _p.getDataset()
        };
      },

      /* Get all data necessary to populate all menus for Data Manager */
      populateManager: function( args ) {
        var clbks = _p.callbacks;

        RIF.getFields( clbks.avlbFieldsSettings, [ _p.getDataset() ] );
        RIF.getNumericFields( [ clbks.avlbFieldsChoro, clbks.avlbFieldsHistogram ], [ _p.getDataset() ] );
        RIF.getFieldsStratifiedByAgeGroup( clbks.avlbFieldsPyramid, [ _p.getGeolevel(), _p.getDataset() ] );
        RIF.getZoomIdentifiers( clbks.zoomTo, [ args.geoLvl ] );
      },

      callbacks: {

        avlbFieldsSettings: function() {
          _p.dropDown( this, _p.hoverSlct );
          _p.fieldCheckboxes( this, _p.colsFilter, _p.colsFilterName );
        },

        avlbFieldsChoro: function() {
          _p.dropDown( this, _p.fieldToMap );
          if ( this.length === 0 ) {
            _p.greyOut( _p.menu );
          } else {
            _p.removeGreyOut( _p.menu );
          }
        },

        avlbFieldsHistogram: function() {
          _p.dropDown( this, _p.histoSlct );
          _p.facade.updateHistogram();

        },

        avlbFieldsPyramid: function() {
          _p.dropDown( this, _p.pyramidSlct );
          _p.facade.updatePyramid();
        },

        zoomTo: function() {
          _p.dropDown( this, _p.zoomTo );
        }
      },

      showScaleRangeInterface: function( args ) {
        _p.showScaleRange( args );
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