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
        .controller('PostalCodeSelect', ['$scope', 'GridService', 'uiGridConstants',
			function ($scope, GridService, uiGridConstants) {
                //Reference the parent scope
                var parentScope = $scope.$parent;
                parentScope.child = $scope;
	/*Code to change height		
				$scope.tableHeight = 'height: 600px';  
  
			function getTableHeight(totalPage, currentPage, pageSize, dataLen) {  
				var rowHeight = 30; // row height  
				var headerHeight = 50; // header height  
				var footerHeight = 60; // bottom scroll bar height  
				var totalH = 0;  
				if (totalPage > 1) {  
					if (currentPage < totalPage) {  
						totalH = pageSize * rowHeight + headerHeight + footerHeight;  
					} else {  
						var lastPageSize = dataLen % pageSize;  
						if (lastPageSize === 0) {  
							totalH = pageSize * rowHeight + headerHeight + footerHeight;  
						} else {  
							totalH = lastPageSize * rowHeight + headerHeight + footerHeight;  
						}  
					}  
					console.log(totalH);  
				} else {  
					totalH = dataLen * rowHeight + headerHeight + footerHeight;  
				}  
				return 'height: ' + (totalH) + 'px';  
			}  
			$interval(function() {  
				$scope.tableHeight = getTableHeight($scope.totalPage, $scope.currentPage, $scope.pageSize, $scope.gridOptions.data.length);  
				console.log("$scope.tableHeight: " + $scope.tableHeight);  
				$scope.gridApi.grid.handleWindowResize();  
				$scope.gridApi.core.refresh();  
			}, 200);  */

            }]);