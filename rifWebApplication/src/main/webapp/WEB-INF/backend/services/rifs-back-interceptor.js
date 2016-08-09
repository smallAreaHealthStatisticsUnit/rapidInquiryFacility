/* 
 * SERVICE $httpInterceptor to handle in and outgoing requests
 */
angular.module("RIF")
        .factory('authInterceptor', ['$q', '$injector',
            function ($q, $injector) {
                return {
                    request: function (config) {
                        //to reject any outgoing requests

                        return config;
                    },
                    response: function (res) {

                        //TODO: if res undefined

                        if (res.data[0] === undefined) {

                        } else {
                            //not logged in redirect to login page
                            // console.log(res.data[0].errorMessages);
                            // console.log(res.status);

                            //    $injector.get('$state').transitionTo('state0');
                        }


                        //called with the response object from the server

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
        });


