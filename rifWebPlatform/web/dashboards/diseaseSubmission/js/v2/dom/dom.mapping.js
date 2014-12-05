RIF.dom = ( function() {


  var get = function( id ) {
    return document.getElementById( id )
  },

  dom = {
    menu: {
      frontMapping: function() {
        return {
          /* Health theme drop down list */
          study: $( '#studyId select' ),
          studySelect: $( '#studyId select' ),
          investigation: $( '#invId select' ),
          resultSet: $( '#Resultset select' ),
          year: $( '#yearsAvailable select' ),
          gender: $( '#gendersAvailable select' ),
          zoomTo: $( '#zoomTo select' ),
          /* Close buttons and extent and clear buttons */
          dialogClose: $( '.modal_close' ),
          clearBtn: $( "#clearSelection" ),
          zoomToExtent: $( "#zoomExtent" )
        }
      },
      settings: function() {
        return {
          settingsBtn: $( "#settingsButton" ),
          settingsMenu: $( "#settings" ),
          resultsChoice: $( "#resultsFilter" ),
          hoverSlct: $( '#fldSlct' ),
          save: $( ".save-fld-settings" )
        }
      },
      choropleth: function() {
        return {
          choroplethModal: $( "#choropleth" ),
          choroplethBtn: $( "#choroplethBtn" ),
          save: $( ".save-fld" ),
          classification: $( "#classification" ),
          palette: $( '.palette' ),
          colourScales: $( '.palette div' ),
          selectedScale: "#selectedScale",
          intervals: $( "#intervals" ),
          edit: $( ".editBreaks a" ),
          invert: $( "#invertColors" ),
          breaks: $( "#breaks" ),
          selectedScale: $( "#selectedScale" ),
          dropDownColourScale: $( '#dropDownColourScale' )
        }
      },
      baseMap: function() {
        return {
          baseMap: $( "#baseMap" ),
          baseMapObj: $( '#baseMapBtn' ),
          saveObj: $( '#savebasemap' ),
          baseMapFieldSelection: $( '#baseMapSlct' ),
          nobasemap: $( '#nobasemap' ),
          basemapselector: $( '#basemapselector' )
        }
      },
      transparency: function() {
        return {
          transpContainer: $( '#transparencyContainer' ),
          transpSlider: $( '#transparencyContainer input' )
        }
      },

    }
  };


  RIF.dom = dom;

} );