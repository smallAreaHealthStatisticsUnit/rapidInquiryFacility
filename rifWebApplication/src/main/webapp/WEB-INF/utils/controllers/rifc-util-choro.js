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
 * CONTROLLER for choropleth map symbology modal used by viewer and mapper
 */
/* global d3 */

angular.module("RIF")
        .controller('ChoroplethModalCtrl', ['$scope', '$uibModal', 'ChoroService', 'ColorBrewerService',
            function ($scope, $uibModal, ChoroService, ColorBrewerService) {

                $scope.open = function (map) {
                    //if there are no studies, do not show
                    if (angular.isUndefined($scope.studyID[map])) {
                        return;
                    }

                    //do not allow modal to open if map in full screen
                    if ($scope.$parent.map[map].isFullscreen()) {
                        return;
                    }

                    //Brewer swatches obtained from https://github.com/timothyrenner/ColorBrewer.jl
                    $scope.$parent.child.options = [];
                    var colorBrewerList = ColorBrewerService.getSchemeList();
                    for (var j in colorBrewerList) {
                        $scope.$parent.child.options.push({name: colorBrewerList[j], image: 'images/colorBrewer/' + colorBrewerList[j] + '.png'});
                    }

                    $scope.$parent.child.mapID = map;
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-choro.html',
                        controller: 'ChoroplethModalInstanceCtrl',
                        windowClass: 'mapping-Modal',
                        scope: $scope.$parent.child
                    });
                    
                    modalInstance.result.then(function (modal) {
						var mapChoroSettings = {
							brewerName: modal.currOption.name,
							invert: modal.checkboxInvert,
							brewer: ColorBrewerService.getColorbrewer(modal.currOption.name, modal.selectedN),
							intervals: modal.selectedN,
							feature: modal.selectedFeature,
							method: modal.method,
							renderer: modal.thisMap
						};
                        ChoroService.getMaps(map).brewerName = mapChoroSettings.brewerName;
                        ChoroService.getMaps(map).invert = mapChoroSettings.invert;
                        ChoroService.getMaps(map).brewer = mapChoroSettings.brewer;
                        ChoroService.getMaps(map).intervals = mapChoroSettings.intervals;
                        ChoroService.getMaps(map).feature = mapChoroSettings.feature;
                        ChoroService.getMaps(map).method = mapChoroSettings.method;
                        ChoroService.getMaps(map).renderer = mapChoroSettings.renderer;
//						$scope.consoleDebug('rifc-util-choro.js() mapChoroSettings for map: ' + map  + "; " + JSON.stringify(mapChoroSettings, null, 2));
                        $scope.$parent.child.refresh(map);
                    });
                };
            }])
        .controller('ChoroplethModalInstanceCtrl', function ($scope, $uibModalInstance, ColorBrewerService, ChoroService) {
            $scope.input = {};
            $scope.input.checkboxInvert = ChoroService.getMaps($scope.mapID).invert;
            $scope.input.selectedSchemeName = ChoroService.getMaps($scope.mapID).brewerName;
            $scope.input.intervalRange = ColorBrewerService.getSchemeIntervals($scope.input.selectedSchemeName);
            $scope.input.selectedN = ChoroService.getMaps($scope.mapID).intervals;
            $scope.input.method = ChoroService.getMaps($scope.mapID).method;
			$scope.input.methodObj = {id: $scope.input.method};
			$scope.input.classifications = ChoroService.getClassifications();

            //set saved swatch selection
            var cb = ChoroService.getMaps($scope.mapID).brewerName;
            for (var i = 0; i < $scope.options.length; i++) {
                if ($scope.options[i].name === cb) {
                    $scope.input.currOption = $scope.options[i];
                }
            }

            //list of attributes
            $scope.input.features = ChoroService.getMaps($scope.mapID).features;
            if ($scope.input.features.indexOf(ChoroService.getMaps($scope.mapID).feature) === -1) {
                $scope.input.selectedFeature = $scope.input.features[0];
            } else {
                $scope.input.selectedFeature = ChoroService.getMaps($scope.mapID).feature;
            }

            //Map renderer on opening
            var onXRenderRestore = angular.copy(ChoroService.getMaps($scope.mapID));

            $scope.domain = [];

            $scope.renderSwatch = function (
				bOnOpen /* Called on modal open */, 
				bCalc /* Secret field, always true */) {
				var rval=ChoroService.doRenderSwatch( // rval is $scope.input.thisMap
					bOnOpen 	/* Called on modal open */, 
					bCalc 		/* Secret field, always true */, 
					$scope, 
					ColorBrewerService);
				if (rval) {
					$scope.input.thisMap=rval;
				}
				$scope.consoleDebug("[rifc-util-choro.js] renderSwatch $scope.input: " + 
					JSON.stringify($scope.input, null, 2));
				
				// Set brewerName and intervals are per getChoroScale() return values
				if ($scope.input.thisMap.brewerName) {
					ChoroService.getMaps($scope.mapID).brewerName=$scope.input.thisMap.brewerName;
				}
				if ($scope.input.thisMap.intervals) {
					$scope.input.selectedN=$scope.input.thisMap.intervals;
				}	
				if ($scope.input.thisMap.invert && $scope.input.thisMap.invert == true) {
					$scope.input.checkboxInvert=$scope.input.thisMap.invert;
				}
				else {
					$scope.input.checkboxInvert=false;	
				}				
				if ($scope.input.thisMap.selectedFeature) { // This does not work!
					$scope.consoleDebug("[rifc-util-choro.js] renderSwatch selectedFeature: " + 
						$scope.input.thisMap.selectedFeature);
//					ChoroService.getMaps($scope.mapID).feature=$scope.input.thisMap.selectedFeature;
					$scope.input.selectedFeature=$scope.input.thisMap.selectedFeature;
				}
				//set swatch selection
				var cb = ChoroService.getMaps($scope.mapID).brewerName;
				for (var i = 0; i < $scope.options.length; i++) {
					if ($scope.options[i].name === cb) {
						$scope.input.currOption = $scope.options[i];
					}
				}				
            };
            
            //ensure modal fields are filled
            $scope.renderSwatch(true, true);

            $scope.close = function () {
                //reset to what was there on modal open  
                ChoroService.getMaps($scope.mapID).renderer = onXRenderRestore.renderer;
                ChoroService.getMaps($scope.mapID).brewerName = onXRenderRestore.brewerName;
				
				ChoroService.getMaps($scope.mapID).invert = onXRenderRestore.invert;
				ChoroService.getMaps($scope.mapID).intervals = onXRenderRestore.intervals;
				ChoroService.getMaps($scope.mapID).method = onXRenderRestore.method;

				//set saved swatch selection
				var cb = ChoroService.getMaps($scope.mapID).brewerName;
				for (var i = 0; i < $scope.options.length; i++) {
					if ($scope.options[i].name === cb) {
						$scope.input.currOption = $scope.options[i];
					}
				}

				//list of attributes
				$scope.input.features = ChoroService.getMaps($scope.mapID).features;
				if ($scope.input.features.indexOf(ChoroService.getMaps($scope.mapID).feature) === -1) {
					$scope.input.selectedFeature = $scope.input.features[0];
				} else {
					$scope.input.selectedFeature = ChoroService.getMaps($scope.mapID).feature;
				}				
                $uibModalInstance.dismiss();
            };

            $scope.choroReset = function () {
				var features=ChoroService.getMaps($scope.mapID).features; // Save feature list
				ChoroService.resetState($scope.mapID);
				ChoroService.getMaps($scope.mapID).features=features;
				$scope.input = {};
				$scope.input.checkboxInvert = ChoroService.getMaps($scope.mapID).invert;
				$scope.input.selectedSchemeName = ChoroService.getMaps($scope.mapID).brewerName;
				$scope.input.intervalRange = ColorBrewerService.getSchemeIntervals($scope.input.selectedSchemeName);
				$scope.input.selectedN = ChoroService.getMaps($scope.mapID).intervals;
				$scope.input.method = ChoroService.getMaps($scope.mapID).method;
				$scope.input.methodObj = {id: $scope.input.method};
				$scope.input.classifications = ChoroService.getClassifications();
				
				var cb = ChoroService.getMaps($scope.mapID).brewerName;
				for (var i = 0; i < $scope.options.length; i++) { 
					if ($scope.options[i].name === cb) {
						$scope.input.currOption = $scope.options[i];
					}
				}
				var brewer = ColorBrewerService.getColorbrewer($scope.input.currOption.name,	
					$scope.input.selectedN);	
				var feature = ChoroService.getMaps($scope.mapID).feature;
				$scope.input.features = ChoroService.getMaps($scope.mapID).features;

				$scope.domain = [];				
				for (var i = 0; i < $scope.tableData[$scope.mapID].length; i++) {
					$scope.domain.push(Number($scope.tableData[$scope.mapID][i][feature]));
				}			

                $scope.consoleDebug("[rifc-util-choro.js] Reset: " + $scope.mapID);
					
				var oldRval;
				var intervals=$scope.input.selectedN;
				if (ChoroService.getMaps($scope.mapID).renderer) {
					oldRval=ChoroService.getMaps($scope.mapID).renderer;
				}
				$scope.input.thisMap = ChoroService.getChoroScale($scope.input.method, $scope.domain, 
					brewer, $scope.input.checkboxInvert, $scope.mapID, 
					oldRval, intervals, $scope);
				 
				// Set renderer, brewer
				$scope.input.renderer = ChoroService.getMaps($scope.mapID).renderer;
				$scope.input.brewer = ChoroService.getMaps($scope.mapID).brewer;
				$scope.input.selectedFeature = feature; // Will force a re-render						
			}
			
            $scope.apply = function () {
                //check breaks are numeric               
                for (var i = 0; i < $scope.input.thisMap.breaks.length; i++) {
                    var thisBreak = Number($scope.input.thisMap.breaks[i]);
                    if (!isNaN(thisBreak)) {
                        $scope.input.thisMap.breaks[i] = thisBreak;
                    } else {
                        $scope.showWarning("Non-numeric break value entered");
                        return;
                    }
                }

                //check breaks are sequential
                var tmp = angular.copy($scope.input.thisMap.breaks);
                tmp.push($scope.input.thisMap.mx);
                tmp.unshift($scope.input.thisMap.mn);
                for (var i = 0; i < tmp.length - 1; i++) {
                    if (tmp[i] > tmp[i + 1]) {
                        $scope.showWarning("Breaks are not in ascending order");
                        return;
                    }
                }
				
				// You could check if the breaks have been changed!
											
                //apply any user made changes to breaks
                $scope.input.thisMap.scale = d3.scaleThreshold()
                        .domain($scope.input.thisMap.breaks)
                        .range($scope.input.thisMap.range);

                $uibModalInstance.close($scope.input);
            };
        });