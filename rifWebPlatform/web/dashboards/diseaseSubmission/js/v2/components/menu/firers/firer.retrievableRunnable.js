RIF.menu[ 'firer-retrievableRunnable' ] = ( function () {

  var firer = {
    isStudyReady: function () {
      this.fire( "isStudyReady", null );
    },

    projectChanged: function ( arg ) {
      this.fire( "projectChanged", arg );
    },

    studyDescriptionChanged: function ( arg ) {
      this.fire( "studyDescriptionChanged", arg );
    }
  };

  return firer;
} );