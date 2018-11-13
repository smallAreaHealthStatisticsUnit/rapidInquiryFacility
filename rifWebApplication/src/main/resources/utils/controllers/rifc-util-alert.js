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
        .controller('AlertCtrl', ['$scope', 'notifications', 'user', 'ParametersService', 'AlertService',
			function ($scope, notifications, user, ParametersService, AlertService) {
            $scope.delay = 0; // mS
			$scope.lastMessage = undefined;
			$scope.messageList = [];
			$scope.messageCount = undefined;
			$scope.messageStart = new Date().getTime();

			$scope.parameters=ParametersService.getParameters()||{debugEnabled: false} ;	
					
			var level = {
				error: 		"ERROR",
				warning: 	"WARNING",
				success:	"SUCCESS",	// For compatibility with ngNotificationsBar; mapped to INFO in middleware
				info:		"INFO",
				debug: 		"DEBUG"
			};
								
			/*
			 * Function: 	isIE()
			 * Parameters: 	None
			 * Returns: 	Nothing
			 * Description:	Test for IE nightmare 
			 */
			function isIE() {
				var myNav = navigator.userAgent.toLowerCase();
				return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
			}

			/*
			 * Function: 	get_browser_info()
			 * Parameters: 	None
			 * Object: { Returns: 	name, version }
			 * Description:	Modified from: https://www.gregoryvarghese.com/how-to-get-browser-name-and-version-via-javascript/
			 */
			function get_browser_info() {
				var ua=navigator.userAgent,tem,M=ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || []; 
				if (/trident/i.test(M[1])){
					tem=/\brv[ :]+(\d+)/g.exec(ua) || []; 
					return {
						name: 'IE ',
						version: (tem[1]||'')
					};
				}   
				if (M[1]==='Chrome'){
					tem=ua.match(/\bOPR\/(\d+)/)
					if(tem!=null) {
						return {
							name:'Opera', 
							version:tem[1]
						};
					}
				}   
				M=M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];
				if ((tem=ua.match(/version\/(\d+)/i))!=null) {
					M.splice(1,1,tem[1]);
				}
				return {
				  name: (M[0] || "UNK"),
				  version: (M[1] || "UNK")
				};
			}
 
			/*
			 * Function: 	callRifFrontEndLogger()
			 * Parameters: 	messageType,
			 *				message, 
			 *				errorMessage,
			 *				errorStack,
			 *				relativeTime
			 * Returns: 	Nothing
			 * Description:	Call rifFrontEndLogger service log to middleware
			 */
			function callRifFrontEndLogger(
				messageType,
				message, 
				errorMessage,
				errorStack,
				relativeTime) {
					
				var browser=get_browser_info();
				var end = new Date();
				var actualTime = end.toLocaleDateString() + " " + end.toLocaleTimeString();

				try {
					user.rifFrontEndLogger(
						(user.currentUser || "NOUSER"), 
						(messageType || "NOMESSAGETYPE"),
						(browser ? browser.name + "; v" + browser.version : "UNKNOWN"), // Browser type
						(message ? message.substring(0, 300) : "NOMESSAGE"), // Limit to 300 characters
						(errorMessage ? errorMessage.substring(0, 300) : undefined), // Limit to 300 characters
						(errorStack ? errorStack.substring(0, 400) : undefined), // Limit to 400 characters,
						actualTime,
	//					actualTime.toDateString() + "; " + actualTime.toTimeString(),
						relativeTime).then(function (res) {
							if (res.data[0].result != "OK") {
								if (window.console && console && console.log && typeof console.log == "function") { // IE safe
									if (isIE()) {
										if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
											console.log("+" + relativeTime + ": [rifFrontEndLogger had ERROR] " + JSON.stringify(res.data) + // IE safe
												"\n" + message); 
										}
									}
									else {
										console.log("+" + relativeTime + ": [rifFrontEndLogger had ERROR] " + JSON.stringify(res.data) + // IE safe
												"\n" + message); 
									}
								}  	
							}
						}, function (err) {
							if (window.console && console && console.log && typeof console.log == "function") { // IE safe
								if (isIE()) {
									if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
										console.log("+" + relativeTime + ": [rifFrontEndLogger had ERROR] " + // IE safe
											err.mesage);
									}
								}
							}
							else {
								console.log("+" + relativeTime + ": [rifFrontEndLogger had ERROR] " + // IE safe
									err.mesage);
							}
						});
				}
				catch (err) {
					if (window.console && console && console.log && typeof console.log == "function") { // IE safe
						if (isIE()) {
							if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
								console.log("+" + relativeTime + ": [rifFrontEndLogger had ERROR] " + // IE safe
									err.message);
							}
						}
					}
					else {
						console.log("+" + relativeTime + ": [rifFrontEndLogger had ERROR] " + // IE safe
							err.message);
					}	
				}
			}
			
			/*
			 * Function: 	consoleDebug()
			 * Parameters:  Message
			 * Returns: 	Nothing
			 * Description:	IE safe console log for debug messages. Requires debugEnabled to be enabled
			 */
			$scope.consoleDebug = function(msg, rifError) {
				var end=new Date().getTime();
				var elapsed=(Math.round((end - $scope.messageStart)/100))/10; // in S	
				if ($scope.parameters.debugEnabled && window.console && console && console.log && typeof console.log == "function") { // IE safe
					if (isIE()) {
						if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
							console.log("+" + elapsed + ": [DEBUG] " + msg + 
								(rifError ? "\nStack: " + rifError.stack : "")); // IE safe
						}
					}
					else {
						console.log("+" + elapsed + ": [DEBUG] " + msg + 
							(rifError ? "\nStack: " + rifError.stack : "")); // IE safe
					}
				}  			
				callRifFrontEndLogger(
					level.debug,
					msg, 
					undefined /* errorMessage */,
					(rifError ? rifError.stack : undefined) /* err.stack */,
					elapsed);
			}
			
			/*
			 * Function: 	consoleLog()
			 * Parameters:  Message
			 * Returns: 	Nothing
			 * Description:	IE safe console log 
			 */
			$scope.consoleLog = function(msg, rifError) {
				var end=new Date().getTime();
				var elapsed=(Math.round((end - $scope.messageStart)/100))/10; // in S	
				if (window.console && console && console.log && typeof console.log == "function") { // IE safe
					if (isIE()) {
						if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
							console.log("+" + elapsed + ": " + msg + 
								(rifError ? "\nStack: " + rifError.stack : "")); // IE safe
						}
					}
					else {
						console.log("+" + elapsed + ": " + msg + 
								(rifError ? "\nStack: " + rifError.stack : "")); // IE safe
					}
				}  
				
				callRifFrontEndLogger(
					level.info,
					msg, 
					undefined /* errorMessage */,
					(rifError ? rifError.stack : undefined) /* err.stack */,
					elapsed);
			}
			
			/*
			 * Function: 	consoleError()
			 * Parameters:  Message, rif Error object [optional]
			 * Returns: 	Nothing
			 * Description:	IE safe console log for errors
			 *				Log message to console with relative timestamp; save message in array
			 */
			$scope.consoleError = function(msg, rifError) {
				var err; // Get stack
				if (rifError) {
					err=rifError;
				}
				else {
					err=new Error("Auto created by controller");
				}
				
				if (angular.isUndefined($scope.lastMessage)) {
                    $scope.messageCount = 0;
				}
				if (angular.isUndefined($scope.lastMessageTime)) {
                    $scope.lastMessageTime = new Date().getTime();
				}				
				angular.copy(($scope.messageCount++));
				var end=new Date().getTime();
				var elapsed=(Math.round((end - $scope.messageStart)/100))/10; 
						// time since application init in S
				var msgInterval=(Math.round((end - $scope.lastMessageTime)/100))/10; 
						// time since last message in S	
				if (angular.isUndefined($scope.lastMessage) || $scope.lastMessage != msg || msgInterval > 5 /* Secs */) {
					if (window.console && console && console.log && typeof console.log == "function") { // IE safe
						if (isIE()) {
							if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
								console.log("+" + elapsed + ": [ERROR] " + msg); // IE safe
							}
						}
						else {
							console.log("+" + elapsed + ": [ERROR] " + msg); // IE safe
						}
					}  					
				}
				else { // Thses are caused by bugs in notifications, or by the RIF generating the messages to often
					if (window.console && console && console.log && typeof console.log == "function") { // IE safe
						if (isIE()) {
							if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
								console.log("+" + elapsed + ": [ERROR]" + msg + "\nStack: " + err.stack); // IE safe
							}
						}
						else {
							console.log("+" + elapsed + ": [ERROR] " + msg + "\nStack: " + err.stack); // IE safe
						}
					}  
				}
                $scope.lastMessage = angular.copy(msg);
				$scope.lastMessageTime = angular.copy(end);
