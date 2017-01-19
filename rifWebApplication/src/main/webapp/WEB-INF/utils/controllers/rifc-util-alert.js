/* 
 * CONTROLLER to handle alert bars and notifications over whole application
 * uses ngNotificationsBar.min and ngNotificationsBar.css
 */
angular.module("RIF")
        .controller('AlertCtrl', function ($scope, notifications) {
            $scope.delay = 0;

            $scope.showError = function (msg) {
                //Limit to one error alert at a time
                var box = document.getElementsByClassName('error');
                if (box.length === 0) {
                    notifications.showError({message: 'Error: ' + msg, hideDelay: $scope.delay, hide: true});
                }
            };
            $scope.showWarning = function (msg) {
                notifications.showWarning({message: 'Warning: ' + msg, hideDelay: $scope.delay, hide: true});
            };
            $scope.showSuccess = function (msg) {
                notifications.showSuccess({message: 'Success: ' + msg, hideDelay: $scope.delay, hide: false});
            };
        });