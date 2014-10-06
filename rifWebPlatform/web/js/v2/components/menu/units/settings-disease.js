RIF.menu.settings = ( function() {

  var parent = this,
     
	 _domObjects = {
	  /* DOM elements */
      settings: $( ".settings" ),
      resultsChoice: $( "#resultsFilter" ),
      hoverSlct: $( '#fldSlct' ),
      save: $( ".save-fld-settings" ),
	 },
	
     /* events */
     _events = function() {

        _domObjects.settings.click( function() {
          $( "#settings" ).show();
        } );

        _domObjects.save.click( function() {
          parent.facade.hoverFieldChange( _domObjects.hoverSlct.val() );
          var resultSetChoice = parent.getCheckedValues( "resultsSets" );
          parent.facade.resultSetSelectionChanged( resultSetChoice );
          $( "#settings" ).hide();
        } )
      },
	
    /* geolevel obj */
    _p = {

      initSettings: function() {
        _events();
      },

      avlbFieldsSettings: function() {
        parent.dropDown( this, _domObjects.hoverSlct );
      },

      getAllFieldsAvailable: function( investigation ) {
        RIF.getFields( _p.avlbFieldsSettings, [ /*investigation*/ "atlas_leu_f" ] );
      },
	  
	  getSettingsMenuDom: function( obj ){
		return _domObjects[ obj ];
	  }
	  

    };

  _p.initSettings();

  return _p;
} );