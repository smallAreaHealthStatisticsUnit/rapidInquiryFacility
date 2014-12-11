/*
 *  The Manager module allows you to visualize raw data
 *  with maps , charts and tables.
 *  Data can be exported as .shp , csv , GEOJSON.
 *  The main purpose of this module is to set event handlers
 *  and  relations between the modules involved in Manager.
 *
 *  @components
 *      list of modules dynamically initialized
 *  @events
 *      list of events occurring within the manager module
 *      ->firer:
 *          objects that will fire the specific event
 *      ->subscriber
 *          objects that will handle the event fired
 *      ->method
 *          method which must be implemented in subscriber object
 */
RIF.diseaseSubmission = ( function() {

  var _p = {

    components: {
      study: 'diseaseSubmission',
      /*map: {
        studyType: 'diseaseSubmission',
        layerType: 'tilesvg'
      },
      */
      menu: {
        studyType: 'diseaseSubmission',
        menus: [ 'frontSubmission', 'studyArea' ]
      },
      table: {
        studyType: 'diseaseSubmission',
        tables: [ 'areaSelectionRenderer' ]
      }
    },

    events: {
      /**--STUDY OBJECT RELATED---**/


      studyNameChanged: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "studyNameChanged"
      },

      healthThemeChanged: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "healthThemeChanged"
      },

      numeratorChanged: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "numeratorChanged"
      },

      denominatorChanged: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "denominatorChanged"
      },

      resolutionChanged: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "resolutionChanged"
      },

      selectAtChanged: {
        subscribers: [ "study" ],
        firer: [ "menu" ],
        method: "selectAtChanged"
      }

    },

    init: function() {
      RIF.dom();
      RIF.initComponents.call( this );
      RIF.addEvents.call( this );
    }

  };


  return {
    setUp: ( function( args ) {
      _p.init();
    }() )
  };

} );