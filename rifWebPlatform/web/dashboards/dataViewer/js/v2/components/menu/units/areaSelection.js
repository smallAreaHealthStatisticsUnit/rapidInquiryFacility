RIF.menu.diseaseSubmissionFront = ( function() {

  var parent = this,

    /* DOM ELEMENTS */
    _domObjects = {
      resolutionAvailableEl: document.getElementById( 'resolutionAvailable' ),
      selectAtAvailableEl: document.getElementById( 'selectAtAvailable' ),
      tableEl: document.getElementById( 'areaSelectionTable' ),
      mapEl: document.getElementById( 'areaSelectionMap' ),
    },


    /* EVENTS */
    _events = function() {

      $( _domObjects.resolutionAvailableEl ).children().click( function() {
        //Fire event Resolution  changed 
      } );

      $( _domObjects.selectAtAvailableEl ).children().click( function() {
        //Fire event Select At  changed 
      } );


    },

    /* geolevel obj */
    _p = {




    };


  return _p;
} );