RIF.study[ 'facade-diseaseSubmission' ] = ( function() {

  var facade = {


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
      this.setStudyAreaSelectAt( arg );
      this.selectAtChangeUpdate( arg );
    },
    resolutionChanged: function( arg ) {
      this.setStudyAreaResolution( arg );
    },
    studyAreaSelectionEvent: function( rows ) {
      /*var currenSelection =  this.getStudyAreas(),
              l = rows.length,
              newSlct = [];
           
          while(l--){
              var  current = rows[l],
                  isPresent = false,
                   k = currenSelection.length || 0;
              while( k-- ){
                if( currenSelection[k] ==  current ){
                    isPresent = true;
                    currenSelection.splice(k, 1);
                    break;
                }    
              }
              
              if(! isPresent){
                newSlct.push(current);  
              }
              
          };*/
      this.setStudyAreas( RIF.unique( rows ) );
    },
    taxonomyChanged: function( arg ) {
      this.setHealthConditionTaxonomy( arg );
      this.setHealthCodes( null );
    },
    icdSelectionChanged: function( arg ) {
      if ( arg.length == 0 ) {
        arg = null;
      };
      this.setHealthCodes( arg );
    },
    startYearChanged: function( arg ) {
      this.setMinYear( arg );
    },
    endYearChanged: function( arg ) {
      this.setMaxYear( arg );
    },
    genderChanged: function( arg ) {
      this.setGender( arg );
    },
    covariatesChanged: function( arg ) {
      this.setCovariates( arg );
    },
    ageGroupsChanged: function( arg ) {
      if ( arg.length <= 0 ) {
        arg = null;
      };
      this.setAgeGroups( arg );
    },

    isInvestigationReady: function() {
      for ( var i in this.parameters ) {
        if ( i != 'covariates' && this.parameters[ i ] == null ) {
          this.investigationNotReadyToBeAdded();
          return;
        }
      }
      this.investigationReadyToBeAdded();
    },


    // FIRERS  
    selectAtChangeUpdate: function( geolvl ) {
      this.fire( 'selectAtChangeUpdate', geolvl );
    },

    investigationReadyToBeAdded: function() {
      this.fire( 'investigationReadyToBeAdded', null );
    },

    investigationNotReadyToBeAdded: function() {
      this.fire( 'investigationNotReadyToBeAdded', null );
    },

  };


  return facade;
} );