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
 * SERVICE $httpInterceptor to handle in and outgoing requests
 * This is just a generic interceptor found on the web somewhere
 */
angular.module("RIF")
        .factory('authInterceptor', ['$q', '$injector', 'AlertService', 
            function ($q, $injector, AlertService) {
                return {
                    request: function (config) {
                        var AuthService = $injector.get('user');
						var currentStateName = $injector.get('$state').current.name
                        //login check here on all outgoing RIF requests to middleware
                        if (!angular.isUndefined(config.headers.rifUser)) {
                            try {
                                AuthService.isLoggedIn(AuthService.currentUser).then(loggedIn, isLoggedInError);
                                function loggedIn(res) {
                                    if (angular.isUndefined(res) || angular.isUndefined(res.data) || res.data[0].result === "false") {
                                        //Redirect to login screen
										if (currentStateName == 'state0') {
											AlertService.consoleError("AuthService.isLoggedIn() failed; already in state0; res: " + 
												((res && res.data) ? JSON.stringify(res.data) : "no res.data"));
										}
										else {
											AlertService.consoleError("AuthService.isLoggedIn() failed; transition from: " + 
											currentStateName + 
												" to state0; res: " + ((res && res.data) ? JSON.stringify(res.data) : "no res.data"));
											$injector.get('$state').transitionTo('state0');
										}
                                    }
                                }
                                function isLoggedInError(err) {
									//Redirect to login screen
									
									if (currentStateName == 'state0') {
										AlertService.consoleError("AuthService.isLoggedIn() error; already in state0; error: " + 
											(err ? err : "no error"));
									}
									else {
										AlertService.consoleError("AuthService.isLoggedIn() error; transition from: " + 
											currentStateName + 
											" to state0; error: " + (err ? err : "no error"));
										$injector.get('$state').transitionTo('state0');
									}
                                }								
                            } catch (e) {
								AlertService.consoleError("Exception in AuthService.isLoggedIn; transition from: " + $injector.get('$state').name + 
									" to state0: " + e.message, e);
                                $injector.get('$state').transitionTo('state0');
                            }
                        }
                        return config;
                    },
                    response: function (res) {
                        //called with the response object from the server				
												
						// Message tracer: REST GET Calls only. Does NOT trace tile GETs						
                        if (!angular.isUndefined(res.config)) {
							var url=res.config.url;
							var restService=undefined;
							var isRest=false;
							if (url.search("http://") == 0 || url.search("https://") == 0) { // Starts with
								isRest=true;
							}
							if (url.indexOf("?") > 0) {
								restService=url.slice(url.lastIndexOf("/")+1, url.indexOf("?"));
							}
							else {
								restService=url.substring(url.lastIndexOf("/")+1);
							}
							if (restService == undefined) {
								url = "[no restService]: " + url;
							}
							else if (restService == "login") { // Hide password
								url = restService;
							}
							else if (restService == "isLoggedIn" || 
									 restService == "getCurrentStatusAllStudies" || 
									 restService == "rifFrontEndLogger") {
								url = undefined;
							}
							
							if (isRest && url && res.config.method == "GET") {
								AlertService.consoleDebug("[rifs-back-interceptor.js] " + res.config.method + ": " + url);
							}	
						}						
						
                        if (!angular.isUndefined(res.data[0])) {
                            if (!angular.isUndefined(res.data[0].errorMessages)) {
                                var scope = $injector.get('$rootScope');
								/* Trap:
								 *
								 * API method "isLoggedIn" has a null "userID" parameter.
								 * Record "User" field "User ID" cannot be empty.
								 */
								if (res.data[0].errorMessages[0] ==
									'API method "isLoggedIn" has a null "userID" parameter.') {
									AlertService.consoleError(res.data[0].errorMessages[0]);
								}
								else if (res.data[0].errorMessages[0] ==
									'Record "User" field "User ID" cannot be empty.') {
									AlertService.consoleError(res.data[0].errorMessages[0]);
								}
								else if (res.data[0].errorMessages[0] ==
									'Unable to roll back database transaction.') {
									AlertService.consoleError(res.data[0].errorMessages[0]);
								}
								else {
									AlertService.showError(res.data[0].errorMessages[0]);
								}
                                return $q.reject(res.data[0].errorMessages[0]);
                            }
                        }
                        return res;
                    },
                    requestError: function (rejection) {
                        return $q.reject("requestError: " + rejection);
                    },
                    responseError: function (rejection) {
                        //for non-200 status
                        return $q.reject("responseError: " + rejection);
                    }
                };
            }])
        .config(["$httpProvider", function ($httpProvider) {
            $httpProvider.interceptors.push('authInterceptor');
			
			// https://stackoverflow.com/questions/1043339/javascript-for-detecting-browser-language-preference
			var getFirstBrowserLanguage = function () {
				var language = navigator.languages && navigator.languages[0] || // Chrome / Firefox
					navigator.language ||   // All browsers
					navigator.userLanguage; // IE <= 10
			   
				return language;
			 };
			 
			$httpProvider.defaults.headers.common["Accept-Language"] = getFirstBrowserLanguage() || "en-US";
        }]);