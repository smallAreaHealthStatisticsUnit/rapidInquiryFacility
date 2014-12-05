RIF.menu.settings = ( function() {

  var parent = this,

    _domObjects = {
      /* DOM elements */
      save: $( ".save-fld-settings" ),
      hoverSlct: $( '#fldSlct' ),
      histoSlct: $( '#fldHistogram' ),
      pyramidSlct: $( '#fldPyramid' ),
      settings: $( ".settings" ),
      colsFilter: $( '#colsFilter' ),
      numRows: $( '#numOfRows' ),
      colsFilterName: "filterCols",

    },

    /* events */
    _events = function() {

      _domObjects.settings.click( function() {
        $( "#settings" ).show();
      } );

      _domObjects.save.click( function() {
        var fields = parent.getCheckedValues( _domObjects.colsFilterName );
        parent.facade.filterTablebyCols( fields );
        parent.facade.changeNumRows( _domObjects.numRows.val() );
        parent.facade.hoverFieldChange( _domObjects.hoverSlct.val() );
        parent.facade.updatePyramid();
        parent.facade.updateHistogram();
        $( "#settings" ).hide();
      } );
    },

    /* geolevel obj */
    _p = {

      initSettings: function() {
        _events();
      },

      getHistogramSelection: function() {
        return _domObjects.histoSlct.val();
      },

      getPyramidSelection: function() {
        return _domObjects.pyramidSlct.val();
      },

      getSettingsMenuDom: function( obj ) {
        return _domObjects[ obj ];
      }

    };

  _p.initSettings();

  return _p;
} );