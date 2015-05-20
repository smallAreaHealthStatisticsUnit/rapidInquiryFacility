(function() {
   var c = function(myFunc, msg) {
         //IE compatible
         return function(error, status, json) {
            try {
               var data = jQuery.parseJSON(json.responseText);
               for (var key in data[0]) {
                  if (key == 'errorMessages') {
                     statusBar(msg, false);
                     statusBar(data[0][key], true, 1);
                     return;
                  };
               };
               callback(myFunc, data);
            } catch (e) {
               callback(myFunc, json.responseText); // This should change when working on results viewer
            }
            statusBar(msg, false);
         };
      },

      asynCall = function() {

         var args = Array.prototype.slice.call(arguments, 0),
            callback = args[1],
            mime = args[2] || "text/plain",
            parameters = args[0] + '&userID=' + RIF.user + '&geographyName=' + RIF.geography,
            url = 'http://localhost:8080/rifServices/' + parameters;

         $.ajax({
            url: url
         }).done(callback).error(function(jqXHR, textStatus, errorThrown) {
            var msg = "Something went wrong with the following service: <br/>" + url + '<br/><br/>' + textStatus + '<br/>' + errorThrown;
            statusBar(msg, true, 1);
         });
      },

      callback = function(myFuncts, data) {
         if (myFuncts instanceof Array) {
            var l = myFuncts.length;
            while (l--) {
               myFuncts[l].call(data);
            }
            return;
         } else if (typeof myFuncts === 'function') {
            myFuncts.call(data)
         };
      },

      xhr = function(url, clbk, msg, mime) {
         statusBar(msg, true);
         asynCall(url, c(clbk, msg), mime);
      },

      statusBar = function(msg, showHide, statusCode) {
         if (msg != -1) {
            RIF.statusBar(msg, showHide, statusCode);
         }
      },

      requests = {
         getGeneralRequest: function(url, myCallback) {
            xhr(url, myCallback);
            return {};
         },
         /*
          *
          * Disease submission specific
          *
          */
         getIsLoggedIn: (function(myCallback, params) {
            var msg = " Checking if already logged in.. ",
               userName = params[0],
               pw = params[1];
            xhr('studySubmission/isLoggedIn?', myCallback, msg);
         }),

         getLogOut: (function(myCallback, params) {
            var msg = " Checking if already logged in.. ";
            xhr('studySubmission/logout?', myCallback, msg);
         }),

         getHealthThemes: function(myCallback, params) {
            var msg = "Retrieving Health Themes ";
            xhr('studySubmission/getHealthThemes?', myCallback, msg);
         },

         getNumeratorDenominator: function(myCallback, params) {
            var msg = "Retrieving Numerator denominator pairs ",
               args = 'healthThemeDescription=' + params[0];
            xhr('studySubmission/getNumerator?' + args, myCallback, msg);
         },

         getSelectAt: function(myCallback, params) {
            var msg = "Retrieving geolevel resolutions ";
            xhr('studySubmission/getGeoLevelSelectValues?', myCallback, msg, 'application/json');
         },

         getResolutions: function(myCallback, params) {
            var msg = "Retrieving geolevel resolutions ",
               args = 'geoLevelSelectName=' + params[0];
            xhr('studySubmission/getGeoLevelViews?' + args, myCallback, msg, 'application/json');
         },

         /*getGeolevelSelect: function ( myCallback, params ) {
        var msg = "Retrieving Area ids for geolevel:" + params[ 0 ];
        args = 'geoLevelSelectName=LEVEL2&geoLevelAreaName=Elliot',
        args += '&geoLevelToMapName=' + params[ 0 ];
        xhr( 'studySubmission/getMapAreas?' + args, myCallback, msg, 'application/json' );
      },*/

         getTableMapAreas: function(myCallback, params) {
            //geoLevelSelectName=LEVEL4&yMax=55.0122&xMax=-6.32507&yMin=54.6456&xMin=-6.68853
            var msg = "Retrieving Area ids for geolevel:" + params[0];
            args = [
            'geoLevelSelectName=' + params[0],
            'yMax=' + params[1]["yMax"],
            'yMin=' + params[1]["yMin"],
            'xMin=' + params[1]["xMin"],
            'xMax=' + params[1]["xMax"]
        ].join('&');

            xhr('studySubmission/getMapAreasForBoundaryRectangle?' + args, myCallback, msg, 'application/json');
         },

         getHealthTaxonomy: function(myCallback, params) {
            var msg = "Retrieving taxonomy codes";
            xhr('studySubmission/getHealthCodeTaxonomies?', myCallback, msg, 'application/json');
         },
         getTopLevelHealthCodes: function(myCallback, params) {
            var msg = "Retrieving top level health codes",
               args = 'healthCodeTaxonomyNameSpace=' + params[0];
            xhr('studySubmission/getTopLevelCodes?' + args, myCallback, msg, 'application/json');
         },
         getSubHealthCodes: function(myCallback, params) {
            var msg = "Retrieving sub level health codes",
               args = 'healthCode=' + params[1] + '&healthCodeNameSpace=' + params[0];
            xhr('studySubmission/getImmediateChildHealthCodes?' + args, myCallback, msg, 'application/json');
         },

         getYearRange: function(myCallback, params) {
            var msg = "Retrieving Years",
               numerator = params[0],
               args = 'numeratorTableName=' + numerator;
            xhr('studySubmission/getYearRange?' + args, myCallback, msg, 'application/json');
         },
         getSexes: function(myCallback, params) {
            var msg = "Retrieving Genders";;
            xhr('studySubmission/getSexes?', myCallback, msg, 'application/json');
         },

         getAgeGroups: function(myCallback, params) {
            var msg = "Retrieving Age groups",
               numerator = params[0],
               args = 'geographyName=SAHSU&numeratorTableName=' + numerator;
            xhr('studySubmission/getAgeGroups?' + args, myCallback, msg, 'application/json');
         },

         /*This may need to be amended: geoLevelSelectName? geoLevelToMapName? */
         getCovariates: function(myCallback, params) {
            var msg = "Retrieving Covariates",
               args = 'geoLevelSelectName=LEVEL1&geoLevelToMapName=LEVEL3';
            xhr('studySubmission/getCovariates?' + args, myCallback, msg, 'application/json');
         },

         getSearchHealthCodes: function(myCallback, params) {
            var msg = "Searching health codes",
               taxonomy = params[0],
               searchTxt = params[1],
               args = 'nameSpace=' + taxonomy + '&searchText=' + params[1] + '&isCaseSensitive=false';
            xhr('studySubmission/getHealthCodesMatchingSearchText?' + args, myCallback, msg, 'application/json');
         },

         getAvailableCalculationMethods: function(myCallback, params) {
            var msg = "Retrieving Calculation Methods";
            xhr('studySubmission/getAvailableCalculationMethods?', myCallback, msg, 'application/json');
         },

         getExtent: function(myCallback, params) {
            var msg = "Retrieving extent for geolevel  " + parmas[0],
               args = 'geoLevelSelectName=' + params[0];
            xhr('studySubmission/getGeoLevelFullExtent?' + args, myCallback, msg, 'application/json');
         },

         getFullExtent: function(myCallback, params) {
            var msg = "Retrieving full extent";
            xhr('studySubmission/getGeographyFullExtent?', myCallback, msg, 'application/json');
         },

         getTiles: function(myCallback, params) {
            var msg = "Retrieving Map tiles";
            xhr('studySubmission/getTilesGivenTile?' + params, myCallback, -1, 'application/json');
         }

      };

   RIF.utils.extend(requests, RIF);

}());