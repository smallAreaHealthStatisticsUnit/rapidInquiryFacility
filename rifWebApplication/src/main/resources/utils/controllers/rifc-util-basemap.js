/**
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 
 * David Morley
 * @author dmorley
 */

/*
 * CONTROLLER for basemap selection modal
 */

angular.module("RIF")
        .controller('BaseMapModalCtrl', ['$scope', '$uibModal', 'LeafletBaseMapService', 'AlertService',
            function ($scope, $uibModal, LeafletBaseMapService, AlertService) {
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
                    var myScope = $scope.$parent.child;
                    //do not allow modal to open if map in full screen
                    if (id === "areamap") {
                        myScope = $scope.$parent;
                        if (myScope[id].isFullscreen()) {
                            return;
                        }
                    } else {
                        if (myScope.map[id].isFullscreen()) {
                            return;
                        }
                    }
                    myScope.id = id;

                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-basemap.html',
                        controller: 'BaseMapModalInstanceCtrl',
                        windowClass: 'osm-Modal',
                        scope: myScope
                    });
                    modalInstance.result.then(function () {
						var currentBaseMapInUse=LeafletBaseMapService.getCurrentBaseMapInUse(myScope.id);
						AlertService.consoleDebug("[rifc-util-basemap.js] selectedBaseMap " + myScope.id + ": " + currentBaseMapInUse);
                        myScope.renderMap(myScope.id, currentBaseMapInUse);
                    });
                };
            }])
        .controller('BaseMapModalInstanceCtrl', ['$scope', '$uibModalInstance', 'LeafletBaseMapService', 'AlertService', 
			function ($scope, $uibModalInstance, LeafletBaseMapService, AlertService) {
            $scope.input = {};
            $scope.input.selectedBaseMap = LeafletBaseMapService.getCurrentBaseMapInUse($scope.id);
            $scope.input.checkboxBaseMap = LeafletBaseMapService.getNoBaseMap($scope.id);
//			AlertService.consoleDebug("[rifc-util-basemap.js] selectedBaseMap: " + $scope.input.selectedBaseMap +
//				"; checkboxBaseMap: " + $scope.input.checkboxBaseMap);
									
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.apply = function () {
                $uibModalInstance.close();
            };
        }]);