/*				var msgList = $scope.messageList;
				var msgItem = {
					sequence:	$scope.messageCount,
					message: 	msg,
					time:		end,
					stack:		err.stack,
					relative:	elapsed,
					level:		level.error
				}
				msgList.push(msg); 
				$scope.messageList = angular.copy(msgList);	 */
				
				callRifFrontEndLogger(
					level.error,
					msg, 
					err.message,
					err.stack,
					elapsed);				
			}

			/*
			 * Function:	rifMessage listener
			 * Parameters:	Event, data object:
			 *					Level [ERROR/WARNING/SUCCESS], message, auto hide (after about 5s): true/false, rif Error object [optional] 
			 * Description:	Call rifMessage()
			 * Usage:		$rootScope.$broadcast('rifMessage', { messageLevel: "DEBUG", msg: "Test message" });
			 */			
			$scope.$on('rifMessage', function (event, data) {
				 if (data.messageLevel && data.msg) {
					rifMessage(data.messageLevel, data.msg, data.rifHide || true, data.rifError);			 
				 }
				 else {
					$scope.consoleError("rifMessage has incorrect data: " + JSON.stringify(data), data.rifError);
				 }
			});
			
			/*
			 * Function:	consoleMessage listener
			 * Parameters:	Event, data object:
			 *					Level [DEBUG/INFO/ERROR], message, rif Error object [optional] 
			 * Description:	Call rifMessage()
			 * Usage:		$rootScope.$broadcast('rifMessage', { messageLevel: "DEBUG", msg: "Test message" });
			 */			
			$scope.$on('consoleMessage', function (event, data) {
				if (data.messageLevel && data.msg) {
					if (data.messageLevel.toUpperCase() == "DEBUG") {
						$scope.consoleDebug(data.msg, data.rifError);	
					}	
					else if (data.messageLevel.toUpperCase() == "INFO") {
						$scope.consoleLog(data.msg, data.rifError);	
					}	
					else if (data.messageLevel.toUpperCase() == "ERROR") {
						$scope.consoleError(data.msg, data.rifError);	
					}	
					else {
						$scope.consoleError("consoleMessage has incorrect messageLevel: " + JSON.stringify(data), data.rifError);
					}	
				}
				else if (data.msg == undefined) { // Do nothing
				}
				else {
					$scope.consoleError("consoleMessage has incorrect data: " + JSON.stringify(data), data.rifError);
				}
			});
						
			/*
			 * Function:	rifMessage()
			 * Parameters:	Level [ERROR/WARNING/SUCCESS], message, auto hide (after about 5s): true/false, rif Error object [optional] 
			 * Desription:	Call notifications.show* to display message to user
			 *				Log message to console with relative timestamp; save message in array
             *				Uses ngNotificationsBar.min and ngNotificationsBar.css
			 */
			function rifMessage(messageLevel, msg, rifHide, rifError) {
				var err; // Get stack
				if (rifError) {
					err=rifError;
				}
				else {
					err=new Error("Auto created by controller");
				}
				
				if (angular.isUndefined($scope.lastMessage)) {
                    $scope.messageCount = 0;
				}
				if (angular.isUndefined($scope.lastMessageTime)) {
                    $scope.lastMessageTime = new Date().getTime();
				}				
				angular.copy(($scope.messageCount++));
				var end=new Date().getTime();
				var elapsed=(Math.round((end - $scope.messageStart)/100))/10; 
						// time since application init in S
				var msgInterval=(Math.round((end - $scope.lastMessageTime)/100))/10; 
						// time since last message in S	
				if (angular.isUndefined($scope.lastMessage) || 
				    $scope.lastMessage != msg || msgInterval > 5 /* Secs */) {
					$scope.consoleLog("+" + elapsed + ": [" + $scope.messageCount + "] " + messageLevel + ": " + msg);
					
					if (messageLevel.toUpperCase() == "ERROR") {	
						notifications.showError({
							message: 'Error: ' + msg, 
							hideDelay: $scope.delay, 
							hide: rifHide
							});	
						$scope.consoleLog("Stack: " + err.stack);
						AlertService.addMessage("+" + elapsed, messageLevel.toUpperCase(), msg, err);
					}
					else if (messageLevel.toUpperCase() == "WARNING") {
						notifications.showWarning({
							message: 'Warning: ' + msg, 
							hideDelay: $scope.delay, 
							hide: rifHide
							});
						AlertService.addMessage("+" + elapsed, messageLevel.toUpperCase(), msg, err);
					}
					else if (messageLevel.toUpperCase() == "SUCCESS") {
						notifications.showSuccess({
							message: 'Success: ' + msg, 
							hideDelay: 
							$scope.delay, 
							hide: rifHide});
						AlertService.addMessage("+" + elapsed, messageLevel.toUpperCase(), msg, err);
					}	
				}
				else { // Thses are caused by bugs in notifications, or by the RIF generating the messages too often
					$scope.consoleLog("+" + elapsed + ": [DUPLICATE: " + $scope.messageCount + 
						", msgInterval=" + msgInterval + "] " + 
						messageLevel + ": " + msg);
				}
                $scope.lastMessage = angular.copy(msg);
				$scope.lastMessageTime = angular.copy(end);
/*				var msgList = $scope.messageList;
				var msgItem = {
					sequence:	$scope.messageCount,
					message: 	msg,
					time:		end,
					stack:		err.stack,
					relative:	elapsed,
					level:		messageLevel
				}
				msgList.push(msg); 
				$scope.messageList = angular.copy(msgList);	*/

				callRifFrontEndLogger(
					messageLevel,
					msg, 
					err.message,
					err.stack,
					elapsed);			
			}
			
            $scope.showError = function (msg, rifError) {
				rifMessage("ERROR", msg, true, rifError);
            };
            $scope.showWarning = function (msg) {
				rifMessage("WARNING", msg, true);
            };
            $scope.showSuccess = function (msg) {
				rifMessage("SUCCESS", msg, true);
            };
            $scope.showErrorNoHide = function (msg, rifError) {
				rifMessage("ERROR", msg, false, rifError);
            };						
            $scope.showWarningNoHide = function (msg) {
				rifMessage("WARNING", msg, false);
            };
            $scope.showSuccessNoHide = function (msg) {
				rifMessage("SUCCESS", msg, false);
            };
        }]);