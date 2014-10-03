RIF.menu.settings = ( function() {

  var parent = this,

    /* geolevel obj */
    _p = {

      initSettings: function() {
        this.events();
      },

      /* DOM elements */
      settings: $( ".settings" ),
      resultsChoice: $( "#resultsFilter" ),
      hoverSlct: $( '#fldSlct' ),
      save: $( ".save-fld-settings" ),

      avlbFieldsSettings: function() {
        parent.dropDown( this, _p.hoverSlct );
      },

      getAllFieldsAvailable: function( investigation ) {
        RIF.getFields( _p.avlbFieldsSettings, [ /*investigation*/ "atlas_leu_f" ] );
      },

      /* events */
      events: function() {

        this.settings.click( function() {
          $( "#settings" ).show();
        } );

        this.save.click( function() {
          parent.facade.hoverFieldChange( _p.hoverSlct.val() );
          var resultSetChoice = parent.getCheckedValues( "resultsSets" );
          parent.facade.resultSetSelectionChanged( resultSetChoice );
          $( "#settings" ).hide();
        } );
      }
    };

  _p.initSettings();

  return _p;
} );