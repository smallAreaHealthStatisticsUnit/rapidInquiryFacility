
angular.module("RIF")
        .controller('BaseMapModalCtrl', ['$scope', '$uibModal', 'leafletData', 'LeafletBaseMapService',
            function ($scope, $uibModal, leafletData, LeafletBaseMapService) {
                //List of all layers by name for drop-down fill
                $scope.myBaseMaps = LeafletBaseMapService.get_baseMapList();
                $scope.selectedBaseMap = LeafletBaseMapService.get_currentBase();
                $scope.checkboxBaseMap = LeafletBaseMapService.get_noBaseMap();

                //Update selected layer from drop-down         
                $scope.setSelected = function (option) {
                    LeafletBaseMapService.set_currentBase(option);
                };
                //Using basemap?
                $scope.setNoBaseMapCheck = function (option) {
                    LeafletBaseMapService.set_noBaseMap(option);
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
                        leafletData.getMap($scope.id).then(function (map) {
                            //store setView
                            //TODO: refactor and on main navbar change
                            LeafletBaseMapService.set_currentZoomLevel(map.getZoom());
                            LeafletBaseMapService.set_currentCentre(map.getCenter());
                        });
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

