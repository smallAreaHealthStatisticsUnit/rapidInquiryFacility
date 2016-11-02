/*
 * SERVICE for all requests to the middleware
 */
angular.module("RIF")
        .constant('rifAPI1', "http://localhost:8080/rifServices/studySubmission/")
        .constant('rifAPI2', "http://localhost:8080/rifServices/studyResultRetrieval/")
        .constant('taxonomyAPI', "http://localhost:8080/taxonomyServices/taxonomyServices/")

        .service('user', ['$http', 'rifAPI1', 'rifAPI2', 'taxonomyAPI',
            function ($http, rifAPI1, rifAPI2, taxonomyAPI) {
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

                    return $http.post(rifAPI1 + "submitStudy/", formData, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    });
                };
                
                //login
                self.login = function (username, password) {
                    //http://localhost:8080/rifServices/studySubmission/login?userID=kgarwood&password=xyz
                    //[{"result":"User kgarwood logged in."}]
                    self.currentUser = username;
                    return $http.get(rifAPI1 + 'login?userID=' + username + '&password=' + password);
                };
                self.logout = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/logout?userID=kgarwood
                    //[{"result":"User kgarwood logged out."}]
                    self.currentUser = "";
                    return $http.get(rifAPI1 + 'logout?userID=' + username);
                };
                self.isLoggedIn = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/isLoggedIn?userID=kgarwood
                    //[{"result":"true"}]
                    return $http.get(rifAPI1 + 'isLoggedIn?userID=' + username);
                };
                //Taxonomy services
                self.initialiseService = function () {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/initialiseService
                    //true
                    return $http.get(taxonomyAPI + 'initialiseService', config);
                };
                self.getTaxonomyServiceProviders = function () {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/getTaxonomyServiceProviders
                    //[{"identifier":"icd10","name":"ICD Taxonomy Service","description":"ICD 10 is a classification of diseases."}]
                    return $http.get(taxonomyAPI + 'getTaxonomyServiceProviders', config);
                };
                self.getRootTerms = function () {
                    return $http.get(taxonomyAPI + 'getRootTerms?taxonomy_id=icd10', config);
                };
                self.getMatchingTerms = function (taxonomy, text) {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/getMatchingTerms?taxonomy_id=icd10&search_text=asthma&is_case_sensitive=false
                    return $http.get(taxonomyAPI + 'getMatchingTerms?taxonomy_id=' + taxonomy + '&search_text=' + text + '&is_case_sensitive=false', config);
                };
                //Project info
                self.getProjects = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getProjects?userID=kgarwood
                    //[{"name":"TEST","description":null}]
                    return $http.get(rifAPI1 + 'getProjects?userID=' + username, config);
                };
                self.getGeographies = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getGeographies?userID=kgarwood
                    //[{"names":["EW01","SAHSU","UK91"]}]
                    return $http.get(rifAPI1 + 'getGeographies?userID=' + username, config);
                };
                self.getHealthThemes = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getHealthThemes?userID=kgarwood&geographyName=SAHSU
                    //[{"name":"SAHSULAND","description":"SAHSU land cancer incidence example data"}]
                    return $http.get(rifAPI1 + 'getHealthThemes?userID=' + username + '&geographyName=' + geography, config);
                };
                self.getFractions = function (username, geography, healthThemeDescription) {
                    //http://localhost:8080/rifServices/studySubmission/getNumerator?userID=kgarwood&geographyName=SAHSU&healthThemeDescription=SAHSU%20land%20cancer%20incidence%20example%20data
                    //[{"numeratorTableName":"SAHSULAND_CANCER","numeratorTableDescription":"Cancer cases in SAHSU land","denominatorTableName":"SAHSULAND_POP","denominatorTableDescription":"SAHSU land population"}]
                    return $http.get(rifAPI1 + 'getNumerator?userID=' + username + '&geographyName=' + geography + "&healthThemeDescription=" + healthThemeDescription, config);
                };
                //Investigation parameters
                self.getYears = function (username, geography, numeratorTableName) {
                    //http://localhost:8080/rifServices/studySubmission/getYearRange?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    //[{"lowerBound":"1989","upperBound":"1996"}]
                    return $http.get(rifAPI1 + 'getYearRange?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numeratorTableName, config);
                };
                self.getSexes = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getSexes?userID=kgarwood
                    //[{"names":["Males","Females","Both"]}]
                    return $http.get(rifAPI1 + 'getSexes?userID=' + username, config);
                };
                self.getCovariates = function (username, geography, geoLevel) {
                    //http://localhost:8080/rifServices/studySubmission/getCovariates?userID=kgarwood&geographyName=SAHSU&geoLevelToMapName=LEVEL4
                    return $http.get(rifAPI1 + 'getCovariates?userID=' + username + '&geographyName=' + geography + '&geoLevelToMapName=' + geoLevel, config);
                };
                self.getAgeGroups = function (username, geography, numerator) {
                    //http://localhost:8080/rifServices/studySubmission/getAgeGroups?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    return $http.get(rifAPI1 + 'getAgeGroups?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numerator, config);
                };
                //geography
                self.getTiles = function (username, geography, geoLevel) {
                    return $http.get(rifAPI1 + 'getTiles?userID=' + username + '&geographyName=' + geography + '&geoLevelSelectName=' + geoLevel +
                            '&tileIdentifier=4&zoomFactor=2&yMax=55.5268097&xMax=-4.88653803&yMin=52.6875343&xMin=-7.58829451', config);
                };
                //geography info
                self.getDefaultGeoLevelSelectValue = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getDefaultGeoLevelSelectValue?userID=kgarwood&geographyName=SAHSU
                    //[{"names":["LEVEL2"]}]
                    return $http.get(rifAPI1 + 'getDefaultGeoLevelSelectValue?userID=' + username + '&geographyName=' + geography, config);
                };
                self.getGeoLevelViews = function (username, geography, geoLevelSelectName) {
                    //http://localhost:8080/rifServices/studySubmission/getGeoLevelViews?userID=kgarwood&geographyName=SAHSU&geoLevelSelectName=LEVEL2
                    //[{"names":["LEVEL2","LEVEL3","LEVEL4"]}]
                    return $http.get(rifAPI1 + 'getGeoLevelViews?userID=' + username + '&geographyName=' + geography +
                            '&geoLevelSelectName=' + geoLevelSelectName, config);
                };
                self.getGeoLevelSelectValues = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getGeoLevelSelectValues?userID=kgarwood&geographyName=SAHSU
                    //[{"names":["LEVEL1","LEVEL2","LEVEL3","LEVEL4"]}]
                    return $http.get(rifAPI1 + 'getGeoLevelSelectValues?userID=' + username + '&geographyName=' + geography, config);
                };
                //statistical methods
                self.getAvailableCalculationMethods = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getAvailableCalculationMethods?userID=kgarwood
                    //[{"codeRoutineName":"car_r_procedure","prior":"Standard deviation","description":"Applies ...
                    return $http.get(rifAPI1 + 'getAvailableCalculationMethods?userID=' + username, config);
                };
                //status
                self.getStudySummaries = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getStudySummaries?userID=kgarwood
                    //[{"studyID":"4","studyName":"05: Attempting to change the state (<var>=><var>) of a ...
                    return $http.get(rifAPI1 + 'getStudySummaries?userID=' + username, config);
                };
                //results for viewer
                self.getSmoothedResults = function (username, studyID, sex, year) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getSmoothedResults?userID=kgarwood&studyID=1&sex=1&year=1990
                    return $http.get(rifAPI2 + 'getSmoothedResults?userID=' + username + '&studyID=' + studyID +
                            '&sex=' + sex + '&year=' + year, config);
                };
                self.getSmoothedResultsForAttributes = function (username, studyID, sex, year, smoothedAttribute1, smoothedAttribute2) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getSmoothedResultsForAttributes?
                    //userID=kgarwood&studyID=1&sex=1&year=1990&smoothedAttribute=lower95&smoothedAttribute=upper95                    
                    return $http.get(rifAPI2 + 'getSmoothedResultsForAttributes?userID=' + username + '&studyID=' + studyID +
                            '&sex=' + sex + '&year=' + year + '&smoothedAttribute=' + smoothedAttribute1 + '&smoothedAttribute=' +
                            smoothedAttribute2, config);
                };
                self.getSmoothedResultAttributes = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getSmoothedResultAttributes?userID=kgarwood&studyID=1
                    //["gid","inv_id","band_id","lower95","upper95","relative_risk","smoothed_relative_risk"...
                    return $http.get(rifAPI2 + 'getSmoothedResultAttributes?userID=' + username + '&studyID=' + studyID, config);
                };
                self.getAllPopulationPyramidData = function (username, studyID, year) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getAllPopulationPyramidData?userID=kgarwood&studyID=1&year=1990
                    //{smoothed_results_header:{population_label,males,females}smoothed_results:{{population_la...
                    return $http.get(rifAPI2 + 'getAllPopulationPyramidData?userID=' + username + '&studyID=' + studyID + '&year=' + year, config);
                };
            }]);