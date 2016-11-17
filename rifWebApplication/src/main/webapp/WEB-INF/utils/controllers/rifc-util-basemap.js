
angular.module("RIF")
        .controller('BaseMapModalCtrl', ['$scope', '$uibModal', 'LeafletBaseMapService',
            function ($scope, $uibModal, LeafletBaseMapService) {
                //List of all layers by name for drop-down fill
                $scope.myBaseMaps = LeafletBaseMapService.getBaseMapList();
                
                //Update selected layer from drop-down         
                $scope.setSelected = function (option) {
                    LeafletBaseMapService.setCurrentBaseMapInUse($scope.id, option);
                };
                
                //Using basemap?
                $scope.setNoBaseMapCheck = function (option) {
                    LeafletBaseMapService.setNoBaseMap($scope.id, option);
                };
                
                //Open the modal
                $scope.open = function (id) {
                    $scope.id = id; //<leaflet id = "id">
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-basemap.html',
                        controller: 'BaseMapModalInstanceCtrl',
                        windowClass: 'osm-Modal',
                        scope: $scope
                    });
                    modalInstance.result.then(function () {
                        $scope.renderMap($scope.id);
                    });
                };
            }])
        .controller('BaseMapModalInstanceCtrl', function ($scope, $uibModalInstance, LeafletBaseMapService) {
            $scope.input = {};
            $scope.input.selectedBaseMap = LeafletBaseMapService.getCurrentBaseMapInUse($scope.id);
            $scope.input.checkboxBaseMap = LeafletBaseMapService.getNoBaseMap($scope.id);

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.apply = function () {
                $uibModalInstance.close();
            };
        });

