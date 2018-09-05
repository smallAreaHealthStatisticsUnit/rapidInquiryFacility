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

 * Peter Hambly
 * @author phambly
 */

/* 
 * CONTROLLER for disease submission messages modal
 */

angular.module("RIF")
        .controller('ModalMessagesCtrl', ['$scope', '$uibModal', 'user', 'uiGridConstants', 'AlertService',
            function ($scope, $uibModal, user, uiGridConstants, AlertService) {
				var testError = new Error("Test Error");
				
                $scope.messagesTableOptions = {
                    enableFiltering: true,
                    enableColumnResizing: true,
//                  enableRowHeaderSelection: false,
					enableHiding: false,
					enableCellEdit: false,
                    enableHorizontalScrollbar: 0,  
					enableFullRowSelection: true,
//					enableRowSelection: true, 
//					enableSelectAll: false,
//					multiSelect: false,  
					enableGridMenu: false,
                    rowHeight: 25,				
					appScopeProvider: { 
						onClickTrace: function(row) {//For error only
							if (row.error) {
								openError(row);	
							}
						}
					},
                    rowTemplate: rowTemplate(),
                    columnDefs: [
						{name: 'time', enableHiding: false, width: 60},
                        {name: 'message', enableHiding: false, width: "*"},
                        {name: 'error', enableHiding: false, width: 48, enableFiltering: false,
								cellTemplate: '<div><button ng-if="row.entity.error" ng-click="grid.appScope.onClickTrace(row.entity)">View</button></div>'}
                    ],
					data: [
						{time: "+1.234", messageLevel: "INFO", message: "Test Message", error: undefined},
						{time: "+1.234", messageLevel: "ERROR", message: "Error Message", error: testError},
						{time: "+1.234", messageLevel: "WARNING", message: "Warning Message", error: undefined},
						{time: "+1.234", messageLevel: "SUCCESS", message: "Success Message", error: undefined}
					],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    } 
                };
			
				$scope.messagesTableOptions.data=AlertService.getMessageList();
				
                function rowTemplate() {
                    return  '<div style="height: 100%" ng-class="{ ' +
                            "statusINFO: row.entity.messageLevel==='INFO'," + 
                            "statusERROR: row.entity.messageLevel==='ERROR'," + 
                            "statusWARNING: row.entity.messageLevel==='WARNING'," + 
                            "statusSUCCESS: row.entity.messageLevel==='SUCCESS'," + 
                            '}">' +
                            '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                            '</div>';
                } 
				
				function openError(row) {
					$scope.traceModalInstance=$uibModal.open({
						animation: true,
						templateUrl: 'dashboards/submission/partials/rifp-dsub-error.html',
						controller: 'ModalErrorInstanceCtrl',
						windowClass: 'error-Modal',
						keyboard: false,
						resolve: {
							getError: function() {			
								return '<table style="width:100%" class="errorTable">' +
									'  <tr>' +
									'    <td>Time</td>' +
									'    <td>' + row.time + ' S</td>' +
									'  </tr>' +
									'  <tr>' +
									'    <td>Message Level</td>' +
									'    <td>' + row.messageLevel + '</td>' +
									'  </tr>' +
									'  <tr>' +
									'    <td>Message</td>' +
									'    <td>' + (row.message || "No message") + '</td>' +
									'  </tr>' +
									'  <tr>' +
									'    <td>Error Message</td>' +
									'    <td>' + (row.error.message || "No error message") + '</td>' +
									'  </tr>' +
									'  <tr>' +
									'    <td>Error stack</td>' +
									'    <td><pre>' + (row.error.stack || "No error stack") + '</pre></td>' +
									'  </tr>' +
									'</table>';
							}
						},
					    scope: $scope 
					});				
				}
				
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-messages.html',
                        controller: 'ModalMessagesInstanceCtrl',
                        windowClass: 'messages-Modal',
                        keyboard: false
                    });
                };
						
            }])
        .controller('ModalMessagesInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            }; 
        });