/* CONTROLLER for login
 * 
 * TODO: username etc on parent scope, 
 * TODO: details on <form>
 * 
 */
/* global L */

angular.module("RIF")
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