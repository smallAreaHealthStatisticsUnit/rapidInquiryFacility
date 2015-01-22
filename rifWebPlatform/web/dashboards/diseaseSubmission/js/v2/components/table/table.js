RIF.table = ( function( settings ) {

  var tables = settings.tables;

  _p = {

    init: function() {
      _p = RIF.mix( RIF.table.utils(), _p );
      return _p;
    },

    proxy: {
      updateStudyGrid: function( geoLvl ) {
        _p.areaSelectionRenderer.request( 'getTabularData', geoLvl );
      },

      addInvestigationRow: function( inv ) {
        _p.investigationsRecap.addRow( inv );
      },

      removeInvestigationRow: function( inv ) {
        _p.facade.removeInvestigationRow( inv );
      },

      getAgeGroups: function( numerator ) {
        _p.ageGroupsRenderer.request( 'getAgeGroups', numerator );
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
    },

    specialEvent: function() {
      $( document ).mouseup( function() {
        _p.isMouseDown = false;
      } );
      return _p;
    },

    isMouseDown: false

  };

  _p.init()
    .getFacade()
    .extendTable()
    .specialEvent() // use when selecting rows from table holding down mouse and drag down/up
  // .setEvents();


  return _p.facade;

} );