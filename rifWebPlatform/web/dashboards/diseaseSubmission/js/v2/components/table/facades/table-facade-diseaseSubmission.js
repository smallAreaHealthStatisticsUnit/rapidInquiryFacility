RIF.table[ 'facade-diseaseSubmission' ] = ( function( _p ) {


  // TO BE MODIFIED, below is the copy of data manager facade
  var facade = {

    //Subscriber
    selectAtChanged: function( geolvl ) {
      _p.proxy.updateStudyGrid( geolvl );
    },


    //FIRER
    studyAreaSelectionEvent: function( rowId ) {
      this.fire( 'studyAreaSelectionEvent', rowId )
    },

    ageGroupsChanged: function( ageGroups ) {
      this.fire( 'ageGroupsChanged', ageGroups );
      this.isInvestigationReady();
    },

    isInvestigationReady: function() {
      this.fire( 'isInvestigationReady', null );
    }

  };

  return facade;


} );