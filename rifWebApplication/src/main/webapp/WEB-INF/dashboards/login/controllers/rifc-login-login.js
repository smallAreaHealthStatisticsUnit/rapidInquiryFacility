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
            'SubmissionStateService', 'StudyAreaStateService', 'CompAreaStateService',
            'ParameterStateService', 'StatsStateService', 'ViewerStateService', 'MappingStateService',
            function ($scope, user, $injector,
                    SubmissionStateService, StudyAreaStateService, CompAreaStateService,
                    ParameterStateService, StatsStateService, ViewerStateService, MappingStateService) {

                $scope.username = "kgarwood";
                $scope.password = "kgarwood";

                $scope.showSpinner = false;

                $scope.login = function () {
                    if (!$scope.showSpinner) {
                        $scope.showSpinner = true;
                        //check if already logged on
                        user.isLoggedIn($scope.username).then(handleLoginCheck, handleServerError);

                        //In development, this bypasses password)
                       // user.login($scope.username, $scope.password).then(handleLogin, handleServerError);
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
                    //log the user in
                    user.login($scope.username, $scope.password).then(handleLogin, handleLogin);
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

                            //initialise the taxonomy service
                            user.initialiseService().then(handleInitialise, handleInitialiseError);
                            function handleInitialise(res) {
                                // console.log("taxonomy initialised");
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
                    $scope.showSpinner = false;
                }
            }]);