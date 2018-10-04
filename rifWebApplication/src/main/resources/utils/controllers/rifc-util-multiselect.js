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
 * CONTROLLER to re-enable multiple selections on ui-grids
 * selections on ui-grid are disabled to allow syncing to map
 * all selections are done using a $watch
 */
angular.module("RIF")
        .controller('MultiSelectCtrl', ['$scope', 'GridService', 'CommonMappingStateService', 'AlertService',
				function ($scope, GridService, CommonMappingStateService, AlertService) {
                //Reference the parent scope
                var parentScope = $scope.$parent;
                parentScope.child = $scope;

                var bShift = false;
                var multiStart = -1;
                var multiStop = -1;

                $scope.keyDown = function ($event) {
                    if (!bShift && $event.keyCode === 16) {
                        bShift = true;
                    }
                };
                //detect shift key (16) up
                $scope.keyUp = function ($event) {
                    if (bShift && $event.keyCode === 16) {
                        bShift = false;
                        multiStop = -1;
                    }
                };

                //reset if mouse not over grid
                $scope.mouseleave = function ($event) {
                    multiStart = -1;
                    multiStop = -1;
                };

                //Table click event to update selectedPolygon 
                $scope.rowClick = function (row) {
                    var myVisibleRows = $scope.gridApi.core.getVisibleRows();
                    var thisPoly = row.entity.label;
                    var thisPolyID = row.entity.area_id;

                    if (!bShift) {
                        //We are doing a single click select on the table
                        if (angular.isDefined($scope.selectedPolygon)) {
                            //working on the study areas
							if (CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, thisPolyID)) {
								AlertService.consoleDebug("[rifc-util-multiselect.js] remove: " + thisPolyID);
								$scope.selectedPolygon = CommonMappingStateService.getState("areamap").removeFromSselectedPolygon($scope.input.name,
									thisPolyID);
							}
                            else {
								var newSelectedPolygon = {id: thisPolyID, gid: thisPolyID, label: thisPoly, band: $scope.currentBand};
								AlertService.consoleDebug("[rifc-util-multiselect.js] add: " + thisPolyID +
									"; newSelectedPolygon: " + JSON.stringify(newSelectedPolygon));
								$scope.selectedPolygon = CommonMappingStateService.getState("areamap").addToSelectedPolygon($scope.input.name,
									newSelectedPolygon);
                            }
							$scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display

                        } else {
                            //working on the viewer
                            for (var i = 0; i < $scope.thisPoly.length; i++) {
                                if ($scope.thisPoly[i] === thisPolyID) {
                                    bFound = true;
                                    $scope.thisPoly.splice(i, 1);
                                    break;
                                }
                            }
                            if (!bFound) {
                                $scope.thisPoly.push(thisPolyID);
                            }
                        }
                    } else {
                        //We are doing a multiple select on the table, shift key is down
                        multiStop = GridService.getMatchRowNumber(myVisibleRows, row.entity.area_id);
                        //Check if the grid has lost focus
                        if (multiStart === -1) {
                            multiStart = multiStop;
                        }
                        //get the sequence
                        for (var i = Math.min(multiStop, multiStart);
                                i <= Math.min(multiStop, multiStart) + (Math.abs(multiStop - multiStart)); i++) {
                            var bFound = false;

                            if (angular.isDefined($scope.selectedPolygon)) {
                                //working on the study areas
                                var thisPoly = myVisibleRows[i].entity.label;
                                var thisPolyID = myVisibleRows[i].entity.area_id;
								
								if (!CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, thisPolyID)) {
									var newSelectedPolygon = {id: thisPolyID, gid: thisPolyID, label: thisPoly, band: $scope.currentBand};
									AlertService.consoleDebug("[rifc-util-multiselect.js] add[" + i + "]: " + thisPolyID +
										"; newSelectedPolygon: " + JSON.stringify(newSelectedPolygon));
									CommonMappingStateService.getState("areamap").addToSelectedPolygon($scope.input.name,
										newSelectedPolygon);
								}
							
                            } else {
                                //working on the viewer
                                thisPolyID = myVisibleRows[i].entity.area_id;
                                for (var j = 0; j < $scope.thisPoly.length; j++) {
                                    if ($scope.thisPoly[j] === thisPolyID) {
                                        bFound = true;
                                        break;
                                    }
                                }
                                if (!bFound) {
                                    $scope.thisPoly.push(thisPolyID);
                                }
                            }
                        } // End of for loop
						$scope.selectedPolygon = CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name);
						$scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display

                    }
                    multiStart = GridService.getMatchRowNumber(myVisibleRows, thisPolyID);
                };
            }]);