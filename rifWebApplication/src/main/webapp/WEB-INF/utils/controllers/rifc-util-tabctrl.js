/* 
 * CONTROLLER to handle tab transitions, logout and alert on new study completion
 */
angular.module("RIF")
        .controller('TabCtrl', ['$scope', 'user', '$injector', '$uibModal', '$interval', '$rootScope',
            function ($scope, user, $injector, $uibModal, $interval, $rootScope) {

                //The user to display
                $scope.username = user.currentUser;

                //Check for update in status
                var stop;
                var studies;
                $scope.studyIds;

                stop = $interval(function () {
                    user.getCurrentStatusAllStudies(user.currentUser).then(function (res) {
                        studies = res.data.smoothed_results;
                        var check = [];
                        for (var i = 0; i < studies.length; i++) {
                            if (studies[i].study_state === "R") {
                                check.push(studies[i].study_id);
                            }
                        }
                        $scope.studyIds = angular.copy(check);
                    }, function (e) {
                        //console.log("Could not retrieve study status");
                    });
                }, 4000);

                $scope.$on('$destroy', function () {
                    if (!angular.isUndefined(stop)) {
                        $interval.cancel(stop);
                        stop = undefined;
                    }
                });

                $scope.$watchCollection('studyIds', function (newNames, oldNames) {
                    if (!angular.isUndefined(oldNames)) {
                        if (newNames === oldNames) {
                            return;
                        }
                        var s = arrayDifference(newNames, oldNames);
                        var name = "";
                        for (var i = 0; i < studies.length; i++) {
                            if (studies[i].study_id === s[0]) {
                                name = studies[i].study_name;
                                break;
                            }
                        }
                        $scope.showSuccess("Study " + s + " - " + name + " has been processed");
                        //update study lists in other tabs
                        $rootScope.$broadcast('updateStudyDropDown', {study_id: s[0], name: name});
                    }
                });

                function arrayDifference(source, target) {
                    return source.filter(function (current) {
                        return target.indexOf(current) === -1;
                    });
                }

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


