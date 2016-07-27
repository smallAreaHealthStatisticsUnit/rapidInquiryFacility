
angular.module("RIF")
        .constant('API', "http://localhost:8080/rifServices/studySubmission/")

        .factory('authInterceptor', ['$q', 'API', '$injector',
            function ($q, API, $injector) {
                return {
                    request: function (config) {
                        //to reject any outgoing requests

                        return config;
                    },
                    response: function (res) {
                        //called with the response object from the server
                        if (res.data[0].errorMessages) {
                            //not logged in redirect to login page
                            console.log(res.data[0].errorMessages);
                            console.log(res.status);
 
                        //    $injector.get('$state').transitionTo('state0');
                        }
                        return res;
                    },
                    requestError: function (rejection) {

                        return $q.reject(rejection);
                    },
                    responseError: function (rejection) {
                        //for non-200 errors

                        return $q.reject(rejection);
                    }
                };
            }])
        .config(function ($httpProvider) {
            $httpProvider.interceptors.push('authInterceptor');
        })

        .service('user', ['$http', 'API',
            function ($http, API) {
                var self = this;
                self.currentUser = "";
                self.login = function (username, password) {
                    //http://localhost:8080/rifServices/studySubmission/login?userID=kgarwood&password=kgarwood
                    //[{"result":"User kgarwood logged in."}]
                    self.currentUser = username;
                    return $http.get(API + 'login?userID=' + username + '&password=' + password);
                };
                self.logout = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/logout?userID=kgarwood
                    //[{"result":"User kgarwood logged out."}]
                    self.currentUser = "";
                    return $http.get(API + 'logout?userID=' + username);
                };
                self.isLoggedIn = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/isLoggedIn?userID=kgarwood
                    //[{"result":"true"}]
                    return $http.get(API + 'isLoggedIn?userID=' + username);
                };
                //TODO: where to put this?
                self.getGeographies = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getGeographies?userID=kgarwood
                    //[{"names":["EW01","SAHSU","UK91"]}]
                    return $http.get(API + 'getGeographies?userID=' + username);
                };
                self.getHealthThemes = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getHealthThemes?userID=kgarwood&geographyName=SAHSU
                    //[{"name":"SAHSULAND","description":"SAHSU land cancer incidence example data"}]
                    return $http.get(API + 'getHealthThemes?userID=' + username + '&geographyName=' + geography);
                };
                self.getFractions = function (username, geography, healthThemeDescription) {
                    //http://localhost:8080/rifServices/studySubmission/getNumerator?userID=kgarwood&geographyName=SAHSU&healthThemeDescription=SAHSU%20land%20cancer%20incidence%20example%20data
                    //[{"numeratorTableName":"SAHSULAND_CANCER","numeratorTableDescription":"Cancer cases in SAHSU land","denominatorTableName":"SAHSULAND_POP","denominatorTableDescription":"SAHSU land population"}]
                    return $http.get(API + 'getNumerator?userID=' + username + '&geographyName=' + geography + "&healthThemeDescription=" + healthThemeDescription);
                };
                self.getProjects = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getProjects?userID=kgarwood
                    //[{"name":"TEST","description":null}]
                    return $http.get(API + 'getProjects?userID=' + username);
                };
                self.getYears = function(username, geography, numeratorTableName) {
                    //http://localhost:8080/rifServices/studySubmission/getYearRange?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    //[{"lowerBound":"1989","upperBound":"1996"}]
                    return $http.get(API + 'getYearRange?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numeratorTableName);
                };

            }])

        .controller('TabCtrl', ['$scope', 'user', '$injector',
            function ($scope, user, $injector) {
                $scope.username = user.currentUser;

                function handleLogout(res) {
                    $injector.get('$state').transitionTo('state0');
                }
                $scope.logout = function () {
                    user.logout(user.currentUser).then(handleLogout, handleLogout);
                };
            }])

        .controller('LoginCtrl', ['$scope', 'user', '$injector',
            function ($scope, user, $injector) {

                function handleLogin(res) {
                    if (res.data[0].result === "User " + $scope.username + " logged in.") {
                        $injector.get('$state').transitionTo('state1');
                    } else {
                        //login failed
                        $scope.showError("Could not login. Please check username and password");
                    }
                }
                function handleServerError(res) {
                    console.log("server error");
                    $scope.showError("Server error");
                }
                $scope.login = function () {
                    user.login($scope.username, $scope.password).then(handleLogin, handleServerError);
                };
            }]);