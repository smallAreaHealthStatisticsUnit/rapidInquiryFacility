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
 * CONTROLLER for login 
 */

angular.module("RIF")
        .controller('LoginCtrl', ['$scope', 'user', '$injector',
            'SubmissionStateService', 'StudyAreaStateService', 'CompAreaStateService', 'ExportStateService',
            'ParameterStateService', 'StatsStateService', 'ViewerStateService', 'MappingStateService', 
			'ParametersService', 'SelectStateService',
            function ($scope, user, $injector,
                    SubmissionStateService, StudyAreaStateService, CompAreaStateService, ExportStateService,
                    ParameterStateService, StatsStateService, ViewerStateService, MappingStateService, 
					ParametersService, SelectStateService) {
						
				setFrontEndParameters = function(username) {
					var getFrontEndParameters=undefined;
					user.getFrontEndParameters(username|| "no user").then(function (res) {   
						if (res.data) {
							 try {
/*
									 {
  "parameters": {
    "usePouchDBCache": false,
    "debugEnabled": false,
    "mappingDefaults": {
      "diseasemap1": {},
      "diseasemap2": {},
      "viewermap": {}
    },
    "defaultLogin": {
      "username": "peter",
      "password": "peter"
    }
  }
}
 */	
								if (res.data.file == undefined) {
									$scope.showWarning('Parameter file name not returned by getFrontEndParameters');
									$scope.consoleLog('getFrontEndParameters: ' + JSON.stringify(res.data, null, 0));
								}
								else if (res.data.frontEndParameters) {
									try {
										getFrontEndParameters=JSON5.parse(res.data.frontEndParameters);	
									}
									catch(e) {	 
										 $scope.showWarning('Could not parse front end parameters file: ' + res.data.file + '; parse error: ' + e.message);
										 $scope.consoleLog('getFrontEndParameters: ' + JSON.stringify(res.data, null, 0));
									}
									if (getFrontEndParameters && getFrontEndParameters.parameters && 
										getFrontEndParameters.parameters.defaultLogin) {
										if (username) {		
//											$scope.consoleLog('getFrontEndParameters file: ' + res.data.file +
//												'; parsed: ' + JSON.stringify(getFrontEndParameters.parameters, null, 2));
											ParametersService.setParameters(getFrontEndParameters.parameters);
										}
										else {		
//											$scope.consoleLog('getFrontEndParameters file: ' + res.data.file +
//												'; [restricted] parsed: ' + JSON.stringify(getFrontEndParameters.parameters, null, 2));
											ParametersService.setLoginParameters(getFrontEndParameters.parameters);
											
										}
//										$scope.consoleDebug('INIT setFrontEndParameters: ' + JSON.stringify(ParametersService.getParameters(), null, 0));
										$scope.parameters=ParametersService.getParameters()||{defaultLogin: {
													username: 	"",
													password:	""
												}
											};
//										$scope.consoleDebug('INIT $scope.parameters: ' + JSON.stringify($scope.parameters, null, 2));
										
										if ($scope.parameters.defaultLogin) {
											$scope.username = $scope.username || $scope.parameters.defaultLogin.username;
											$scope.password = $scope.password || $scope.parameters.defaultLogin.password;
										}
									}
									else if (getFrontEndParameters && getFrontEndParameters.parameters) {			
										$scope.showWarning('Missing front end parameters in file: ' + res.data.file);
										$scope.consoleLog('getFrontEndParameters; parsed: ' + JSON.stringify(getFrontEndParameters.parameters, null, 2));
									}
									else if (getFrontEndParameters && getFrontEndParameters.parameters == undefined) {			
										$scope.showWarning('Null parameters in front end parameters file: ' + res.data.file);
										$scope.consoleLog('getFrontEndParameters; parsed: ' + JSON.stringify(getFrontEndParameters, null, 2));
									}
									else {			
										$scope.showWarning('No front end parameters in file: ' + res.data.file);
										$scope.consoleLog('getFrontEndParameters; parsed: ' + JSON.stringify(res.data.frontEndParameters, null, 2));
									}	
								}
								else {
									$scope.showWarning('Parameter file: ' + res.data.file + ' has no frontEndParameters');
									$scope.consoleLog('getFrontEndParameters: ' + JSON.stringify(res.data, null, 0));									
								}
							 }
							 catch(e) {	 
								 $scope.showWarning('Could not load and parse front end parameters file: ' +  e.message);
								 $scope.consoleLog('getFrontEndParameters: ' + JSON.stringify(res.data, null, 0));
							 }
						} else {
							$scope.showWarning('Could not get front end parameters');
							$scope.consoleLog('getFrontEndParameters: ' + JSON.stringify(res, null, 0));
						}	

					}, handleParameterError); 				
				}
					
				setFrontEndParameters(undefined);
				document.getElementById('username').focus(); // Focus on username
				
                //The angular material button has a progress spinner
                $scope.showSpinner = false;

                $scope.login = function () {
                    if (!$scope.showSpinner) {
                        $scope.showSpinner = true;

                        //check if already logged on
                        user.isLoggedIn($scope.username).then(handleLoginCheck, handleServerError);

                        //In development, this bypasses password)
                        //user.login($scope.username, $scope.password).then(handleLogin, handleServerError);

						setFrontEndParameters($scope.username);
                    }
                };

                function handleLoginCheck(res) {
                    //if logged in already then logout
                    if (res.data[0].result === "true") {
                        user.logout($scope.username).then(callLogin, handleServerError);
                    } else {
                        callLogin();
                    }
                }
                function callLogin() {   
					if ($scope.username && $scope.username != "" && $scope.password && $scope.password != "") {                 
						//Encode password
						var encodedPassword = encodeURIComponent($scope.password);
						//log the user in
						user.login($scope.username, encodedPassword).then(handleLogin, handleLogin);		
					}
					else {
                        $scope.showWarning('You must enter a username and password');
                        $scope.showSpinner = false;
					}
                }
                function handleLogin(res) {
                    try {
                        if (res.data[0].result === "User " + $scope.username + " logged in.") {
                            //login success
                            $injector.get('$state').transitionTo('state1');

                            //reset all services
                            SubmissionStateService.resetState();
                            StudyAreaStateService.resetState();
                            CompAreaStateService.resetState();
                            ParameterStateService.resetState();
                            StatsStateService.resetState();
                            ViewerStateService.resetState();
                            MappingStateService.resetState();
                            ExportStateService.resetState();
							SelectStateService.resetState();

                            //initialise the taxonomy service
                            user.initialiseService().then(handleInitialise, handleInitialiseError);
                            function handleInitialise(res) {
                                // $scope.consoleLog("taxonomy initialised"); 
                            }
                            function handleInitialiseError(e) {
                                $scope.showError('Could not initialise the taxonomy service');
                            }
                        } else {
                            //login failed
                            $scope.showSpinner = false;
                        }
                    } catch (error) {
                        $scope.showSpinner = false;
                    }
                }
                function handleServerError(res) {
                    //Tomcat probably not running, could also be CORS error or we are trying to connect to the wrong localhost (check rifs-back-urls)
                    $scope.showSpinner = false;
                    $scope.showError('Could not establish a connection to Tomcat (is it running?)');
                }
                function handleParameterError(res) {
                    //Tomcat probably not running, could also be CORS error or we are trying to connect to the wrong localhost (check rifs-back-urls)
                    $scope.showSpinner = false;
                    $scope.showWarning('Unable to get front end parameters from middleware');
                }
            }]);
