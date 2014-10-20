RIF.menu.diseaseStudyLevel = ( function() {

  var parent = this,

    _domObjects = {
      /* DOM elements */
      study: $( '#studyId select' ),
      investigation: $( '#invId select' ),
      resultSet: $( '#Resultset select' ),
	  year: $( '#yearsAvailable select' ),
      gender: $( '#gendersAvailable select' ),
      zoomTo: $( '#zoomTo select' ),
    },

    /* events */
    _events = function() {
      /*_domObjects.zoomTo.on( 'change', function() {
        parent.facade.zoomTo( RIF.replaceAll( '_', ' ', this.value ) );
      } );*/
    },

    /* geolevel obj */
    _p = {

      initdiseaseStudyLevel: function() {
        _events();
        this.getStudies();
      },

      getStudies: function() {
        RIF.getStudies( parent.callbacks.studyCallback, [] );
      },

      getInvestigations: function( studyId ) {
        RIF.getInvestigations( parent.callbacks.avlbInvestigations, [ studyId ] );
      },

      getResultsSetAvailable: function(  ) {
        RIF.getResultsSetAvailable( parent.callbacks.avlbResultSet, [ ] );
      },
	  
	  getGenderAvailable: function(  ) {
        RIF.getGenderAvailableForStudy( parent.callbacks.avlbGender, [] );
      },
	  
	  getYearsAvailable: function( ) {
        RIF.getYearsAvailableForStudy( parent.callbacks.avlbYears, [] );
      },

      getZoomIds: function( studyId ) {
        //RIF.getZoomIdentifiers( _p.callbacks.zoomTo , [ studyId ] );
      },


      /* Set - Gets */
      setStudy: function( studyId ) {
        _study = studyId;
        //May want to fire a set study event to register in high level module  
      },

      setInvestigation: function( investigation ) {
        _p.investigation = investigation;
      },


      setResultSet: function( resultSet ) {
        _p.resultSet = resultSet;
      },

      getStudy: function() {
        return _study;
      },

      getInvestigation: function() {
        return _p.investigation;
      },

      getResultSet: function() {
        return _p.resultSet;
      },

      getDiseaseStudyLevelMenuDom: function( obj ) {
        return _domObjects[ obj ];
      },

    };

  _p.initdiseaseStudyLevel();

  return _p;
} );