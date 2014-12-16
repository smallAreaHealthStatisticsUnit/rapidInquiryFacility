RIF.table = ( function( settings ) {

  var tables = settings.tables,

    _p = {



      init: function() {
        _p = RIF.mix( RIF.table.utils(), _p );
        return _p;
      },

      proxy: {
        updateStudyGrid: function( geoLvl ) {
          _p.areaSelectionRenderer.request( 'getTabularData', geoLvl );
        }
      },

      getFacade: function() {
        this.facade = RIF.getFacade( 'table', settings.studyType, this );
        return this;
      },

      extendTable: function() {
        _p = this.extendTableComponent( _p, tables );
        return _p;
      },

      setEvents: function( table ) {
        var units = table || tables;
        var ev = this.setTableEvent( _p, tables );
      }


    };

  _p.init()
    .getFacade()
    .extendTable()
    .setEvents();

  return _p.facade;

} );