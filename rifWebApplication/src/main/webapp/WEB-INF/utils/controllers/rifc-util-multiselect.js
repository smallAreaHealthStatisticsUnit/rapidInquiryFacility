/* 
 * CONTROLLER to re-enable multiple selections on ui-grids
 */
angular.module("RIF")
        .controller('MultiSelectCtrl', ['$scope', 'GridService', function ($scope, GridService) {
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
                        var bFound = false;
                        if (angular.isDefined($scope.selectedPolygon)) {
                            //working on the study areas
                            for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                if ($scope.selectedPolygon[i].id === thisPolyID) {
                                    bFound = true;
                                    $scope.selectedPolygon.splice(i, 1);
                                    break;
                                }
                            }
                            if (!bFound) {
                                $scope.selectedPolygon.push({id: thisPolyID, gid: thisPolyID, label: thisPoly, band: $scope.currentBand});
                            }
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
                                for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                                    if ($scope.selectedPolygon[j].id === thisPolyID) {
                                        bFound = true;
                                        break;
                                    }
                                }
                                if (!bFound) {
                                    $scope.selectedPolygon.push({id: thisPolyID, gid: thisPolyID, label: thisPoly, band: $scope.currentBand});
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
                        }
                    }
                    multiStart = GridService.getMatchRowNumber(myVisibleRows, thisPolyID);
                };
            }]);