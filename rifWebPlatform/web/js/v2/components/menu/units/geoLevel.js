RIF.menu.geoLevel = ( function() {

  var parent = this,

    /* geolevel obj */
    _p = {

      initGeolevel: function() {
        this.events();
        RIF.getGeolevels( _p.geolevelsClbk );
      },

      /* DOM elements */
      geolevels: $( '#geolevel select' ),
      zoomTo: $( '#zoomTo select' ),
      dataSet: $( '#dataSet select' ),
      avlbFlds: $( '' ),

      /* callbacks */
      geolevelsClbk: function() { // called once only
        if ( this.length > 0 ) {
          parent.dropDown( this, _p.geolevels );
          _p.setGeolevel( this[ 0 ] ); //First geolevel in list
          _p.getDataSets();
        }
      },

      getDataSets: function() {
        RIF.getDataSetsAvailable( _p.dataSetsClbk, [ _p.currentGeolvl ] );
      },

      dataSetsClbk: function() {
        _p.setDataset( this[ 0 ] );
        parent.dropDown( this, _p.dataSet );
        _p.updateOtherComponents();
      },

      updateOtherComponents: function() {
        parent.facade.addGeolevel( _p.currentGeolvl, _p.currentdataset );
        parent.facade.addTabularData( _p.currentdataset );
      },

      setGeolevel: function( geolvl ) {
        _p.currentGeolvl = geolvl;
      },

      setDataset: function( dataset ) {
        _p.currentdataset = dataset;
      },

      getGeolevel: function( geolvl ) {
        return _p.currentGeolvl;
      },

      getDataset: function( geolvl ) {
        return _p.currentdataset;
      },

      /* events */
      events: function() {
        this.zoomTo.on( 'change', function() {
          parent.facade.zoomTo( RIF.replaceAll( '_', ' ', this.value ) );
        } );

        this.dataSet.on( 'change', function() {
          parent.facade.addTabularData( this.value );
          parent.updateSettings( this.value );
        } );

        this.geolevels.on( 'change', function() {
          _p.setGeolevel( this.value );
          _p.getDataSets();
        } );
      }
    };

  _p.initGeolevel();

  return _p;
} );