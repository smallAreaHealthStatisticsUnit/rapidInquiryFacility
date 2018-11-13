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
 * SERVICE for Alerts functions. Calls Alert controller
 */
angular.module("RIF")
        .factory('AlertService', ['$rootScope',
                function ($rootScope) {
					$rootScope.messagesList = [];
			
					function rifMessage2(messageLevel, msg, rifHide, rifError) {
						var err; // Get stack
						if (rifError) {
							err=rifError;
						}
						else {
							err=new Error("Auto created by service");
						}
										
						$rootScope.$broadcast('rifMessage', { 
							messageLevel: messageLevel, 
							msg: msg,
							rifHide: rifHide,
							rifError: err
						});						
					}
					
                    return {
						addMessage: function (time, messageLevel, msg, rifError) {
							var err; // Get stack
							if (rifError) {
								err=rifError;
							}
							else {
								err=new Error("Auto created by service");
							}
							
							var msg=angular.copy({ 
								time: time,
								messageLevel: messageLevel, 
								message: msg,
								error: err
							});
							$rootScope.messagesList.push(msg);
//							$rootScope.$broadcast('consoleMessage', { 
//								messageLevel: "DEBUG", 
//								msg: "[rifs-util-alert.js] Add[" + $rootScope.messagesList.length + "]: " + JSON.stringify(msg)
//							});
						},
						getMessageList: function() {
//							$rootScope.$broadcast('consoleMessage', { 
//								messageLevel: "DEBUG", 
//								msg: "[rifs-util-alert.js] Get: " + $rootScope.messagesList.length
//							});
							return angular.copy($rootScope.messagesList);
						},
                        rifMessage: function (messageLevel, msg, rifHide, rifError) {
							rifMessage2(messageLevel, msg, rifHide, rifError);
                        },
						// Back compatibility controller style messages
						showError: function (msg, rifError) {
							rifMessage2("ERROR", msg, true, rifError);
						},
						showWarning: function (msg) {
							rifMessage2("WARNING", msg, true);
						},
						showSuccess: function (msg) {
							rifMessage2("SUCCESS", msg, true);
						},
						showErrorNoHide: function (msg, rifError) {
							rifMessage2("ERROR", msg, false, rifError);
						},	
                        consoleDebug: function (msg, rifError) {
                            $rootScope.$broadcast('consoleMessage', { 
								messageLevel: "DEBUG", 
								msg: msg,
								rifError: rifError
							});
                        },
                        consoleLog: function (msg, rifError) {
                            $rootScope.$broadcast('consoleMessage', { 
								messageLevel: "INFO", 
								msg: msg,
								rifError: rifError
							});
                        },
                        consoleError: function (msg, rifError) {
							var err; // Get stack
							if (rifError) {
								err=rifError;
							}
							else {
								err=new Error("Auto created by service");
							}
											
                            $rootScope.$broadcast('consoleMessage', { 
								messageLevel: "ERROR", 
								msg: msg,
								rifError: err
							});
                        }
                    };

                }]);