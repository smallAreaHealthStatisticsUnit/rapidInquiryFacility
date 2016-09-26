/* 
 * CONTROLLER to handle tab transitions and logout
 */
angular.module("RIF")
        .controller('TabCtrl', ['$scope', 'user', '$injector', '$uibModal',
            function ($scope, user, $injector, $uibModal) {
                $scope.username = user.currentUser;
                
                //yes-no modal for $scope.logout
                $scope.openLogout = function () {
                    $scope.modalHeader = "Log out";
                    $scope.modalBody = "Unsaved work will be lost. Are you sure?";
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-yesno.html',
                        controller: 'ModalLogoutYesNoInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        scope: $scope,
                        keyboard: false
                    });
                };
                
                $scope.logout = function () {
                    $scope.openLogout();
                };
                $scope.doLogout = function () {
                    user.logout(user.currentUser).then(handleLogout, handleLogout);
                };
                function handleLogout(res) {
                    $injector.get('$state').transitionTo('state0');
                }             
            }])
        .controller('ModalLogoutYesNoInstanceCtrl', 
            function ($scope, $uibModalInstance) {
                $scope.close = function () {
                    $uibModalInstance.dismiss();
                };
                $scope.submit = function () {
                    $scope.doLogout();
                    $uibModalInstance.close();
                };
        });


