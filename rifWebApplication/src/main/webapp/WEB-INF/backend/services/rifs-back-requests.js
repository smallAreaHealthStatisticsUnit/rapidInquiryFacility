/*
 * SERVICE for all requests to the middleware
 * 
 */
angular.module("RIF")
        .constant('rifAPI', "http://localhost:8080/rifServices/studySubmission/")
        .constant('taxonomyAPI', "http://localhost:8080/taxonomyServices/taxonomyServices/")

        .service('user', ['$http', 'rifAPI', 'taxonomyAPI',
            function ($http, rifAPI, taxonomyAPI) {
                var self = this;
                self.currentUser = "";

                //login
                self.login = function (username, password) {
                    //http://localhost:8080/rifServices/studySubmission/login?userID=kgarwood&password=xyz
                    //[{"result":"User kgarwood logged in."}]
                    self.currentUser = username;
                    return $http.get(rifAPI + 'login?userID=' + username + '&password=' + password);
                };
                self.logout = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/logout?userID=kgarwood
                    //[{"result":"User kgarwood logged out."}]
                    self.currentUser = "";
                    return $http.get(rifAPI + 'logout?userID=' + username);
                };
                self.isLoggedIn = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/isLoggedIn?userID=kgarwood
                    //[{"result":"true"}]
                    return $http.get(rifAPI + 'isLoggedIn?userID=' + username);
                };

                //Taxonomy services
                self.initialiseService = function () {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/initialiseService
                    //true
                    return $http.get(taxonomyAPI + 'initialiseService');
                };
                self.getTaxonomyServiceProviders = function() {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/getTaxonomyServiceProviders
                    //[{"identifier":"icd10","name":"ICD Taxonomy Service","description":"ICD 10 is a classification of diseases."}]
                    return $http.get(taxonomyAPI + 'getTaxonomyServiceProviders');
                };
                self.getRootTerms = function () {
                    return $http.get(taxonomyAPI + 'getRootTerms?taxonomy_id=icd10');
                };
                self.getMatchingTerms = function (taxonomy, text) {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/getMatchingTerms?taxonomy_id=icd10&search_text=asthma&is_case_sensitive=false
                    return $http.get(taxonomyAPI + 'getMatchingTerms?taxonomy_id=' + taxonomy + '&search_text=' + text + '&is_case_sensitive=false');
                };

                //Project info
                self.getProjects = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getProjects?userID=kgarwood
                    //[{"name":"TEST","description":null}]
                    return $http.get(rifAPI + 'getProjects?userID=' + username);
                };
                self.getGeographies = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getGeographies?userID=kgarwood
                    //[{"names":["EW01","SAHSU","UK91"]}]
                    return $http.get(rifAPI + 'getGeographies?userID=' + username);
                };
                self.getHealthThemes = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getHealthThemes?userID=kgarwood&geographyName=SAHSU
                    //[{"name":"SAHSULAND","description":"SAHSU land cancer incidence example data"}]
                    return $http.get(rifAPI + 'getHealthThemes?userID=' + username + '&geographyName=' + geography);
                };
                self.getFractions = function (username, geography, healthThemeDescription) {
                    //http://localhost:8080/rifServices/studySubmission/getNumerator?userID=kgarwood&geographyName=SAHSU&healthThemeDescription=SAHSU%20land%20cancer%20incidence%20example%20data
                    //[{"numeratorTableName":"SAHSULAND_CANCER","numeratorTableDescription":"Cancer cases in SAHSU land","denominatorTableName":"SAHSULAND_POP","denominatorTableDescription":"SAHSU land population"}]
                    return $http.get(rifAPI + 'getNumerator?userID=' + username + '&geographyName=' + geography + "&healthThemeDescription=" + healthThemeDescription);
                };

                //Investigation parameters
                self.getYears = function (username, geography, numeratorTableName) {
                    //http://localhost:8080/rifServices/studySubmission/getYearRange?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    //[{"lowerBound":"1989","upperBound":"1996"}]
                    return $http.get(rifAPI + 'getYearRange?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numeratorTableName);
                };
                self.getSexes = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getSexes?userID=kgarwood
                    //[{"names":["Males","Females","Both"]}]
                    return $http.get(rifAPI + 'getSexes?userID=' + username);
                };
                self.getCovariates = function (username, geography, geoLevel) {
                    //http://localhost:8080/rifServices/studySubmission/getCovariates?userID=kgarwood&geographyName=SAHSU&geoLevelToMapName=LEVEL4
                    return $http.get(rifAPI + 'getCovariates?userID=' + username + '&geographyName=' + geography + '&geoLevelToMapName=' + geoLevel);
                };
                self.getAgeGroups = function (username, geography, numerator) {
                    //http://localhost:8080/rifServices/studySubmission/getAgeGroups?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    return $http.get(rifAPI + 'getAgeGroups?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numerator);
                };
                
                //geography
                self.getTiles = function() {
                    return $http.get(rifAPI + 'getTiles?userID=kgarwood&geographyName=SAHSU&geoLevelSelectName=LEVEL2&tileIdentifier=4&zoomFactor=2&yMax=55.5268097&xMax=-4.88653803&yMin=52.6875343&xMin=-7.58829451');
                };
                


            }]);