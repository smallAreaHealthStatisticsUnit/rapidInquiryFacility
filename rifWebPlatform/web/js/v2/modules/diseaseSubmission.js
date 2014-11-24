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
        menus: [ 'diseaseSubmissionFront' /*'settings', 'baseMap', 'transparency'*/ ]
      },
      //resizable: 'diseaseSubmission'
    },

    events: {
      /**--STUDY OBJECT RELATED---**/
    },

    init: function() {
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