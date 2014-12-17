RIF.menu.studyArea = ( function( _dom ) {

  var parent = this,

    _requests = {
      getSelectAtsAvailable: function( user ) {
        RIF.getSelectAt( _callbacks[ 'getSelectAt' ], [ user ] );
      },
      getResolutionsAvailable: function( params ) {
        RIF.getResolutions( _callbacks[ 'getResolutions' ], [ params[ 0 ], params[ 1 ] ] );
      },



    },

    _callbacks = {
      getSelectAt: function() {
        var selectAts = this[ 0 ].names;
        parent.dropDownInputText( selectAts, _dom.selectAtAvailable );
        parent.proxy.studyAreaReady();
      },
      getResolutions: function() {
        var resolutions = this[ 0 ].names;
        //check if current resolution is allowed
        _dom.resolution
          .removeClass( 'inputBorderSelection' )
          .val( '' );

        parent.dropDownInputText( resolutions, _dom.resolutionAvailable );
      }
    },

    /* geolevel obj */
    _p = {

      request: function( reqName, params ) {
        _requests[ reqName ]( params );
      },

    };


  return {
    studyArea: _p
  };
} );