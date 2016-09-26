/* CONTROLLER to handle alert bars and notifications over whole application
 * This is on the main <body> scope in index.html
 * Subsequent contollers in data-ui-view are children
 * uses ngNotificationsBar.min and ngNotificationsBar.css
 */
angular.module("RIF")
        .controller('AlertCtrl', function ($scope, notifications) {
            $scope.hide = true;
            $scope.delay = 5000;
            
            $scope.showError = function (msg) {
                notifications.showError({message: 'Error: ' + msg, hideDelay: $scope.delay, hide: $scope.hide});
            };
            $scope.showWarning = function (msg) {
                notifications.showWarning({message: 'Warning: ' + msg, hideDelay: $scope.delay, hide: $scope.hide});
            };
            $scope.showSuccess = function (msg) {
                notifications.showSuccess({message: 'Success: ' + msg, hideDelay: $scope.delay, hide: $scope.hide});
            };
        });