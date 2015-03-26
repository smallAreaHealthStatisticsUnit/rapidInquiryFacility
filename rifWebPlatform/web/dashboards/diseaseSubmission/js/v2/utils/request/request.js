( function () {
  var c = function ( myFunc, msg ) {
      //IE compatible
      return function ( error, status, json ) {
        try {
          var data = jQuery.parseJSON( json.responseText );
          for ( var key in data[ 0 ] ) {
            if ( key == 'errorMessages' ) {
              RIF.statusBar( msg, false );
              RIF.statusBar( data[ 0 ][ key ], true, 1 );
              return;
            };
          };
          callback( myFunc, data );
        } catch ( e ) {
          callback( myFunc, json.responseText ); // This should change when working on results viewer
        }
        RIF.statusBar( msg, false );
      };
    },
      
    asynCall = function () {

      var args = Array.prototype.slice.call( arguments, 0 ),
        callback = args[ 1 ],
        mime = args[ 2 ] || "text/plain",
        parameters = args[ 0 ] + '&userID=' + RIF.user,
        url = 'http://localhost:8080/rifServices/' + parameters;

      $.ajax( {
        url: url
      } ).done( callback ).error( function ( jqXHR, textStatus, errorThrown ) {
        var msg = "Something went wrong with the following service: <br/>" + url + '<br/><br/>' + textStatus + '<br/>' + errorThrown;
        RIF.statusBar( msg, true, 1 );
      } );
    },

    callback = function ( myFuncts, data ) {
      if ( myFuncts instanceof Array ) {
        var l = myFuncts.length;
        while ( l-- ) {
          myFuncts[ l ].call( data );
        }
        return;
      } else if ( typeof myFuncts === 'function' ) {
        myFuncts.call( data )
      };
    },
      
    xhr = function ( url, clbk, msg, mime ) {
      RIF.statusBar( msg, true );
      asynCall( url, c( clbk, msg ), mime );
    },
      
    requests = {
      getGeneralRequest: function ( url, myCallback ) {
        xhr( url, myCallback );
        return {};
      },
      /*
       *
       * Disease submission specific
       *
       */
      getIsLoggedIn: ( function ( myCallback, params ) {
        var msg = " Checking if already logged in.. ",
          userName = params[ 0 ],
          pw = params[ 1 ],
          args = 'userID=' + userName;
        xhr( 'studySubmission/isLoggedIn?' + args, myCallback, msg );
      } ),
      getLogOut: ( function ( myCallback, params ) {
        var msg = " Checking if already logged in.. ",
          userName = params[ 0 ],
          pw = params[ 1 ],
          args = 'userID=' + userName;
        xhr( 'studySubmission/logout?' + args, myCallback, msg );
      } ),
      getLogIn: ( function ( myCallback, params ) {
        var msg = "Logging in.. ",
          userName = params[ 0 ],
          pw = params[ 1 ],
          args = 'userID=' + userName + '&password=' + pw;
        xhr( 'studySubmission/login?' + args, myCallback, msg );
      } ),
      getHealthThemes: function ( myCallback, params ) {
        var msg = "Retrieving Health Themes ",
          args = 'geographyName=SAHSU';
        xhr( 'studySubmission/getHealthThemes?' + args, myCallback, msg );
      },
      getNumeratorDenominator: function ( myCallback, params ) {
        var msg = "Retrieving Numerator denominator pairs ",
          args = 'geographyName=SAHSU&healthThemeDescription=' + params[ 0 ];
        xhr( 'studySubmission/getNumerator?' + args, myCallback, msg );
      },
      getSelectAt: function ( myCallback, params ) {
        var msg = "Retrieving geolevel resolutions ",
          args = 'geographyName=SAHSU';
        xhr( 'studySubmission/getGeoLevelSelectValues?' + args, myCallback, msg, 'application/json' );
      },
      getResolutions: function ( myCallback, params ) {
        var msg = "Retrieving geolevel resolutions ",
          args = 'geoLevelSelectName=' + params[ 1 ] + '&geographyName=SAHSU';
        xhr( 'studySubmission/getGeoLevelViews?' + args, myCallback, msg, 'application/json' );
      },
      getGeolevelSelect: function ( myCallback, params ) {
        var msg = "Retrieving Area ids for geolevel:" + params[ 0 ];
        args = 'geographyName=SAHSU&geoLevelSelectName=LEVEL2&geoLevelAreaName=Elliot',
        args += '&geoLevelToMapName=' + params[ 0 ];
        xhr( 'studySubmission/getMapAreas?' + args, myCallback, msg, 'application/json' );
      },
      getHealthTaxonomy: function ( myCallback, params ) {
        var msg = "Retrieving taxonomy codes",
          args = '';
        xhr( 'studySubmission/getHealthCodeTaxonomies?' + args, myCallback, msg, 'application/json' );
      },
      getTopLevelHealthCodes: function ( myCallback, params ) {
        var msg = "Retrieving top level health codes",
          args = 'healthCodeTaxonomyNameSpace=' + params[ 0 ];
        xhr( 'studySubmission/getTopLevelCodes?' + args, myCallback, msg, 'application/json' );
      },
      getSubHealthCodes: function ( myCallback, params ) {
        var msg = "Retrieving sub level health codes",
          args = 'healthCode=' + params[ 1 ] + '&healthCodeNameSpace=' + params[ 0 ];
        xhr( 'studySubmission/getImmediateChildHealthCodes?' + args, myCallback, msg, 'application/json' );
      },
      getYearRange: function ( myCallback, params ) {
        var msg = "Retrieving Years",
          numerator = params[ 0 ],
          args = 'geographyName=SAHSU&numeratorTableName=' + numerator;
        xhr( 'studySubmission/getYearRange?' + args, myCallback, msg, 'application/json' );
      },
      getSexes: function ( myCallback, params ) {
        var msg = "Retrieving Genders",
          args = '';
        xhr( 'studySubmission/getSexes?' + args, myCallback, msg, 'application/json' );
      },
      getAgeGroups: function ( myCallback, params ) {
        var msg = "Retrieving Age groups",
          numerator = params[ 0 ],
          args = 'geographyName=SAHSU&numeratorTableName=' + numerator;
        xhr( 'studySubmission/getAgeGroups?' + args, myCallback, msg, 'application/json' );
      },
      getCovariates: function ( myCallback, params ) {
        var msg = "Retrieving Covariates",
          args = 'geographyName=SAHSU&geoLevelSelectName=LEVEL1&geoLevelToMapName=LEVEL3';
        xhr( 'studySubmission/getCovariates?' + args, myCallback, msg, 'application/json' );
      },
      getSearchHealthCodes: function ( myCallback, params ) {
        var msg = "Searching health codes",
          taxonomy = params[ 0 ],
          searchTxt = params[ 1 ],
          args = 'nameSpace=' + taxonomy + '&searchText=' + params[ 1 ] + '&isCaseSensitive=false';
        xhr( 'studySubmission/getHealthCodesMatchingSearchText?' + args, myCallback, msg, 'application/json' );
      }
    };

  RIF.utils.extend( requests, RIF );

}() );