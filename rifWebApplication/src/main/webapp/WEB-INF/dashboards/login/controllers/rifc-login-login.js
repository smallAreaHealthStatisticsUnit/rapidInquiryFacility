/* 
 * CONTROLLER for login 
 */

angular.module("RIF")
        .controller('LoginCtrl', ['$scope', 'user', '$injector', 
            'SubmissionStateService', 'StudyAreaStateService', 'CompAreaStateService', 'ParameterStateService', 'StatsStateService',
            function ($scope, user, $injector, 
                    SubmissionStateService, StudyAreaStateService, CompAreaStateService, ParameterStateService, StatsStateService) {

                $scope.username = "kgarwood";
                $scope.password = "kgarwood";

                $scope.login = function () {
                    //check if already logged on
                    //(TODO: bookmark)In development, bypass password
                    //user.isLoggedIn($scope.username).then(handleLoginCheck, handleServerError);
                    user.login($scope.username, $scope.password).then(handleLogin, handleServerError);
                };
                function handleLoginCheck(res) {
                    //if logged in already then logout
                    if (res.data[0].result === "true") {
                        user.logout($scope.username).then(callLogin, handleServerError);
                    } else {
                        callLogin();
                    }
                }
                function callLogin() {
                    //log the user in
                    user.login($scope.username, $scope.password).then(handleLogin, handleLogin);
                }
                function handleLogin(res) {
                    if (res.data[0].result === "User " + $scope.username + " logged in.") {
                        $injector.get('$state').transitionTo('state1');

                        //reset all services
                        SubmissionStateService.resetState();
                        StudyAreaStateService.resetState();
                        CompAreaStateService.resetState();
                        ParameterStateService.resetState();
                        StatsStateService.resetState();

                        //initialise the taxonomy service
                        user.initialiseService().then(handleInitialise, handleInitialiseError);
                        function handleInitialise(res) {
                           // console.log("taxonomy initialised");
                        }
                        function handleInitialiseError(e) {
                            $scope.showError('Could not initialise the taxonomy service');
                        }                       
                    } else {
                        //login failed
                        $scope.showError("Could not login. Please check username and password");
                    }
                }
                function handleServerError(res) {
                    $scope.showError("Could not login. Please check username and password");
                    console.log("login error");
                }
            }]);