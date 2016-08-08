/* CONTROLLER to handle alert bars and notifications over whole application
 * This is on the main <body> scope in index.html
 * Subsequent contollers in data-ui-view are children
 * uses ngNotificationsBar.min and ngNotificationsBar.css
 */
angular.module("RIF")
        .controller('AlertCtrl', function ($scope, notifications) {
            $scope.hide = false;
            $scope.showError = function (msg) {
                notifications.showError({message: 'Error: ' + msg, hide: $scope.hide});
            };
            $scope.showWarning = function (msg) {
                notifications.showWarning({message: 'Warning: ' + msg, hide: $scope.hide});
            };
            $scope.showSuccess = function (msg) {
                notifications.showSuccess({message: 'Success: ' + msg, hide: $scope.hide});
            };

            $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
     //           console.log('resize root');
            });


            //$scope.showError("an error message");
            //$scope.showSuccess("a success message");
            //$scope.showWarning("a warning message");
        });