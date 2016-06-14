//https://github.com/emgeee/angular-jwt-auth-tutorial/blob/master/code/app.js
//https://djds4rce.wordpress.com/2013/08/13/understanding-angular-http-interceptors/
angular.module("RIF")
        .factory('authInterceptor', ['$q', 'API', 'auth',
            function ($q, API, auth) {
                return {
                    // automatically attach Authorization header
                    request: function (config) {
                        var token = auth.getToken();
                        if (config.url.indexOf(API) === 0 && token) {
                            config.headers.Authorization = 'Bearer ' + token;
                        }
                        return config;

                    },
                    // If a token was sent back, save it
                    response: function (res) {
                        if (res.config.url.indexOf(API) === 0 && res.data.token) {
                            console.log('here');
                            auth.saveToken(res.data.token);
                        }
                        return res;
                    },
                    responseError: function (rejection) {
                        if (rejection.status === 401) {
                            //$urlRouterProvider.otherwise("/login")
                            // 
                            // Return a new promise
                            //   return userService.authenticate().then(function () {
                            //       return $injector.get('$http')(rejection.config);
                            //   });
                        }

                        /* If not a 401, do nothing with this error.
                         * This is necessary to make a `responseError`
                         * interceptor a no-op. */
                        return $q.reject(rejection);
                    }
                };
            }])

        .service('user', ['$http', 'API', 'auth',
            function ($http, API, auth) {
                var self = this;

                self.register = function (username, password) {
                    return $http.post(API + '/auth/register', {
                        username: username,
                        password: password
                    });
                };

                self.login = function (username, password) {
                    return $http.post(API + '/auth/login', {
                        username: username,
                        password: password
                    });
                };

                self.someRequest = function () {
                    return $http.get('/api/first-item')
                            .success(function (data, status, headers, config) {
                                // write your code for successful request
                                // result object will be always valid.
                                console.log(data);
                            })
                            .error(function (data, status, headers, config) {
                                console.log(status); // will print 400 when data.data.status != 0
                            });
                };


            }])

        .service('auth', ['$window',
            function ($window) {
                var self = this;

                self.parseJwt = function (token) {
                    var base64Url = token.split('.')[1];
                    var base64 = base64Url.replace('-', '+').replace('_', '/');
                    return JSON.parse($window.atob(base64));
                };

                self.saveToken = function (token) {
                    $window.localStorage['jwtToken'] = token;
                };

                self.getToken = function () {
                    return $window.localStorage['jwtToken'];
                };

                self.isAuthed = function () {
                    var token = self.getToken();
                    if (token) {
                        var params = self.parseJwt(token);
                        return Math.round(new Date().getTime() / 1000) <= params.exp;
                    } else {
                        return false;
                    }
                };

                self.logout = function () {
                    $window.localStorage.removeItem('jwtToken');
                };

            }])

        .constant('API', 'http://test-routes.herokuapp.com')

        .config(function ($httpProvider) {
            $httpProvider.interceptors.push('authInterceptor');
        })

        .controller('MainCtrl', ['$scope', 'user', 'auth',
            function ($scope, user, auth) {
                var self = this;

                function handleRequest(res) {
                    console.log(res.status);
                    var token = res.data ? res.data.token : null;
                    if (token) {
                        console.log('JWT:', token);
                    } else {
                        $scope.showError("AUTH FAIL");
                        //direct to login page
                    }
                    $scope.message = res.data.message;
                }

                self.login = function () {
                    user.login(self.username, self.password)
                            .then(handleRequest, handleRequest);
                };
                self.register = function () {
                    user.register(self.username, self.password)
                            .then(handleRequest, handleRequest);
                };
                self.logout = function () {
                    auth.logout && auth.logout();
                };
                self.isAuthed = function () {
                    console.log(auth.isAuthed ? auth.isAuthed() : false);
                    return auth.isAuthed ? auth.isAuthed() : false;
                };
                //self.xyz make a server request
                //....




            }])
        ;