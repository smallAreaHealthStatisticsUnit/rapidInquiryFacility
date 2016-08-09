/* 
 * CONTROLLER to handle tab transitions and logout
 */
angular.module("RIF")
        .controller('TabCtrl', ['$scope', 'user', '$injector',
            function ($scope, user, $injector) {
                $scope.username = user.currentUser;

                function handleLogout(res) {
                    $injector.get('$state').transitionTo('state0');
                }
                $scope.logout = function () {
                    user.logout(user.currentUser).then(handleLogout, handleLogout);
                };
                
            }]);


