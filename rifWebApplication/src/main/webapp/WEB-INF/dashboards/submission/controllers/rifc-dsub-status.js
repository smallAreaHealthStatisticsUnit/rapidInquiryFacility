/* 
 * CONTROLLER for disease submission status modal
 */

//              
//              Study state - C: created, not verfied; V: verified, but no other work done;
//              E - extracted imported or created, but no results or maps created; 
//              R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.

angular.module("RIF")
        .controller('ModalStatusCtrl', ['$scope', '$uibModal', 'user', 'uiGridConstants',
            function ($scope, $uibModal, user, uiGridConstants) {

                $scope.statusTableOptions = {
                    enableFiltering: true,
                    enableColumnResizing: true,
                    enableRowHeaderSelection: false,
                    enableHorizontalScrollbar: 0,
                    rowHeight: 25,
                    rowTemplate: rowTemplate(),
                    columnDefs: [
                        {
                            name: 'study_id', enableHiding: false, type: 'number', width: 75,
                            sort: {
                                direction: uiGridConstants.DESC,
                                priority: 0
                            }
                        },
                        {name: 'study_name', enableHiding: false, width: 150},
                        {name: 'study_description', enableHiding: false, width: 125},
                        {name: 'message', enableHiding: false, width: "*"},
                        {name: 'date', enableHiding: false, width: 150},
                        {name: 'study_state', enableHiding: false, width: 100}
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };

                function rowTemplate() {
                    return  '<div id="testdiv" tabindex="0">' +
                            '<div style="height: 100%" ng-class="{ ' +
                            //'statusC: study_state===C,' + //C: created, not verfied
                            //'statusV: study_state===V,' + //V: verified, but no other work done;
                            //'statusE: study_state===E,' + //E: extracted imported or created
                            'statusR: study_state===R,' + //R: results computed
                            //'statusU: study_state===U' + //U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.
                            '}">' +
                            '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                            '</div>';
                }

                user.getCurrentStatusAllStudies(user.currentUser).then(function (res) {
                    $scope.summary = res.data.smoothed_results;
                    $scope.statusTableOptions.data = $scope.summary;
                }, handleStudyError);

                function handleStudyError(e) {
                    $scope.showError("Could not retrieve study status");
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