RIF.menu.diseaseStudyLevel = ( function() {

  var parent = this,

    /* geolevel obj */
    _p = {

      initdiseaseStudyLevel: function() {
        this.events();
        this.getStudies();
      },

      /* DOM elements */
      study: $( '#studyId select' ),
      investigation: $( '#invId select' ),
      resultSet: $( '#Resultset select' ),
      zoomTo: $( '#zoomTo select' ),


      getStudies: function() {
        RIF.getStudies( parent.callbacks.studyCallback, [] );
      },

      getInvestigations: function( studyId ) {
        RIF.getInvestigations( parent.callbacks.avlbInvestigations, [ studyId ] );
      },

      getResultsSetAvailable: function( studyId ) {
        RIF.getResultsSetAvailable( parent.callbacks.avlbResultSet, [ studyId ] );
      },

      getZoomIds: function( studyId ) {
        //RIF.getZoomIdentifiers( _p.callbacks.zoomTo , [ studyId ] );
      },

      updateOtherComponents: function() {
        //parent.facade.addGeolevel( _p.currentGeolvl, _p.currentdataset );
      },

      /* Set - Gets */
      setStudy: function( studyId ) {
        _p.study = studyId;
        //May want to fire a set study event to register in high level module  
      },

      setInvestigation: function( investigation ) {
        _p.investigation = investigation;
      },


      setResultSet: function( resultSet ) {
        _p.resultSet = resultSet;
      },

      getStudy: function() {
        return _p.study;
      },

      getInvestigation: function() {
        return _p.investigation;
      },

      getResultSet: function() {
        return _p.resultSet;
      },


      /* events */
      events: function() {
        this.zoomTo.on( 'change', function() {
          parent.facade.zoomTo( RIF.replaceAll( '_', ' ', this.value ) );
        } );

        /*		this.dataSet.on('change', function() {
					parent.updateSettings(  this.value );
				});
				
				this.geolevels.on('change', function() {
					_p.setGeolevel( this.value );
				});*/
      }
    };

  _p.initdiseaseStudyLevel();

  return _p;
} );