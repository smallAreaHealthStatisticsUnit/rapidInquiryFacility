
angular.module("RIF")
        .controller('BaseMapModalCtrl', ['$scope', '$uibModal', 'LeafletBaseMapService',
            function ($scope, $uibModal, LeafletBaseMapService) {
                //List of all layers by name for drop-down fill
                $scope.myBaseMaps = LeafletBaseMapService.getBaseMapList();
                $scope.selectedBaseMap = LeafletBaseMapService.getCurrentBase();
                $scope.checkboxBaseMap = LeafletBaseMapService.getNoBaseMap();

                //Update selected layer from drop-down         
                $scope.setSelected = function (option) {
                    LeafletBaseMapService.setCurrentBase(option);
                };
                //Using basemap?
                $scope.setNoBaseMapCheck = function (option) {
                    LeafletBaseMapService.setNoBaseMap(option);
                };
                //Open the modal
                $scope.open = function (id) {
                    $scope.id = id; //<leaflet id = "id">
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-basemap.html',
                        controller: 'BaseMapModalInstanceCtrl',
                        windowClass: 'mapping-Modal'
                    });
                    modalInstance.result.then(function () {
                        $scope.parent.renderMap($scope.id);
                    });
                };
            }])
        .controller('BaseMapModalInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.apply = function () {
                $uibModalInstance.close();
            };
        });

