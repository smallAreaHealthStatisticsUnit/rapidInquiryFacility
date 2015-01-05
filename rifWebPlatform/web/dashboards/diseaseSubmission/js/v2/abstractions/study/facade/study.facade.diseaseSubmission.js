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
    },


    // Firer  
    selectAtChangeUpdate: function( geolvl ) {
      this.fire( 'selectAtChangeUpdate', geolvl );
    }

  };


  return facade;
} );