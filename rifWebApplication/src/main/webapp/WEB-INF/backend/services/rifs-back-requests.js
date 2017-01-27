/*
 * SERVICE for all requests to the middleware
 */
angular.module("RIF")
        .constant('studySubmissionURL', "http://localhost:8080/rifServices/studySubmission/")
        .constant('studyResultRetrievalURL', "http://localhost:8080/rifServices/studyResultRetrieval/")
        .constant('taxonomyServicesURL', "http://localhost:8080/taxonomyServices/taxonomyServices/")

        .service('user', ['$http', 'studySubmissionURL', 'studyResultRetrievalURL', 'taxonomyServicesURL',
            function ($http, studySubmissionURL, studyResultRetrievalURL, taxonomyServicesURL) {
                var self = this;
                self.currentUser = "";

                //identify specific middleware calls in the interceptor
                var config = {
                    headers: {
                        "rifUser": "rif"
                    }
                };

                //submit a study               
                self.submitStudy = function (username, jsonObj) {
                    var blob = new Blob([JSON.stringify(jsonObj)], {
                        type: "text/plain"
                    });

                    var formData = new FormData();
                    formData.append("userID", username);
                    formData.append("fileField", blob, "submissionFile.txt");
                    formData.append("fileFormat", "JSON");

                    return $http.post(studySubmissionURL + "submitStudy/", formData, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    });
                };

                //login
                self.login = function (username, password) {
                    //http://localhost:8080/rifServices/studySubmission/login?userID=kgarwood&password=xyz
                    //[{"result":"User kgarwood logged in."}]
                    self.currentUser = username;
                    return $http.get(studySubmissionURL + 'login?userID=' + username + '&password=' + password);
                };
                self.logout = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/logout?userID=kgarwood
                    //[{"result":"User kgarwood logged out."}]
                    self.currentUser = "";
                    return $http.get(studySubmissionURL + 'logout?userID=' + username);
                };
                self.isLoggedIn = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/isLoggedIn?userID=kgarwood
                    //[{"result":"true"}]
                    return $http.get(studySubmissionURL + 'isLoggedIn?userID=' + username);
                };
                //Taxonomy services
                self.initialiseService = function () {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/initialiseService
                    //true
                    return $http.get(taxonomyServicesURL + 'initialiseService', config);
                };
                self.getTaxonomyServiceProviders = function () {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/getTaxonomyServiceProviders
                    //[{"identifier":"icd10","name":"ICD Taxonomy Service","description":"ICD 10 is a classification of diseases."}]
                    return $http.get(taxonomyServicesURL + 'getTaxonomyServiceProviders', config);
                };
                self.getRootTerms = function () {
                    return $http.get(taxonomyServicesURL + 'getRootTerms?taxonomy_id=icd10', config);
                };
                self.getMatchingTerms = function (taxonomy, text) {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/getMatchingTerms?taxonomy_id=icd10&search_text=asthma&is_case_sensitive=false
                    return $http.get(taxonomyServicesURL + 'getMatchingTerms?taxonomy_id=' + taxonomy + '&search_text=' + text + '&is_case_sensitive=false', config);
                };
                //Project info
                self.getProjects = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getProjects?userID=kgarwood
                    //[{"name":"TEST","description":null}]
                    return $http.get(studySubmissionURL + 'getProjects?userID=' + username, config);
                };
                self.getProjectDescription = function (username, projectName) {
                    //http://localhost:8080/rifServices/studySubmission/getProjectDescription?userID=kgarwood&projectName=TEST
                    //[{"result":"Test Project. Will be disabled when in production."}]
                    return $http.get(studySubmissionURL + 'getProjectDescription?userID=' + username + '&projectName=' + projectName, config);
                };
                self.getGeographies = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getGeographies?userID=kgarwood
                    //[{"names":["EW01","SAHSU","UK91"]}]
                    return $http.get(studySubmissionURL + 'getGeographies?userID=' + username, config);
                };
                self.getHealthThemes = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getHealthThemes?userID=kgarwood&geographyName=SAHSU
                    //[{"name":"SAHSULAND","description":"SAHSU land cancer incidence example data"}]
                    return $http.get(studySubmissionURL + 'getHealthThemes?userID=' + username + '&geographyName=' + geography, config);
                };
                self.getFractions = function (username, geography, healthThemeDescription) {
                    //http://localhost:8080/rifServices/studySubmission/getNumerator?userID=kgarwood&geographyName=SAHSU&healthThemeDescription=SAHSU%20land%20cancer%20incidence%20example%20data
                    //[{"numeratorTableName":"SAHSULAND_CANCER","numeratorTableDescription":"Cancer cases in SAHSU land","denominatorTableName":"SAHSULAND_POP","denominatorTableDescription":"SAHSU land population"}]
                    return $http.get(studySubmissionURL + 'getNumerator?userID=' + username + '&geographyName=' + geography + "&healthThemeDescription=" + healthThemeDescription, config);
                };
                //Investigation parameters
                self.getYears = function (username, geography, numeratorTableName) {
                    //http://localhost:8080/rifServices/studySubmission/getYearRange?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    //[{"lowerBound":"1989","upperBound":"1996"}]
                    return $http.get(studySubmissionURL + 'getYearRange?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numeratorTableName, config);
                };
                self.getSexes = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getSexes?userID=kgarwood
                    //[{"names":["Males","Females","Both"]}]
                    return $http.get(studySubmissionURL + 'getSexes?userID=' + username, config);
                };
                self.getCovariates = function (username, geography, geoLevel) {
                    //http://localhost:8080/rifServices/studySubmission/getCovariates?userID=kgarwood&geographyName=SAHSU&geoLevelToMapName=LEVEL4
                    return $http.get(studySubmissionURL + 'getCovariates?userID=' + username + '&geographyName=' + geography + '&geoLevelToMapName=' + geoLevel, config);
                };
                self.getAgeGroups = function (username, geography, numerator) {
                    //http://localhost:8080/rifServices/studySubmission/getAgeGroups?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    return $http.get(studySubmissionURL + 'getAgeGroups?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numerator, config);
                };
                //geography
                self.getTiles = function (username, geography, geoLevel, leaflet) {
                    //http://localhost:8080/rifServices/studySubmission/getTiles?userID=kgarwood&geographyName=SAHSU&geoLevelSelectName=LEVEL1&tileIdentifier=4&zoomFactor=2&
                    //yMax=55.5268097&xMax=-4.88653803&yMin=52.6875343&xMin=-7.58829451

                    //TODO: what is tileIdentifier?? 1979_1321 in rifServices/src/test/java/rifServices/test/services/GetTiles.java 

                    config.leaflet = leaflet; //defines which map is target for these tiles
                    return $http.get(studySubmissionURL + 'getTiles?userID=' + username + '&geographyName=' + geography + '&geoLevelSelectName=' + geoLevel +
                            '&tileIdentifier=4&zoomFactor=2&yMax=55.5268097&xMax=-4.88653803&yMin=52.6875343&xMin=-7.58829451', config);
                };
                //geography info
                self.getDefaultGeoLevelSelectValue = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getDefaultGeoLevelSelectValue?userID=kgarwood&geographyName=SAHSU
                    //[{"names":["LEVEL2"]}]
                    return $http.get(studySubmissionURL + 'getDefaultGeoLevelSelectValue?userID=' + username + '&geographyName=' + geography, config);
                };
                self.getGeoLevelViews = function (username, geography, geoLevelSelectName) {
                    //http://localhost:8080/rifServices/studySubmission/getGeoLevelViews?userID=kgarwood&geographyName=SAHSU&geoLevelSelectName=LEVEL2
                    //[{"names":["LEVEL2","LEVEL3","LEVEL4"]}]
                    return $http.get(studySubmissionURL + 'getGeoLevelViews?userID=' + username + '&geographyName=' + geography +
                            '&geoLevelSelectName=' + geoLevelSelectName, config);
                };
                self.getGeoLevelSelectValues = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getGeoLevelSelectValues?userID=kgarwood&geographyName=SAHSU
                    //[{"names":["LEVEL1","LEVEL2","LEVEL3","LEVEL4"]}]
                    return $http.get(studySubmissionURL + 'getGeoLevelSelectValues?userID=' + username + '&geographyName=' + geography, config);
                };
                //statistical methods
                self.getAvailableCalculationMethods = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getAvailableCalculationMethods?userID=kgarwood
                    //[{"codeRoutineName":"car_r_procedure","prior":"Standard deviation","description":"Applies ...
                    return $http.get(studySubmissionURL + 'getAvailableCalculationMethods?userID=' + username, config);
                };
                //status
                self.getCurrentStatusAllStudies = function (username) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getCurrentStatusAllStudies?userID=kgarwood              
                    //{"smoothed_results_header":["study_id","study_name","study_description","study_state","message","date"]
                    return $http.get(studyResultRetrievalURL + 'getCurrentStatusAllStudies?userID=' + username, config);
                };
                //results for viewer
                self.getSmoothedResults = function (username, studyID, sex) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getSmoothedResults?userID=kgarwood&studyID=1&sex=1
                    //{"smoothed_results_header":["area_id","band_id","genders","observed","...
                    return $http.get(studyResultRetrievalURL + 'getSmoothedResults?userID=' + username + '&studyID=' + studyID + '&sex=' + sex, config);
                };
                self.getAllPopulationPyramidData = function (username, studyID, year) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getAllPopulationPyramidData?userID=kgarwood&studyID=1&year=1990
                    //{smoothed_results_header:{population_label,males,females}smoothed_results:{{population_la...
                    return $http.get(studyResultRetrievalURL + 'getAllPopulationPyramidData?userID=' + username + '&studyID=' + studyID + '&year=' + year, config);
                };
                //get info on a completed study
                self.getYearsForStudy = function (username, studyID, leaflet) {
                    config.leaflet = leaflet;
                    //http://localhost:8080/rifServices/studyResultRetrieval/getYearsForStudy?userID=kgarwood&study_id=1
                    //{"years{":["1989","1990","1991","1992","1993","1994","1995","1996"]}
                    return $http.get(studyResultRetrievalURL + 'getYearsForStudy?userID=' + username + '&study_id=' + studyID, config);
                };
                self.getSexesForStudy = function (username, studyID, leaflet) {
                    config.leaflet = leaflet;
                    //http://localhost:8080/rifServices/studyResultRetrieval/getSexesForStudy?userID=kgarwood&study_id=1
                    //[{"names":["Males","Females","Both"]}]
                    return $http.get(studyResultRetrievalURL + 'getSexesForStudy?userID=' + username + '&study_id=' + studyID, config);
                };
                self.getGeographyAndLevelForStudy = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getGeographyAndLevelForStudy?userID=kgarwood&studyID=239
                    //[["SAHSU","LEVEL3"]]
                    return $http.get(studyResultRetrievalURL + 'getGeographyAndLevelForStudy?userID=' + username + '&studyID=' + studyID);
                };
            }]);