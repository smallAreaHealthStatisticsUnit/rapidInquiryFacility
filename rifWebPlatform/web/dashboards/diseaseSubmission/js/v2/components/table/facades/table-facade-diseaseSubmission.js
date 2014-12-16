RIF.table[ 'facade-diseaseSubmission' ] = ( function( _p ) {


  // TO BE MODIFIED, below is the copy of data manager facade
  var facade = {

    //Subscriber
    studyAreaSelectionEvent: function( rowId ) {
      this.fire( 'studyAreaSelectionEvent', rowId )
    },

    selectAtChanged: function( geolvl ) {
      _p.proxy.updateStudyGrid( geolvl );
    }

  };

  return facade;


} );