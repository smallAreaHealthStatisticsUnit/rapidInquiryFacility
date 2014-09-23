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
      save: $( ".save-fld-settings" ),


      /* events */
      events: function() {

        this.settings.click( function() {
          $( "#settings" ).show();
        } );

        this.save.click( function() {

          $( "#settings" ).hide();
        } );
      }
    };

  _p.initSettings();

  return _p;
} );