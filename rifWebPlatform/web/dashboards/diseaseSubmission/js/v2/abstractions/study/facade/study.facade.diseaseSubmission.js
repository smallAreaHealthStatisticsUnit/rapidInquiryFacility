RIF.study[ 'facade-diseaseSubmission' ] = ( function() {

  var facade = {


    //FIRERS



    //SuBSCRIBERS
    studyNameChanged: function( arg ) {
      this.setStudyName( arg );
    },
    healthThemeChanged: function( arg ) {
      this.setHealthTheme( arg );
    },
    numeratorChanged: function( arg ) {
      this.setNumerator( arg );
    },
    denominatorChanged: function( arg ) {
      this.setDenominator( arg );
    },
    selectAtChanged: function( arg ) {
      this.setSelectAt( arg );
    },
    resolutionChanged: function( arg ) {
      this.setResolution( arg );
    }

  };


  return facade;
} );