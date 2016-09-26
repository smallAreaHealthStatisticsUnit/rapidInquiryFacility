/* 
 * CONTROLLER for disease submission status modal
 */
angular.module("RIF")
        .controller('ModalStatusCtrl', ['$scope', '$uibModal', 'user',
            function ($scope, $uibModal, user) {

                $scope.statusTableOptions = {
                    enableFiltering: true,
                    enableColumnResizing: true,
                    enableRowHeaderSelection: false,
                    enableHorizontalScrollbar: 1,
                    rowHeight: 25,
                    columnDefs: [
                        {name: 'studyID', enableHiding: false, width: 100},
                        {name: 'studyName', enableHiding: false},
                        {name: 'studySummary', enableHiding: false}
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };

                user.getStudySummaries(user.currentUser).then(function (res) {
                    $scope.summary = res.data;
                    $scope.statusTableOptions.data = $scope.summary;
                }, handleStudyError);

                function handleStudyError(e) {
                    $scope.showError("xCould not retrieve study status");
                }

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-status.html',
                        controller: 'ModalStatusInstanceCtrl',
                        windowClass: 'status-Modal',
                        backdrop: 'static',
                        keyboard: false
                    });
                };
            }])
        .controller('ModalStatusInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            };
        });