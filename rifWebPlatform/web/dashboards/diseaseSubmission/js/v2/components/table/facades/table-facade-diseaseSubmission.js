RIF.table[ 'facade-diseaseSubmission' ] = ( function( studyType ) {


  // TO BE MODIFIED, below is the copy of data manager facade
  var facade = {

    //Subscriber
    studyAreaSelectionEvent: function( rowId ) {
      this.fire( 'studyAreaSelectionEvent', rowId )
    }

  };

  return facade;


} );