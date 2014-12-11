RIF.table = ( function( settings ) {

  var tables = settings.tables,

    _p = {



      init: function() {
        _p = RIF.mix( RIF.table.utils(), _p );
        return _p;
      },


      getFacade: function() {
        this.facade = RIF.getFacade( 'table', settings.studyType, this );
        return this;
      },

      extendTable: function() {
        _p = this.extendTableComponent( _p, tables );
        return _p;
      },

      setEvents: function() {
        var ev = this.setTableEvent( _p, tables );
      }


    };

  _p.init()
    .getFacade()
    .extendTable()
    .setEvents();

  return _p.facade;

} );