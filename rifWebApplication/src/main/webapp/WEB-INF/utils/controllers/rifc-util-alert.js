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
 * CONTROLLER to handle alert bars and notifications over whole application
 */
angular.module("RIF")
        .controller('AlertCtrl', function ($scope, notifications) {
            $scope.delay = 0; // mS
			$scope.lastMessage = undefined;
			$scope.messageList = [];
			$scope.messageCount = undefined;
			$scope.messageStart = new Date().getTime();
			
            //uses ngNotificationsBar.min and ngNotificationsBar.css
            $scope.showErrorNoHide = function (msg) {
				if (angular.isUndefined($scope.lastMessage)) {
                    $scope.messageCount = 0;
				}
				angular.copy(($scope.messageCount++));
				if (angular.isUndefined($scope.lastMessage) || $scope.lastMessage != msg) {
                    notifications.showError({message: 'Error: ' + msg, hideDelay: $scope.delay, hide: false});
					var end=new Date().getTime();
					var elapsed=(Math.round((end - $scope.messageStart)/100))/10; // in S	
					console.log("+" + elapsed + ": [" + $scope.messageCount + "] ERROR: " + msg);
				}
                $scope.lastMessage = angular.copy(msg);
				var msgList = $scope.messageList;
				msgList.push(msg); 
				$scope.messageList = angular.copy(msgList);
            };			
            $scope.showError = function (msg) {
				if (angular.isUndefined($scope.lastMessage)) {
                    $scope.messageCount = 0;
				}
				angular.copy(($scope.messageCount++));
				if (angular.isUndefined($scope.lastMessage) || $scope.lastMessage != msg) {
					notifications.showError({message: 'Error: ' + msg, hideDelay: $scope.delay, hide: true});
					var end=new Date().getTime();
					var elapsed=(Math.round((end - $scope.messageStart)/100))/10; // in S	
					console.log("+" + elapsed + ": [" + $scope.messageCount + "] ERROR: " + msg);
                }
                $scope.lastMessage = angular.copy(msg);
				var msgList = $scope.messageList;
				msgList.push(msg); 
				$scope.messageList = angular.copy(msgList);
            };
            $scope.showWarning = function (msg) {
				if (angular.isUndefined($scope.lastMessage)) {
                    $scope.messageCount = 0;
				}
				angular.copy(($scope.messageCount++));
				if (angular.isUndefined($scope.lastMessage) || $scope.lastMessage != msg) {
					notifications.showWarning({message: 'Warning: ' + msg, hideDelay: $scope.delay, hide: true});
					var end=new Date().getTime();
					var elapsed=(Math.round((end - $scope.messageStart)/100))/10; // in S	
					console.log("+" + elapsed + ": [" + $scope.messageCount + "] WARNING: " + msg);
				}
                $scope.lastMessage = angular.copy(msg);
				var msgList = $scope.messageList;
				msgList.push(msg); 
				$scope.messageList = angular.copy(msgList);
            };
            $scope.showSuccess = function (msg) {
				if (angular.isUndefined($scope.lastMessage)) {
                    $scope.messageCount = 0;
				}
				angular.copy(($scope.messageCount++));
				if (angular.isUndefined($scope.lastMessage) || $scope.lastMessage != msg) {
					notifications.showSuccess({message: 'Success: ' + msg, hideDelay: $scope.delay, hide: true});
					var end=new Date().getTime();
					var elapsed=(Math.round((end - $scope.messageStart)/100))/10; // in S	
					console.log("+" + elapsed + ": [" + $scope.messageCount + "] SUCCESS: " + msg);
				}
                $scope.lastMessage = angular.copy(msg);
				var msgList = $scope.messageList;
				msgList.push(msg); 
				$scope.messageList = angular.copy(msgList);
            };
            $scope.showWarningNoHide = function (msg) {
				if (angular.isUndefined($scope.lastMessage)) {
                    $scope.messageCount = 0;
				}
				angular.copy(($scope.messageCount++));
				if (angular.isUndefined($scope.lastMessage) || $scope.lastMessage != msg) {
					notifications.showWarning({message: 'Warning: ' + msg, hideDelay: $scope.delay, hide: false});
					var end=new Date().getTime();
					var elapsed=(Math.round((end - $scope.messageStart)/100))/10; // in S	
					console.log("+" + elapsed + ": [" + $scope.messageCount + "] WARNING: " + msg);
				}
                $scope.lastMessage = angular.copy(msg);
				var msgList = $scope.messageList;
				msgList.push(msg); 
				$scope.messageList = angular.copy(msgList);
            };
            $scope.showSuccessNoHide = function (msg) {
				if (angular.isUndefined($scope.lastMessage)) {
                    $scope.messageCount = 0;
				}
				angular.copy(($scope.messageCount++));
				if (angular.isUndefined($scope.lastMessage) || $scope.lastMessage != msg) {
					notifications.showSuccess({message: 'Success: ' + msg, hideDelay: $scope.delay, hide: false});
					var end=new Date().getTime();
					var elapsed=(Math.round((end - $scope.messageStart)/100))/10; // in S	
					console.log("+" + elapsed + ": [" + $scope.messageCount + "] SUCCESS: " + msg);
				}
                $scope.lastMessage = angular.copy(msg);
				var msgList = $scope.messageList;
				msgList.push(msg); 
				$scope.messageList = angular.copy(msgList);
            };
        });