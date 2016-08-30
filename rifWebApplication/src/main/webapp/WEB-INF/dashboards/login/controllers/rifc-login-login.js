/* CONTROLLER for login
 * 
 * TODO: username etc on parent scope, 
 * TODO: details on <form>
 * 
 */

angular.module("RIF")
        .controller('LoginCtrl', ['$scope', 'user', '$injector',
            function ($scope, user, $injector) {
                $scope.login = function () {
                    //check if already logged on
                    //user.isLoggedIn($scope.username).then(handleLoginCheck, handleServerError);
                    
                    //(TODO: bookmark)In development, bypass password
                    user.login($scope.username, $scope.password).then(handleLogin, handleServerError);
                };              
                function handleLoginCheck(res) {
                    //if logged in already then logout
                    if (res.data[0].result === "true") {
                        user.logout($scope.username).then(callLogin, handleServerError);
                    }
                    else {
                        callLogin();
                    }               
                }              
                function callLogin() {
                    //log the user in
                    user.login($scope.username, $scope.password).then(handleLogin, handleServerError);
                }
                function handleLogin(res) {
                    if (res.data[0].result === "User " + $scope.username + " logged in.") {
                        $injector.get('$state').transitionTo('state1');
                        
                        //TODO: reset the model and trees to defaults in states
                        
                    } else {
                        //login failed
                        $scope.showError("Could not login. Please check username and password");
                    }
                }
                function handleServerError(res) {
                    console.log("login error");
                }
            }]);