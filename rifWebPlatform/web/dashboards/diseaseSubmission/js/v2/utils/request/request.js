(function () {

  var baseUrl = 'http://localhost:8080/rifServices/studySubmission/';

  var c = function (myFunc, msg) {
      //IE compatible
      return function (error, status, json) {
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

    asynCall = function () {
      var args = Array.prototype.slice.call(arguments, 0),
        callback = args[1],
        mime = args[2] || "text/plain",
        parameters = args[0] + '&userID=' + RIF.user + '&geographyName=' + RIF.geography,
        url = baseUrl + parameters;
      $.ajax({
        url: url
      }).done(callback).error(function (jqXHR, textStatus, errorThrown) {
        var msg = "Something went wrong with the following service: <br/>" + url + '<br/><br/>' + textStatus + '<br/>' + errorThrown;
        statusBar(msg, true, 1);
      });
    },

    post = function (service, data) { //Use to asyncronously submit set up submission file to middleware
      var url = baseUrl + service;
      $.ajax({
        url: url,
        type: "POST",
        data: data,
        processData: false,
        contentType: false,
        /*success: function( data ) {
               var msg = " <h1>Your Study has been succesfully submitted<h1>" +
               "<h2>As soon as the results have been calculated you will be notfied, you can now close this window.</h2>";
               RIF.statusBar(msg, true, 'notify');
           }, 
           error: function() {
            console.log($.makeArray(arguments));
           },
           complete: function() {
            console.log($.makeArray(arguments));
          }*/
      });

      // This should be deleted if the succes callback works
      var msg = " <h1>Your Study has been succesfully submitted<h1>" +
        "<h2>As soon as the results have been calculated you will be notfied, you can now close this window.</h2>";
      RIF.statusBar(msg, true, 'notify');
    },

    callback = function (myFuncts, data) {
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

    xhr = function (url, clbk, msg, mime) {
      statusBar(msg, true);
      asynCall(url, c(clbk, msg), mime);
    },

    statusBar = function (msg, showHide, statusCode) {
      if (msg != -1) {
        RIF.statusBar(msg, showHide, statusCode);
      }
    },

    requests = {
      getGeneralRequest: function (url, myCallback) {
        xhr(url, myCallback);
        return {};
      },
      /*
       *
       * Disease submission specific
       *
       */
      getIsLoggedIn: (function (myCallback, params) {
        var msg = " Checking if already logged in.. ",
          userName = params[0],
          pw = params[1];
        xhr('isLoggedIn?', myCallback, msg);
      }),

      getLogOut: (function (myCallback, params) {
        var msg = " Checking if already logged in.. ";
        xhr('logout?', myCallback, msg);
      }),

      getHealthThemes: function (myCallback, params) {
        var msg = "Retrieving Health Themes ";
        xhr('getHealthThemes?', myCallback, msg);
      },

      getNumeratorDenominator: function (myCallback, params) {
        var msg = "Retrieving Numerator denominator pairs ",
          args = 'healthThemeDescription=' + params[0];
        xhr('getNumerator?' + args, myCallback, msg);
      },

      getSelectAt: function (myCallback, params) {
        var msg = "Retrieving geolevel resolutions ";
        xhr('getGeoLevelSelectValues?', myCallback, msg, 'application/json');
      },

      getResolutions: function (myCallback, params) {
        var msg = "Retrieving geolevel resolutions ",
          args = 'geoLevelSelectName=' + params[0];
        xhr('getGeoLevelViews?' + args, myCallback, msg, 'application/json');
      },


      getTableMapAreas: function (myCallback, params) {
        //geoLevelSelectName=LEVEL4&yMax=55.0122&xMax=-6.32507&yMin=54.6456&xMin=-6.68853
        var msg = "Retrieving Area ids for geolevel:" + params[0];
        args = [
          'geoLevelSelectName=' + params[0],
          'yMax=' + params[1]["ymax"],
          'yMin=' + params[1]["ymin"],
          'xMin=' + params[1]["xmin"],
          'xMax=' + params[1]["xmax"]
        ].join('&');

        xhr('getMapAreasForBoundaryRectangle?' + args, myCallback, msg, 'application/json');
      },

      getHealthTaxonomy: function (myCallback, params) {
        var msg = "Retrieving taxonomy codes";
        xhr('getHealthCodeTaxonomies?', myCallback, msg, 'application/json');
      },
      getTopLevelHealthCodes: function (myCallback, params) {
        var msg = "Retrieving top level health codes",
          args = 'healthCodeTaxonomyNameSpace=' + params[0];
        xhr('getTopLevelCodes?' + args, myCallback, msg, 'application/json');
      },
      getSubHealthCodes: function (myCallback, params) {
        var msg = "Retrieving sub level health codes",
          args = 'healthCode=' + params[1] + '&healthCodeNameSpace=' + params[0];
        xhr('getImmediateChildHealthCodes?' + args, myCallback, msg, 'application/json');
      },

      getYearRange: function (myCallback, params) {
        var msg = "Retrieving Years",
          numerator = params[0],
          args = 'numeratorTableName=' + numerator;
        xhr('getYearRange?' + args, myCallback, msg, 'application/json');
      },
      getSexes: function (myCallback, params) {
        var msg = "Retrieving Genders";;
        xhr('getSexes?', myCallback, msg, 'application/json');
      },

      getAgeGroups: function (myCallback, params) {
        var msg = "Retrieving Age groups",
          numerator = params[0],
          args = 'numeratorTableName=' + numerator;
        xhr('getAgeGroups?' + args, myCallback, msg, 'application/json');
      },

      /*This may need to be amended: geoLevelSelectName? geoLevelToMapName? */
      getCovariates: function (myCallback, params) {
        var msg = "Retrieving Covariates",
          selectAt = params[0],
          resolution = params[1],
          args = 'geoLevelSelectName=' + selectAt + '&geoLevelToMapName=' + resolution;
        xhr('getCovariates?' + args, myCallback, msg, 'application/json');
      },

      getSearchHealthCodes: function (myCallback, params) {
        var msg = "Searching health codes",
          taxonomy = params[0],
          searchTxt = params[1],
          args = 'nameSpace=' + taxonomy + '&searchText=' + params[1] + '&isCaseSensitive=false';
        xhr('getHealthCodesMatchingSearchText?' + args, myCallback, msg, 'application/json');
      },

      getAvailableCalculationMethods: function (myCallback, params) {
        var msg = "Retrieving Calculation Methods";
        xhr('getAvailableCalculationMethods?', myCallback, msg, 'application/json');
      },

      getExtent: function (myCallback, params) {
        var msg = "Retrieving extent for geolevel  " + parmas[0],
          args = 'geoLevelSelectName=' + params[0];
        xhr('getGeoLevelFullExtent?' + args, myCallback, msg, 'application/json');
      },

      getFullExtent: function (myCallback, params) {
        var msg = "Retrieving full extent";
        xhr('getGeographyFullExtent?', myCallback, msg, 'application/json');
      },

      getTiles: function (myCallback, params) {
        var msg = "Retrieving Map tiles";
        xhr('getTilesGivenTile?' + params, myCallback, -1, 'application/json');
      },

      submitStudy: function (jsonObj) {
        var blob = new Blob([JSON.stringify(jsonObj)], {
          type: "text/plain"
        });
        var formData = new FormData();
        formData.append("userID", RIF.user);
        formData.append("fileField", blob, "submissionFile.txt");
        post("submitStudy/", formData);
      },

    };

  RIF.utils.extend(requests, RIF);

}());